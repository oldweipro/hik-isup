package com.oldwei.isup.controller;

import com.oldwei.isup.model.R;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 云台控制接口
 */
@RestController
@RequestMapping("/api/ptz")
@RequiredArgsConstructor
public class PtzControlController {

    private final ISAPIService isapiService;

    /**
     * 云台控制接口
     */
    @PostMapping("/control")
    public R<String> controlPtz(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "60") int pan,
            @RequestParam(defaultValue = "0") int tilt,
            @RequestParam(defaultValue = "1000") int duration
    ) {
        isapiService.controlPtz(deviceId, pan, tilt, duration);
        return R.ok("云台控制指令已发送");
    }
}
