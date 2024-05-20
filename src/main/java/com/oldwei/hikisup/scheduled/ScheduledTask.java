package com.oldwei.hikisup.scheduled;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.domain.DeviceRemoteControl;
import com.oldwei.hikisup.sdk.service.impl.CmsUtil;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.GlobalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author oldwei
 * @date 2021-9-16 14:10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTask {
    private final CmsUtil cmsUtil;
    private final IMediaStreamService mediaStreamService;
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
        GlobalCacheService.getInstance().getAll().forEach((key, value) -> {
            DeviceCache deviceCache = (DeviceCache) value;
            int lLoginID = deviceCache.getLLoginID();
            // 同步通道号
            DeviceRemoteControl deviceRemoteControl = cmsUtil.CMS_XMLRemoteControl(lLoginID);
            deviceCache.setLChannel(deviceRemoteControl.getLChannel());
            deviceCache.setIsOnline(deviceRemoteControl.getIsOnline());
            // 推流操作
//            if (StringUtils.isNoneBlank(deviceCache.getDeviceId()) && deviceCache.getIsOnline() == 1 && deviceCache.getIsPushed() == 0 && deviceCache.getLChannel() > 0) {
//                deviceCache.setIsPushed(1);
//                mediaStreamService.openStream(deviceCache.getLLoginID(), deviceCache.getLChannel(), deviceCache.getDeviceId());
//            }
            GlobalCacheService.getInstance().put(key, deviceCache);
        });
    }

}
