package com.oldwei.hikisup.controller;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.domain.DeviceRemoteControl;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.service.impl.CmsUtil;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.GlobalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
    private final CmsUtil cmsUtil;

    @GetMapping("/pushStream")
    public String pushStream() {
        Map<String, Object> objectMap = GlobalCacheService.getInstance().getAll();
        // 检查 objectMap 是否为 null 或者是否为空
        if (objectMap != null && !objectMap.isEmpty()) {
            // 取出一个对象
            Map.Entry<String, Object> entry = objectMap.entrySet().iterator().next();
            String key = entry.getKey();
            DeviceCache stream = (DeviceCache) entry.getValue();
            mediaStreamService.saveStream(stream.getLLoginID(), stream.getLChannel(), stream.getDeviceId());
            // 打印 key 和 value
            return "Key: " + key + ", Value: " + stream;
        } else {
            // objectMap 为空或为 null 的情况
            return "objectMap is null or empty.";
        }
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
    public ResponseEntity<Resource> getVideo() throws IOException {
        Path videoPath = Paths.get("AZ8888888.mp4");
        // 指定要下载的文件路径
        URL fileDownloadUrl = videoPath.toUri().toURL();
        UrlResource resource = new UrlResource(fileDownloadUrl);

        // 确保文件存在且可读
        if (resource.exists() && resource.isReadable()) {
            // 获取文件名
            String fileName = resource.getFilename();

            // 设置Content-Disposition头，用于定义文件名和传输编码
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            // 设置Content-Type为文件类型，这里使用文件名进行猜测
            String contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null) {
                contentType = "application/octet-stream"; // 默认类型
            }
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            // 设置Content-Length头，表示文件大小
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));

            // 创建ResponseEntity对象，使用文件流作为响应体
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } else {
            // 如果文件不存在或不可读，则返回404
            return ResponseEntity.notFound().build();
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
                return cmsUtil.CMS_XMLRemoteControl(lLoginID);
            }
        }
        DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
        deviceRemoteControl.setIsOnline(0);
        return deviceRemoteControl;
    }
}
