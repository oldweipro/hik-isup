package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
public class FDController {
    private final ISAPIService isapiService;
    private final IDeviceService deviceService;

    /**
     * 云台控制接口
     */
    @GetMapping("/search")
    public R<String> search(
            @RequestParam int userId
    ) {
        return R.ok(isapiService.FDSearch(userId));
    }

    /**
     * 异步导入数据接口
     * 图片大小要求在200kb以下的jpg格式文件
     *
     */
    @GetMapping("/asyncImportDatas")
    public R<String> asyncImportDatas(
            @RequestParam String deviceId,
            @RequestParam String xmlUrl
    ) {
        Optional<Device> oneOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceId, deviceId));
        if (oneOpt.isPresent()) {
            Device device = oneOpt.get();
            Integer loginId = device.getLoginId();
            if (loginId == null) {
                return R.fail("设备未登录，无法导入数据");
            }
            return R.ok(isapiService.asyncImportDatas(loginId, xmlUrl));
        }
        return R.fail();
    }

    /**
     * 云台控制接口
     */
    @GetMapping("/searchLPListAudit")
    public R<String> searchLPListAudit(
            @RequestParam int userId,
            @RequestParam int channelId
    ) {
        return R.ok(isapiService.searchLPListAudit(userId, channelId));
    }
}
