package com.oldwei.isup.controller;

import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.service.DeviceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 人脸检测接口
 */
@Slf4j
@RestController
@RequestMapping("/api/face-detections")
@RequiredArgsConstructor
public class FaceDetectionController {
    
    private final ISAPIService isapiService;
    private final DeviceCacheService deviceCacheService;

    /**
     * 异步导入人脸数据
     * 图片大小要求在200kb以下的jpg格式文件
     */
    @PostMapping("/import")
    public R<String> importData(
            @RequestParam String deviceId,
            @RequestParam String xmlUrl) {
        
        log.debug("导入人脸数据 - deviceId: {}, xmlUrl: {}", deviceId, xmlUrl);
        
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return R.fail("设备不存在");
        }
        
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();
        if (loginId == null) {
            return R.fail("设备未登录，无法导入数据");
        }
        
        return R.ok(isapiService.asyncImportDatas(loginId, xmlUrl));
    }
}
