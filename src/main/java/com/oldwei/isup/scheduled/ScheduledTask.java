package com.oldwei.isup.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.domain.DeviceRemoteControl;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.IDeviceService;
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
    private final IDeviceService deviceService;
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
        deviceService.list(new LambdaQueryWrapper<Device>().gt(Device::getLoginId, -1)).forEach((device) -> {
            int lLoginID = device.getLoginId();
            // 同步通道号
            DeviceRemoteControl deviceRemoteControl = cmsUtil.CMS_XMLRemoteControl(lLoginID);
            int flag = 0;
            if (device.getChannel() != null && !device.getChannel().equals(deviceRemoteControl.getLChannel())) {
                // 通道号不一致，说明设备可能重启了，重新登录
                device.setChannel(deviceRemoteControl.getLChannel());
                flag++;
            }
            if (flag > 0) {
                deviceService.updateById(device);
            }
        });
    }

}
