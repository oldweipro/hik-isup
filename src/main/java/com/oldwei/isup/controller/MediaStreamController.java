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
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mediaStream")
public class MediaStreamController {
    private final IMediaStreamService mediaStreamService;
    private final CmsUtil cmsUtil;
    private final IDeviceService deviceService;
    private final HikStreamProperties hikStreamProperties;

    private static final String UPLOAD_DIR = "upload/audio/";

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

//    @PostMapping("/voiceTrans/{deviceId}")
//    public Mono<R<String>> voiceTrans(
//            @PathVariable String deviceId,
//            @RequestPart("file") FilePart filePart) {
//
//        // 1. 生成唯一文件名
//        String originalFilename = filePart.filename();
//        String suffix = originalFilename.contains(".")
//                ? originalFilename.substring(originalFilename.lastIndexOf("."))
//                : "";
//        String filename = UUID.randomUUID().toString() + suffix;
//        Path targetPath = Paths.get(UPLOAD_DIR, filename);   // 推荐用 Paths.get(基路径, 文件名)
//
//        // 2. 先查询设备（建议也做成响应式，但这里先用阻塞式也行，影响不大）
//        Optional<Device> deviceOpt = deviceService.getOneOpt(
//                new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
//
//        if (deviceOpt.isEmpty()) {
//            return Mono.just(R.fail("设备ID不存在: " + deviceId));
//        }
//        Device device = deviceOpt.get();
//        Integer loginId = device.getLoginId();
//
//        // 3. 把文件保存到磁盘，然后再调用转录服务
//        return filePart.transferTo(targetPath)                 // 这步是真正的保存动作，返回 Mono<Void>
//                .then(Mono.fromCallable(() -> {
//                    byte[] wavBytes = Files.readAllBytes(targetPath);
//
//                    if (wavBytes.length <= 44) {
//                        throw new IllegalArgumentException("无效的WAV文件，太小");
//                    }
//
//                    // 关键：去掉 44 字节 WAV 头，得到纯 PCM
//                    byte[] purePcmBytes = Arrays.copyOfRange(wavBytes, 44, wavBytes.length);
//
//                    // 包装成 InputStream（你的旧代码完全不用改！）
//                    InputStream pcmInputStream = new ByteArrayInputStream(purePcmBytes);
//                    // 文件已经100%写完，此时路径才真正可用
//                    String fileFullPath = targetPath.toAbsolutePath().toString();
//                    // 或者如果你只需要文件名或相对路径，根据实际情况返回
//                    // String fileFullPath = "/uploads/" + filename;
//
//                    // 这里调用你的实时/离线转录服务，把路径传进去
//                    mediaStreamService.voiceTrans(loginId, fileFullPath);
//
//                    return R.ok(fileFullPath);
//                }));
//    }

    @PostMapping("/voiceTrans/{deviceId}")
    public Mono<R<String>> voiceTrans(
            @PathVariable String deviceId,
            @RequestPart("file") FilePart filePart) {

        // 1. 生成临时文件路径（仍然需要临时落地，因为 FilePart 是流式上传）
        String filename = UUID.randomUUID() + ".wav";
        Path tempWavPath = Paths.get(UPLOAD_DIR, filename);

        // 2. 查询设备（保持不变）
        Optional<Device> deviceOpt = deviceService.getOneOpt(
                new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));

        if (deviceOpt.isEmpty()) {
            return Mono.just(R.fail("设备ID不存在: " + deviceId));
        }
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();

        return filePart.transferTo(tempWavPath)  // 先把前端上传的 wav 完整落地
                .then(Mono.fromCallable(() -> {
                    try {
                        byte[] wavBytes = Files.readAllBytes(tempWavPath);

                        if (wavBytes.length <= 44) {
                            throw new IllegalArgumentException("无效的WAV文件，太小");
                        }

                        // 关键：去掉 44 字节 WAV 头，得到纯 PCM
                        byte[] purePcmBytes = Arrays.copyOfRange(wavBytes, 44, wavBytes.length);

                        // 包装成 InputStream（你的旧代码完全不用改！）
                        InputStream pcmInputStream = new ByteArrayInputStream(purePcmBytes);

                        // 直接调用你原来的转录服务（原来接收 FileInputStream 的地方完全兼容）
                        mediaStreamService.voiceTrans(loginId, pcmInputStream);
                        // 注意：如果你的 voiceTrans 方法内部会 close 这个 stream，这里不需要你手动 close
                        // ByteArrayInputStream close 是空操作，安全

                        // 用完即删临时 wav 文件
                        Files.deleteIfExists(tempWavPath);

                        return R.ok("上传成功，内存转纯PCM直传，已处理 " + purePcmBytes.length + " bytes");

                    } catch (Exception e) {
                        // 出错也要尝试删临时文件
                        Files.deleteIfExists(tempWavPath);
                        throw new RuntimeException("处理音频失败: " + e.getMessage(), e);
                    }
                }));
    }
}
