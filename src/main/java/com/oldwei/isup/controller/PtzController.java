package com.oldwei.isup.controller;

import com.oldwei.isup.model.R;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 云台控制接口
 */
@Slf4j
@RestController
@RequestMapping("/api/devices/{deviceId}/ptz")
@RequiredArgsConstructor
public class PtzController {

    private final ISAPIService isapiService;

    /**
     * 控制云台运动
     *
     * @param deviceId 设备ID
     * @param pan      水平角度
     * @param tilt     垂直角度
     * @param duration 持续时间(毫秒)
     */
    @PostMapping
    public R<String> control(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "1") int channelId,
            @RequestParam(defaultValue = "60") int pan,
            @RequestParam(defaultValue = "0") int tilt,
            @RequestParam(defaultValue = "1000") int duration) {

        log.debug("云台控制 - deviceId: {}, pan: {}, tilt: {}, duration: {}",
                deviceId, pan, tilt, duration);

        isapiService.controlPtz(deviceId, channelId, pan, tilt, duration);
        return R.ok("云台控制指令已发送");
    }
}
