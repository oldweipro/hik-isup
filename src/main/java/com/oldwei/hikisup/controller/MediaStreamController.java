package com.oldwei.hikisup.controller;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.domain.DeviceRemoteControl;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.GlobalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

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

    /**
     * 保存视频录像并返回视频地址
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/saveStream/{deviceId}")
    public String saveStream(@PathVariable String deviceId) {
        DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
        mediaStreamService.saveStream(stream.getLLoginID(), stream.getLChannel(), stream.getDeviceId());
        return "oh yeah!";
    }

    @GetMapping("/video")
    public ResponseEntity<Resource> getVideo() throws MalformedURLException {
        Path videoPath = Paths.get("/opt/hik-isup/video/AZ8888888.mp4");
        Resource videoResource = new UrlResource(videoPath.toUri());

        if (videoResource.exists() && videoResource.isReadable()) {
            return ResponseEntity.ok().body(videoResource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * 获取当前系统内注册上的设备列表
     *
     * @return
     */
    @GetMapping("/deviceList")
    public Map<String, Object> deviceList() {
//        String json = JSON.toJSONString(map);
        return GlobalCacheService.getInstance().getAll();
    }

    @GetMapping("/device/{deviceId}")
    public DeviceRemoteControl device(@PathVariable String deviceId) {
        Object object = GlobalCacheService.getInstance().get(deviceId);
        if (Objects.nonNull(object)) {
            DeviceCache device = (DeviceCache) object;
            if (Objects.nonNull(device)) {
                int lLoginID = device.getLLoginID();
                return cmsDemo.CMS_XMLRemoteControl(lLoginID);
            }
        }
        DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
        deviceRemoteControl.setIsOnline(0);
        return deviceRemoteControl;
    }
}
