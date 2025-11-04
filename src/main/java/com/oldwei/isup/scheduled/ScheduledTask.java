package com.oldwei.isup.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.xml.DeviceInfo;
import com.oldwei.isup.model.xml.InputProxyChannelStatusList;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        deviceService.list(new LambdaQueryWrapper<Device>().gt(Device::getLoginId, -1).eq(Device::getParentId, 0)).forEach((device) -> {
            int lLoginID = device.getLoginId();
            // 同步通道号
            DeviceInfo xmlContent = isapiService.GetDevInfo(lLoginID);
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
        });
    }

}
