package com.oldwei.isup.controller;

import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.vo.PlayURL;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.service.DeviceCacheService;
import com.oldwei.isup.service.IMediaStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 流媒体控制接口（预览、回放）
 */
@Slf4j
@RestController
@RequestMapping("/api/devices/{deviceId}")
@RequiredArgsConstructor
public class StreamController {
    
    private final IMediaStreamService mediaStreamService;
    private final DeviceCacheService deviceCacheService;
    private final HikStreamProperties hikStreamProperties;

    /**
     * 开始实时预览
     */
    @PostMapping("/preview")
    public R<PlayURL> startPreview(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return R.fail("设备不存在，无法预览");
        }
        
        Device device = deviceOpt.get();
        Integer sessionId = StreamManager.userIDandSessionMap.get(
                device.getLoginId() * 100 + device.getChannel());
        log.debug("开始预览 - deviceId: {}, sessionId: {}", deviceId, sessionId);
        
        mediaStreamService.preview(device);
        
        PlayURL playURL = new PlayURL();
        playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" 
                + hikStreamProperties.getRtmp().getPort() + "/live/" + device.getDeviceId());
        playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" 
                + hikStreamProperties.getHttp().getPort() + "/live/" + device.getDeviceId() + ".live.flv");
        
        return R.ok(playURL);
    }

    /**
     * 停止实时预览
     */
    @DeleteMapping("/preview")
    public R<Boolean> stopPreview(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        deviceOpt.ifPresent(device -> {
            log.debug("停止预览 - deviceId: {}", deviceId);
            mediaStreamService.stopPreview(device);
        });
        return R.ok(true);
    }

    /**
     * 开始回放
     */
    @PostMapping("/playback")
    public R<PlayURL> startPlayback(
            @PathVariable String deviceId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        if (deviceId == null || deviceId.isEmpty()) {
            return R.fail("设备ID不能为空");
        }
        
        // URL 解码
        try {
            if (startTime != null) {
                startTime = URLDecoder.decode(startTime, StandardCharsets.UTF_8);
            }
            if (endTime != null) {
                endTime = URLDecoder.decode(endTime, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("时间参数解码失败: startTime={}, endTime={}", startTime, endTime, e);
        }
        
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            log.warn("回放失败: 设备不存在, deviceId={}", deviceId);
            return R.fail("回放失败: 设备不存在");
        }
        
        Device device = deviceOpt.get();
        Integer loginId = device.getLoginId();
        Integer sessionId = StreamManager.playbackUserIDandSessionMap.get(loginId);
        
        log.info("开始回放 - deviceId: {}, startTime: {}, endTime: {}", deviceId, startTime, endTime);
        mediaStreamService.playbackByTime(deviceId, loginId, device.getChannel(), startTime, endTime);
        
        PlayURL playURL = new PlayURL();
        playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" 
                + hikStreamProperties.getRtmp().getPort() + "/playback/" + deviceId);
        playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" 
                + hikStreamProperties.getHttp().getPort() + "/playback/" + deviceId + ".live.flv");
        
        return R.ok(playURL);
    }

    /**
     * 停止回放
     */
    @DeleteMapping("/playback")
    public R<String> stopPlayback(@PathVariable String deviceId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            Integer loginId = device.getLoginId();
            log.debug("停止回放 - deviceId: {}", deviceId);
            mediaStreamService.stopPlayBackByTime(loginId);
        }
        return R.ok();
    }
}
