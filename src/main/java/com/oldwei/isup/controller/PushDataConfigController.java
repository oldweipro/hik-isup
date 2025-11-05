package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.model.PushDataConfig;
import com.oldwei.isup.model.R;
import com.oldwei.isup.service.IPushDataConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pushDataConfig")
@RequiredArgsConstructor
public class PushDataConfigController {
    private final IPushDataConfigService pushDataConfigService;

    /**
     * 获取列表
     */
    @GetMapping
    public R<List<PushDataConfig>> list(@RequestParam PushDataConfig pushDataConfig) {
        return R.ok(pushDataConfigService.list(new LambdaQueryWrapper<PushDataConfig>()
                        .eq(pushDataConfig.getEnable() != null, PushDataConfig::getEnable, pushDataConfig.getPushPath())
                        .eq(pushDataConfig.getId() != null, PushDataConfig::getId, pushDataConfig.getId())
                )
        );
    }

    /**
     * 设置推送地址
     */
    @PostMapping
    public R<String> setting(@RequestBody PushDataConfig pushDataConfig) {
        if (pushDataConfigService.saveOrUpdate(pushDataConfig)) {
            return R.ok("设置推送地址成功");
        } else {
            return R.fail("设置推送地址失败");
        }
    }

    /**
     * 删除推送地址
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable String id) {
        if (pushDataConfigService.removeById(id)) {
            return R.ok("删除推送地址成功");
        } else {
            return R.fail("删除推送地址失败");
        }
    }
}
