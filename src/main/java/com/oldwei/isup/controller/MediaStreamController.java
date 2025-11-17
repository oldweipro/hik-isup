package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.domain.DeviceRemoteControl;
import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.vo.PlayURL;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mediaStream")
public class MediaStreamController {
    private final IMediaStreamService mediaStreamService;
    private final CmsUtil cmsUtil;
    private final IDeviceService deviceService;
    private final HikStreamProperties hikStreamProperties;

    @GetMapping("/deviceList")
    public R<List<Device>> getDeviceList(Device device) {
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        if (device.getDeviceId() != null && !device.getDeviceId().isEmpty()) {
            queryWrapper.like(Device::getDeviceId, device.getDeviceId());
        }
        queryWrapper.eq(device.getIsOnline() != null, Device::getIsOnline, device.getIsOnline());
        List<Device> devices = deviceService.list(queryWrapper);
        return R.ok(devices);
    }

    /**
     * 1. 设备列表
     * 2. 设备详情
     * 3. 设备预览
     * 4.
     */

    /**
     * 保存视频录像并返回视频地址
     *
     * @param deviceId
     * @return
     */
    @PostMapping("/preview/{deviceId}")
    public R<PlayURL> startPreview(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            Integer sessionId = StreamManager.userIDandSessionMap.get(device.getLoginId() * 100 + device.getChannel());
            if (sessionId == null) {
                mediaStreamService.preview(device);
            } else {
                StreamHandler streamHandler = StreamManager.concurrentMap.get(sessionId);
                if (device.getIsOnline() == 1) {
                    if (streamHandler == null) {
                        log.info("设备{}预览流不存在，开始创建预览流", deviceId);
                        mediaStreamService.preview(device);
                    }
                } else {
                    return R.fail("设备不在线，无法预览");
                }
            }
            PlayURL playURL = new PlayURL();
//                    playURL.setWsFlv("ws://192.168.2.235:9002/?playKey=" + deviceId);
            playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" + hikStreamProperties.getRtmp().getPort() + "/live/" + device.getDeviceId());
            playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" + hikStreamProperties.getHttp().getPort() + "/live/" + device.getDeviceId() + ".live.flv");
            return R.ok(playURL);
        }
        return R.fail("设备不存在，无法预览");
    }

    @DeleteMapping("/preview/{deviceId}")
    public R<Boolean> stopPreview(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        deviceOpt.ifPresent(mediaStreamService::stopPreview);
        return R.ok(true);
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
    @GetMapping("/cachedDeviceList")
    public List<Device> deviceList() {
        return deviceService.list();
    }

    @GetMapping("/device/{deviceId}")
    public DeviceRemoteControl device(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            PpvspMessage ppvspMessage = cmsUtil.CMS_XMLRemoteControl(device.getLoginId());
            DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
            deviceRemoteControl.setIsOnline(1);
            String ch = ppvspMessage.getParams().getDeviceStatusXML().getChStatus().getCh();
            deviceRemoteControl.setLChannel(ch);
            return deviceRemoteControl;
        }
        DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
        deviceRemoteControl.setIsOnline(0);
        return deviceRemoteControl;
    }
}
