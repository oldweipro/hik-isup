package com.oldwei.hikisup.controller;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.GlobalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mediaStream")
public class MediaStreamController {
    private final IMediaStreamService mediaStreamService;
    private final CmsDemo cmsDemo;

    @GetMapping("/pushStream")
    public String pushStream() {

        return "oh yeah!";
    }

    @DeleteMapping("/pushStream/{deviceId}")
    public String deletePushStream(@PathVariable String deviceId) {
        DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
        mediaStreamService.deleteStreamCV(stream.getLLoginID());
        return "oh yeah!";
    }

    @GetMapping("/openStream/{deviceId}")
    public String openStream(@PathVariable String deviceId, @RequestParam("liveAddress") String liveAddress) {
        DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
        mediaStreamService.openStreamCV(stream.getLLoginID(), stream.getLChannel(), stream.getDeviceId(), liveAddress);
        return "oh yeah!";
    }

    @GetMapping("/saveStream/{deviceId}")
    public String saveStream(@PathVariable String deviceId) {
        DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
        mediaStreamService.saveStream(stream.getLLoginID(), stream.getLChannel(), stream.getDeviceId());
        return "oh yeah!";
    }

    @GetMapping("/streamList")
    public Map<String, Object> streamList() {
        return GlobalCacheService.getInstance().getAll();
    }

    @GetMapping("/device/{lLoginID}")
    public Map<String, Object> device(@PathVariable int lLoginID) {
        log.info("lLoginID: {}", lLoginID);
        cmsDemo.CMS_XMLRemoteControl(lLoginID);
        return GlobalCacheService.getInstance().getAll();
    }
}
