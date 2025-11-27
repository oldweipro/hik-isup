package com.oldwei.isup.scheduled;

import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.xml.DeviceInfo;
import com.oldwei.isup.model.xml.InputProxyChannelStatusList;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.DeviceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author oldwei
 * @date 2021-9-16 14:10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTask {
    private final CmsUtil cmsUtil;
    private final DeviceCacheService deviceCacheService;
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
     * TODO 迁移到设备回调中
     * 同步海康设备信息
     * 定期同步设备的通道信息和在线状态
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void syncHikDevice() {
        // 获取所有已注册的设备
        Map<Integer, String> loginIdMap = getAllRegisteredDevices();

        if (loginIdMap.isEmpty()) {
            log.debug("没有在线的设备需要同步");
            return;
        }
        loginIdMap.forEach(this::syncDeviceChannels);
    }

    /**
     * 获取所有已注册的设备
     */
    private Map<Integer, String> getAllRegisteredDevices() {
        Map<Integer, String> result = new HashMap<>();
        deviceCacheService.listAll().stream()
                .filter(d -> d.getLoginId() != null && d.getLoginId() > -1)
                .forEach(d -> result.putIfAbsent(d.getLoginId(), d.getDeviceId()));
        return result;
    }

    /**
     * 同步单个设备的通道信息
     * 统一处理所有设备类型
     *
     * @param lLoginID 登录句柄
     * @param deviceId 设备ID
     */
    private void syncDeviceChannels(Integer lLoginID, String deviceId) {
        try {
            // 获取设备信息
            DeviceInfo deviceInfo = isapiService.GetDevInfo(lLoginID);
            if (deviceInfo == null) {
                log.warn("获取设备信息失败，设备ID: {}, 登录句柄: {}", deviceId, lLoginID);
                return;
            }

            String deviceType = deviceInfo.getDeviceType();
            log.debug("同步设备: {}, 类型: {}", deviceId, deviceType);

            // 获取通道列表
            List<Integer> onlineChannelIds = getDeviceChannels(lLoginID, deviceType);
            if (onlineChannelIds == null || onlineChannelIds.isEmpty()) {
                log.warn("未能获取设备通道信息，设备ID: {}, 类型: {}", deviceId, deviceType);
                return;
            }

            // 获取或创建设备对象
            Device device = deviceCacheService.getByDeviceId(deviceId).orElse(new Device());
            device.setDeviceId(deviceId);
            device.setDeviceType(deviceType);
            device.setIsOnline(1);
            device.setLoginId(lLoginID);

            // 更新通道列表
            updateDeviceChannels(device, onlineChannelIds);

            // 保存设备
            deviceCacheService.saveOrUpdate(device);
        } catch (Exception e) {
            log.error("同步设备通道失败，设备ID: {}, 登录句柄: {}", deviceId, lLoginID, e);
        }
    }

    /**
     * 更新设备的通道列表
     *
     * @param device           设备对象
     * @param onlineChannelIds 在线通道ID列表
     */
    private void updateDeviceChannels(Device device, List<Integer> onlineChannelIds) {
        Map<Integer, Device.Channel> existingChannels = device.getChannels().stream()
                .collect(Collectors.toMap(Device.Channel::getChannelId, ch -> ch));

        // 创建新的通道列表
        List<Device.Channel> newChannels = new ArrayList<>();

        // 更新或创建在线通道
        for (Integer channelId : onlineChannelIds) {
            Device.Channel channel = existingChannels.get(channelId);
            if (channel != null) {
                // 已存在的通道，标记为在线
                channel.setIsOnline(1);
                newChannels.add(channel);
            } else {
                // 新通道
                newChannels.add(new Device.Channel(channelId, 1));
                log.info("发现新通道: {}_{}", device.getDeviceId(), channelId);
            }
        }

        // 将不在线的通道标记为离线（保留历史通道）
        existingChannels.values().stream()
                .filter(ch -> !onlineChannelIds.contains(ch.getChannelId()))
                .forEach(ch -> {
                    if (ch.getIsOnline() == 1) {
                        ch.setIsOnline(0);
                        newChannels.add(ch);
                        log.info("通道离线: {}_{}", device.getDeviceId(), ch.getChannelId());
                    } else {
                        newChannels.add(ch);
                    }
                });

        device.setChannels(newChannels);
    }

    /**
     * 获取设备的通道列表
     * 统一处理DVR、NVR、IPCamera等类型
     *
     * @param lLoginID   登录句柄
     * @param deviceType 设备类型
     * @return 在线通道ID列表
     */
    private List<Integer> getDeviceChannels(int lLoginID, String deviceType) {
        try {
            switch (deviceType) {
                case "DVR", "NVR" -> {
                    // DVR/NVR设备：获取所有数字通道状态
                    InputProxyChannelStatusList channelStatusList = isapiService.GetAllDigitalChannelStatus(lLoginID);
                    if (channelStatusList != null && channelStatusList.getChannels() != null) {
                        return channelStatusList.getChannels().stream()
                                .filter(ch -> ch.getOnline() != null && ch.getOnline())
                                .map(ch -> ch.getId())
                                .collect(Collectors.toList());
                    }
                }
                case "IPCamera" -> {
                    // IPCamera设备：通过远程控制获取通道号
                    PpvspMessage msg = cmsUtil.CMS_XMLRemoteControl(lLoginID);
                    if (msg != null && msg.getParams() != null &&
                            msg.getParams().getDeviceStatusXML() != null &&
                            msg.getParams().getDeviceStatusXML().getChStatus() != null) {

                        String channelStr = msg.getParams().getDeviceStatusXML().getChStatus().getCh();
                        if (channelStr != null && !channelStr.isEmpty()) {
                            try {
                                // 尝试直接解析为数字
                                Integer channelId = Integer.valueOf(channelStr.trim());
                                return Collections.singletonList(channelId);
                            } catch (NumberFormatException e) {
                                // 尝试取第一个字符
                                try {
                                    Integer channelId = Integer.valueOf(String.valueOf(channelStr.charAt(0)));
                                    return Collections.singletonList(channelId);
                                } catch (Exception ex) {
                                    log.error("解析IPCamera通道号失败，通道字符串: {}", channelStr);
                                }
                            }
                        }
                    }
                }
                default -> log.warn("不支持的设备类型: {}", deviceType);
            }
        } catch (Exception e) {
            log.error("获取设备通道失败，设备类型: {}", deviceType, e);
        }
        return Collections.emptyList();
    }
}
