package com.oldwei.isup.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.config.HikPlatformProperties;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.vo.UploadData;
import com.oldwei.isup.model.xml.DeviceInfo;
import com.oldwei.isup.model.xml.InputProxyChannelStatusList;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import com.oldwei.isup.util.WebFluxHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author oldwei
 * @date 2021-9-16 14:10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTask {
    private final CmsUtil cmsUtil;
    private final IDeviceService deviceService;
    private final ISAPIService isapiService;
    private final IMediaStreamService mediaStreamService;
    private final HikPlatformProperties hikPlatformProperties;

    // Cron表达式范例：
    //
    //每隔5秒执行一次：*/5 * * * * ?
    //
    //每隔1分钟执行一次：0 */1 * * * ?
    //
    //每天23点执行一次：0 0 23 * * ?
    //
    //每天凌晨1点执行一次：0 0 1 * * ?
    //
    //每月1号凌晨1点执行一次：0 0 1 1 * ?
    //
    //每月最后一天23点执行一次：0 0 23 L * ?
    //
    //每周星期天凌晨1点实行一次：0 0 1 ? * L
    //
    //在26分、29分、33分执行一次：0 26,29,33 * * * ?
    //
    //每天的0点、13点、18点、21点都执行一次：0 0 0,13,18,21 * * ?

    /**
     * 一分钟执行一次
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void searchHikDevice() {
        // 每隔一分钟扫描一次设备
//        log.info("每隔五秒钟扫描一次设备");
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().gt(Device::getLoginId, -1).eq(Device::getParentId, 0));
        if (list.isEmpty()) return;
        list.forEach((device) -> {
            int lLoginID = device.getLoginId();
            // 同步通道号
            DeviceInfo xmlContent = isapiService.GetDevInfo(lLoginID);
            if (xmlContent != null) {
                // 判断设备类型
                switch (xmlContent.getDeviceType()) {
                    case "DVR" -> {
                        // 已知：警灯盒子
                        InputProxyChannelStatusList channelStatusList = isapiService.GetAllDigitalChannelStatus(lLoginID);
//                    log.info("设备类型为DVR\n{}", channelStatusList);
                        // 再套一层循环，遍历通道状态
                        channelStatusList.getChannels().forEach(channelStatus -> {
                            if (channelStatus.getOnline()) {
                                String channel = String.valueOf(channelStatus.getId());
                                String deviceId = device.getDeviceId() + "_" + channel;
//                            log.info("通道号: {}", channel);
                                Optional<Device> oneOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
                                if (oneOpt.isPresent()) {
                                    Device subDevice = oneOpt.get();
                                    // 通道号不一致，说明设备可能重启了，重新登录
//                                log.info("已找到对应的子设备: {}", one);
                                    subDevice.setChannel(Integer.valueOf(channel));
                                    subDevice.setLoginId(device.getLoginId());
                                    subDevice.setIsOnline(1);
                                    deviceService.updateById(subDevice);
                                } else {
//                                log.warn("未找到对应的子设备: {}", deviceId);
                                    Device subDevice = new Device();
                                    subDevice.setParentId(device.getId());
                                    subDevice.setDeviceId(deviceId);
                                    subDevice.setIsOnline(1);
                                    subDevice.setLoginId(device.getLoginId());
                                    subDevice.setChannel(Integer.valueOf(channel));
                                    deviceService.save(subDevice);
//                                log.info("已创建子设备: {}", deviceId);
                                }
                            }
                        });
                    }
                    case "NVR" -> log.info("设备类型为NVR");
                    case "IPCamera" -> {
//                    log.info("设备类型为IPCamera");
                        PpvspMessage msg = cmsUtil.CMS_XMLRemoteControl(lLoginID);
                        String channel = String.valueOf(msg.getParams().getDeviceStatusXML().getChStatus().getCh().charAt(0));
//                    log.info("通道号: {}", channel);
                        // 通道号不一致，说明设备可能重启了，重新登录
                        device.setChannel(Integer.valueOf(channel));
                        deviceService.updateById(device);
                    }
                    default -> log.info("设备类型未知: {}", xmlContent.getDeviceType());
                }
            }
        });
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void stopPreview() {
        // 每隔3秒钟扫描一次设备
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().gt(Device::getLoginId, -1).eq(Device::getIsOnline, 1));
        if (list.isEmpty()) return;
        list.forEach((device) -> {
            int loginId = device.getLoginId();
            int channelId = device.getChannel();
            int loginchannelId = loginId * 100 + channelId;

//            log.info("检查设备{}-{} 是否需要停止预览", device.getDeviceId(), channelId);
            Boolean flag = StreamManager.loginchannelIdAndstopflag.get(loginchannelId);
            if (flag != null && flag) {
                log.info("设备{}-{} 停止预览", device.getDeviceId(), channelId);
                UploadData uploadData = new UploadData();
                uploadData.setDataType("PushStreamStop");
                uploadData.setData(device.getDeviceId());
                String pushPath = hikPlatformProperties.getPushAddress();
                WebFluxHttpUtil.postAsync(pushPath, uploadData, String.class).subscribe(resp -> {
                    log.info("推送到 {} 返回结果：{}", pushPath, resp);
                }, error -> {
                    log.error("推送到 {} 失败：{}", pushPath, error.getMessage());
                });
                StreamManager.loginchannelIdAndstopflag.remove(loginchannelId);
                mediaStreamService.stopPreview(device);
            }
        });
    }

    @Scheduled(cron = "*/3 * * * * ?")
    public void stopPlaybackPreview() {
        // 每隔3秒钟扫描一次设备
        List<Device> list = deviceService.list(new LambdaQueryWrapper<Device>().gt(Device::getLoginId, -1).eq(Device::getIsOnline, 1));
        if (list.isEmpty()) return;
        list.forEach((device) -> {
            int loginId = device.getLoginId();
            int channelId = device.getChannel();
            int loginchannelId = loginId * 100 + channelId;

            Boolean flag = StreamManager.playbackLoginchannelIdAndstopflag.get(loginchannelId);
            if (flag != null && flag) {
                mediaStreamService.stopPlayBackByTime(loginId);
            }
        });
    }

}
