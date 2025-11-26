package com.oldwei.isup.controller;

import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.tts.DataItem;
import com.oldwei.isup.model.tts.TtsRequest;
import com.oldwei.isup.service.DeviceCacheService;
import com.oldwei.isup.service.IMediaStreamService;
import com.oldwei.isup.util.WebFluxHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 语音对讲控制接口
 */
@Slf4j
@RestController
@RequestMapping("/api/devices/{deviceId}/voice")
@RequiredArgsConstructor
public class VoiceController {
    
    private final IMediaStreamService mediaStreamService;
    private final DeviceCacheService deviceCacheService;
    
    private static final String UPLOAD_DIR = "upload/audio/";

    /**
     * TTS语音播报
     */
    @PostMapping("/tts")
    public Mono<R<Object>> sendTtsVoice(
            @PathVariable String deviceId,
            @RequestBody DataItem dataItem) {

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString();
        String mp3Filename = "tts_" + filename + ".mp3";
        String pcmFilename = "tts_" + filename + "_8k.pcm";
        Path mp3TargetPath = Paths.get(UPLOAD_DIR, mp3Filename);
        Path pcmTargetPath = Paths.get(UPLOAD_DIR, pcmFilename);

        // 查询设备
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return Mono.just(R.fail("设备ID不存在: " + deviceId));
        }
        
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();

        // 构建TTS请求数据
        List<DataItem> dataList = Arrays.asList(
                new DataItem("路人甲", dataItem.getText(), "zh-CN-YunjianNeural", "0%")
        );
        TtsRequest request = new TtsRequest();
        request.setData(dataList);

        String url = "http://localhost:3000/api/v1/tts/generateJson";

        // 异步发送TTS请求并处理文件转换和转录
        return WebFluxHttpUtil.postAsync(url, request, byte[].class)
                .flatMap(audioData -> {
                    if (audioData == null || audioData.length == 0) {
                        return Mono.error(new RuntimeException("TTS音频数据为空"));
                    }

                    return Mono.fromCallable(() -> {
                        // 保存MP3文件
                        try (FileOutputStream fos = new FileOutputStream(mp3TargetPath.toFile())) {
                            fos.write(audioData);
                            fos.flush();
                        } catch (IOException e) {
                            throw new RuntimeException("保存MP3文件失败: " + e.getMessage());
                        }

                        // 检查MP3文件是否存在且有内容
                        if (!Files.exists(mp3TargetPath) || Files.size(mp3TargetPath) == 0) {
                            throw new IllegalArgumentException("生成的音频文件为空或不存在");
                        }

                        // 使用FFmpeg将MP3转换为8K μ-law PCM
                        ProcessBuilder processBuilder = new ProcessBuilder(
                                "ffmpeg",
                                "-i", mp3TargetPath.toAbsolutePath().toString(),
                                "-f", "mulaw",
                                "-ac", "1",
                                "-ar", "8000",
                                "-acodec", "pcm_mulaw",
                                "-y",
                                pcmTargetPath.toAbsolutePath().toString()
                        );

                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();

                        if (exitCode != 0) {
                            StringBuilder errorOutput = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getErrorStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    errorOutput.append(line).append("\n");
                                }
                            }
                            throw new RuntimeException("音频转码失败,FFmpeg退出码: " + exitCode +
                                    ", 错误信息: " + errorOutput.toString());
                        }

                        // 检查转换后的PCM文件是否存在
                        if (!Files.exists(pcmTargetPath)) {
                            throw new RuntimeException("转换后的PCM文件不存在: " + pcmTargetPath.toString());
                        }

                        // 调用转录服务
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
                        log.error("清理临时文件失败: {}", e.getMessage());
                    }
                });
    }

    /**
     * 上传音频对讲
     */
    @PostMapping("/upload")
    public Mono<R<Object>> uploadVoice(
            @PathVariable String deviceId,
            @RequestPart("file") FilePart filePart) {

        // 生成唯一文件名
        String originalFilename = filePart.filename();
        String suffix = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + suffix;
        Path targetPath = Paths.get(UPLOAD_DIR, filename);

        // 查询设备
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return Mono.just(R.fail("设备ID不存在: " + deviceId));
        }
        
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();

        // 保存文件并转码,然后调用转录服务
        return filePart.transferTo(targetPath)
                .then(Mono.fromCallable(() -> {
                    // 检查文件是否存在且有内容
                    if (!Files.exists(targetPath) || Files.size(targetPath) <= 44) {
                        throw new IllegalArgumentException("无效的音频文件,文件大小异常");
                    }

                    // 生成转码后的文件路径
                    String originalFileName = targetPath.getFileName().toString();
                    String baseName = originalFileName.contains(".")
                            ? originalFileName.substring(0, originalFileName.lastIndexOf("."))
                            : originalFileName;
                    String g711uFilename = baseName + "_g711u.pcm";
                    Path g711uTargetPath = Paths.get(UPLOAD_DIR, g711uFilename);

                    // 使用FFmpeg将原始文件转换为G.711 μ-law
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "ffmpeg",
                            "-i", targetPath.toAbsolutePath().toString(),
                            "-f", "mulaw",
                            "-ac", "1",
                            "-ar", "8000",
                            "-acodec", "pcm_mulaw",
                            "-y",
                            g711uTargetPath.toAbsolutePath().toString()
                    );

                    Process process = processBuilder.start();
                    int exitCode = process.waitFor();

                    if (exitCode != 0) {
                        StringBuilder errorOutput = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                errorOutput.append(line).append("\n");
                            }
                        }
                        throw new RuntimeException("音频转码失败,FFmpeg退出码: " + exitCode +
                                ", 错误信息: " + errorOutput.toString());
                    }

                    // 检查转码后的文件是否存在
                    if (!Files.exists(g711uTargetPath)) {
                        throw new RuntimeException("转码后的文件不存在: " + g711uTargetPath.toString());
                    }

                    // 获取转码后的完整文件路径
                    String g711uFileFullPath = g711uTargetPath.toAbsolutePath().toString();

                    // 调用转录服务,传入转码后的文件路径
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
                        log.error("清理临时文件失败: {}", e.getMessage());
                    }
                });
    }
}
