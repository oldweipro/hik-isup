package com.oldwei.isup.controller;

import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.DeviceRemoteControl;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.DeviceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 设备管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    
    private final DeviceCacheService deviceCacheService;
    private final CmsUtil cmsUtil;

    /**
     * 获取设备列表
     */
    @GetMapping
    public R<List<Device>> getDevices(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Integer isOnline) {
        List<Device> devices = deviceCacheService.list(d -> {
            boolean match = true;
            if (deviceId != null && !deviceId.isEmpty()) {
                match = d.getDeviceId().contains(deviceId);
            }
            if (match && isOnline != null) {
                match = isOnline.equals(d.getIsOnline());
            }
            return match;
        });
        return R.ok(devices);
    }

    /**
     * 获取设备详情
     */
    @GetMapping("/{deviceId}")
    public R<Device> getDevice(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        return deviceOpt.map(R::ok)
                .orElse(R.fail("设备不存在"));
    }

    /**
     * 获取设备远程控制信息
     */
    @GetMapping("/{deviceId}/remote-control")
    public R<DeviceRemoteControl> getDeviceRemoteControl(@PathVariable String deviceId) {
        DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            PpvspMessage ppvspMessage = cmsUtil.CMS_XMLRemoteControl(device.getLoginId());
            deviceRemoteControl.setIsOnline(1);
            String ch = ppvspMessage.getParams().getDeviceStatusXML().getChStatus().getCh();
            deviceRemoteControl.setLChannel(ch);
            return R.ok(deviceRemoteControl);
        }
        
        deviceRemoteControl.setIsOnline(0);
        return R.ok(deviceRemoteControl);
    }
}
