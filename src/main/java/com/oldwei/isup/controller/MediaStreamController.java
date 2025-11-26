package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.domain.DeviceRemoteControl;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.tts.DataItem;
import com.oldwei.isup.model.tts.TtsRequest;
import com.oldwei.isup.model.vo.PlayURL;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import com.oldwei.isup.util.WebFluxHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            System.out.println("sessionId=" + sessionId);
            mediaStreamService.preview(device);
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

    @PostMapping("/generateTtsVoiceTrans/{deviceId}")
    public Mono<R<Object>> generateTtsVoiceTrans(
            @PathVariable String deviceId,
            @RequestBody DataItem dataItem) {

        // 1. 生成唯一文件名
        String filename = UUID.randomUUID().toString();
        String mp3Filename = "tts_" + filename + ".mp3";
        String pcmFilename = "tts_" + filename + "_8k.pcm";
        Path mp3TargetPath = Paths.get(UPLOAD_DIR, mp3Filename);
        Path pcmTargetPath = Paths.get(UPLOAD_DIR, pcmFilename);

        // 2. 查询设备
        Optional<Device> deviceOpt = deviceService.getOneOpt(
                new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));

        if (deviceOpt.isEmpty()) {
            return Mono.just(R.fail("设备ID不存在: " + deviceId));
        }
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();

        // 3. 构建请求数据
        List<DataItem> dataList = Arrays.asList(
                new DataItem("路人甲",
                        dataItem.getText(),
                        "zh-CN-YunjianNeural",
                        "0%")
        );

        TtsRequest request = new TtsRequest();
        request.setData(dataList);

        String url = "http://localhost:3000/api/v1/tts/generateJson";

        // 4. 异步发送TTS请求并处理文件转换和转录
        return WebFluxHttpUtil.postAsync(url, request, byte[].class)
                .flatMap(audioData -> {
                    if (audioData == null || audioData.length == 0) {
                        return Mono.error(new RuntimeException("TTS音频数据为空"));
                    }

                    // 5. 保存音频文件
                    return Mono.fromCallable(() -> {
                        // 保存MP3文件
                        try (FileOutputStream fos = new FileOutputStream(mp3TargetPath.toFile())) {
                            fos.write(audioData);
                            fos.flush();
                        } catch (IOException e) {
                            throw new RuntimeException("保存MP3文件失败: " + e.getMessage());
                        }

                        // 6. 检查MP3文件是否存在且有内容
                        if (!Files.exists(mp3TargetPath) || Files.size(mp3TargetPath) == 0) {
                            throw new IllegalArgumentException("生成的音频文件为空或不存在");
                        }

                        // 7. 使用FFmpeg将MP3转换为8K 16bit单声道PCM
                        ProcessBuilder processBuilder = new ProcessBuilder(
                                "ffmpeg",
                                "-i", mp3TargetPath.toAbsolutePath().toString(), // 输入文件路径
                                "-f", "mulaw",                    // 输出格式：μ-law编码
                                "-ac", "1",                                     // 输出声道数：单声道
                                "-ar", "8000",                                  // 输出采样率：8K
                                "-acodec", "pcm_mulaw",           // 明确指定编码器
                                "-y",                                           // 覆盖输出文件
                                pcmTargetPath.toAbsolutePath().toString()       // 输出文件路径
                        );

                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();

                        if (exitCode != 0) {
                            // 转码失败，读取错误信息
                            StringBuilder errorOutput = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getErrorStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    errorOutput.append(line).append("\n");
                                }
                            }
                            throw new RuntimeException("音频转码失败，FFmpeg退出码: " + exitCode +
                                    ", 错误信息: " + errorOutput.toString());
                        }

                        // 8. 检查转换后的PCM文件是否存在
                        if (!Files.exists(pcmTargetPath)) {
                            throw new RuntimeException("转换后的PCM文件不存在: " + pcmTargetPath.toString());
                        }

                        // 9. 调用转录服务
                        mediaStreamService.voiceTrans(loginId, pcmTargetPath.toAbsolutePath().toString());

                        return R.ok(pcmTargetPath.toAbsolutePath().toString());
                    });
                })
                .doOnError(error -> {
                    // 发生错误时清理临时文件
                    try {
                        if (Files.exists(mp3TargetPath)) {
                            Files.delete(mp3TargetPath);
                        }
                        if (Files.exists(pcmTargetPath)) {
                            Files.delete(pcmTargetPath);
                        }
                    } catch (Exception e) {
                        System.err.println("清理临时文件失败: " + e.getMessage());
                    }
                });
    }

    @PostMapping("/voiceTrans/{deviceId}")
    public Mono<R<Object>> voiceTrans(
            @PathVariable String deviceId,
            @RequestPart("file") FilePart filePart) {

        // 1. 生成唯一文件名
        String originalFilename = filePart.filename();
        String suffix = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + suffix;
        Path targetPath = Paths.get(UPLOAD_DIR, filename);

        // 2. 查询设备
        Optional<Device> deviceOpt = deviceService.getOneOpt(
                new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));

        if (deviceOpt.isEmpty()) {
            return Mono.just(R.fail("设备ID不存在: " + deviceId));
        }
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();

        // 3. 保存文件并转码，然后调用转录服务
        return filePart.transferTo(targetPath)
                .then(Mono.fromCallable(() -> {
                    // 检查文件是否存在且有内容
                    if (!Files.exists(targetPath) || Files.size(targetPath) <= 44) {
                        throw new IllegalArgumentException("无效的音频文件，文件大小异常");
                    }

                    // 生成转码后的文件路径
                    String originalFileName = targetPath.getFileName().toString();
                    String baseName = originalFileName.contains(".")
                            ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                            : originalFileName;
                    String g711uFilename = baseName + "_g711u.pcm";
                    Path g711uTargetPath = Paths.get(UPLOAD_DIR, g711uFilename);

                    // 4. 使用FFmpeg将原始文件转换为G.711 μ-law
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "ffmpeg",
                            "-i", targetPath.toAbsolutePath().toString(), // 输入文件路径
                            "-f", "mulaw",                    // 输出格式：μ-law编码
                            "-ac", "1",                       // 输出声道数：单声道
                            "-ar", "8000",                    // 输出采样率：8K
                            "-acodec", "pcm_mulaw",           // 明确指定编码器
                            "-y",                             // 覆盖输出文件
                            g711uTargetPath.toAbsolutePath().toString() // 输出文件路径
                    );

                    Process process = processBuilder.start();
                    int exitCode = process.waitFor();

                    if (exitCode != 0) {
                        // 转码失败，读取错误信息
                        StringBuilder errorOutput = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                errorOutput.append(line).append("\n");
                            }
                        }
                        throw new RuntimeException("音频转码失败，FFmpeg退出码: " + exitCode +
                                ", 错误信息: " + errorOutput.toString());
                    }

                    // 5. 检查转码后的文件是否存在
                    if (!Files.exists(g711uTargetPath)) {
                        throw new RuntimeException("转码后的文件不存在: " + g711uTargetPath.toString());
                    }

                    // 6. 获取转码后的完整文件路径
                    String g711uFileFullPath = g711uTargetPath.toAbsolutePath().toString();

                    // 7. 调用你的实时/离线转录服务，传入转码后的文件路径
                    mediaStreamService.voiceTrans(loginId, g711uFileFullPath);

                    return R.ok(g711uFileFullPath);
                }))
                .doOnError(error -> {
                    // 发生错误时清理临时文件
                    try {
                        String originalFileName = targetPath.getFileName().toString();
                        String baseName = originalFileName.contains(".")
                                ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                                : originalFileName;
                        String g711uFilename = baseName + "_g711u.pcm";
                        Path g711uTargetPath = Paths.get(UPLOAD_DIR, g711uFilename);

                        if (Files.exists(targetPath)) {
                            Files.delete(targetPath);
                        }
                        if (Files.exists(g711uTargetPath)) {
                            Files.delete(g711uTargetPath);
                        }
                    } catch (Exception e) {
                        System.err.println("清理临时文件失败: " + e.getMessage());
                    }
                });
    }

//    @PostMapping("/voiceTrans1/{deviceId}")
//    public Mono<R<String>> voiceTrans1(
//            @PathVariable String deviceId,
//            @RequestPart("file") FilePart filePart) {
//
//        // 1. 生成临时文件路径（仍然需要临时落地，因为 FilePart 是流式上传）
//        String filename = UUID.randomUUID() + ".wav";
//        Path tempWavPath = Paths.get(UPLOAD_DIR, filename);
//
//        // 2. 查询设备（保持不变）
//        Optional<Device> deviceOpt = deviceService.getOneOpt(
//                new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
//
//        if (deviceOpt.isEmpty()) {
//            return Mono.just(R.fail("设备ID不存在: " + deviceId));
//        }
//        Device device = deviceOpt.get();
//        Integer loginId = device.getLoginId();
//
//        return filePart.transferTo(tempWavPath)  // 先把前端上传的 wav 完整落地
//                .then(Mono.fromCallable(() -> {
//                    try {
//                        byte[] wavBytes = Files.readAllBytes(tempWavPath);
//
//                        if (wavBytes.length <= 44) {
//                            throw new IllegalArgumentException("无效的WAV文件，太小");
//                        }
//
//                        // 关键：去掉 44 字节 WAV 头，得到纯 PCM
//                        byte[] purePcmBytes = Arrays.copyOfRange(wavBytes, 44, wavBytes.length);
//
//                        // 包装成 InputStream（你的旧代码完全不用改！）
//                        InputStream pcmInputStream = new ByteArrayInputStream(purePcmBytes);
//
//                        // 直接调用你原来的转录服务（原来接收 FileInputStream 的地方完全兼容）
//                        mediaStreamService.voiceTrans(loginId, pcmInputStream);
//                        // 注意：如果你的 voiceTrans 方法内部会 close 这个 stream，这里不需要你手动 close
//                        // ByteArrayInputStream close 是空操作，安全
//
//                        // 用完即删临时 wav 文件
//                        Files.deleteIfExists(tempWavPath);
//
//                        return R.ok("上传成功，内存转纯PCM直传，已处理 " + purePcmBytes.length + " bytes");
//
//                    } catch (Exception e) {
//                        // 出错也要尝试删临时文件
//                        Files.deleteIfExists(tempWavPath);
//                        throw new RuntimeException("处理音频失败: " + e.getMessage(), e);
//                    }
//                }));
//    }
}
