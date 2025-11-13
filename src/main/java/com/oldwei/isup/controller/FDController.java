package com.oldwei.isup.controller;

import com.oldwei.isup.model.R;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fd")
@RequiredArgsConstructor
public class FDController {
    private final ISAPIService isapiService;

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
     * 云台控制接口
     */
    @GetMapping("/asyncImportDatas")
    public R<String> asyncImportDatas(
            @RequestParam int userId
    ) {
        return R.ok(isapiService.asyncImportDatas(userId));
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
