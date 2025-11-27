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
    public R<PlayURL> startPreview(
            @PathVariable String deviceId,
            @RequestParam(required = false, defaultValue = "1") Integer channelId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        if (deviceOpt.isEmpty()) {
            return R.fail("设备不存在，无法预览");
        }
        
        Device device = deviceOpt.get();
        // 查找指定的通道
        Device.Channel channel = device.getChannels().stream()
                .filter(ch -> ch.getChannelId().equals(channelId))
                .findFirst()
                .orElse(null);
        
        if (channel == null) {
            return R.fail("通道不存在: " + channelId);
        }
        
        String streamKey = deviceId + "_" + channelId;
        Integer sessionId = StreamManager.userIDandSessionMap.get(
                device.getLoginId() * 100 + channelId);
        log.debug("开始预览 - deviceId: {}, channelId: {}, sessionId: {}", deviceId, channelId, sessionId);
        
        // 防重复：如果该通道已有RTP服务，直接返回播放地址
        if (StreamManager.deviceRTP.containsKey(streamKey)) {
            log.info("通道已在预览中，忽略重复开启: {}", streamKey);
            PlayURL playURL = new PlayURL();
            playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" 
                    + hikStreamProperties.getRtmp().getPort() + "/live/" + streamKey);
            playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" 
                    + hikStreamProperties.getHttp().getPort() + "/live/" + streamKey + ".live.flv");
            return R.ok(playURL);
        }
        
        mediaStreamService.preview(device, channelId);
        
        PlayURL playURL = new PlayURL();
        playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" 
                + hikStreamProperties.getRtmp().getPort() + "/live/" + streamKey);
        playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" 
                + hikStreamProperties.getHttp().getPort() + "/live/" + streamKey + ".live.flv");
        
        return R.ok(playURL);
    }

    /**
     * 停止实时预览
     */
    @DeleteMapping("/preview")
    public R<Boolean> stopPreview(
            @PathVariable String deviceId,
            @RequestParam(required = false, defaultValue = "1") Integer channelId) {
        Optional<Device> deviceOpt = deviceCacheService.getByDeviceId(deviceId);
        deviceOpt.ifPresent(device -> {
            log.debug("停止预览 - deviceId: {}, channelId: {}", deviceId, channelId);
            mediaStreamService.stopPreview(device, channelId);
        });
        return R.ok(true);
    }

    /**
     * 开始回放
     */
    @PostMapping("/playback")
    public R<PlayURL> startPlayback(
            @PathVariable String deviceId,
            @RequestParam(required = false, defaultValue = "1") Integer channelId,
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
        
        String streamKey = deviceId + "_" + channelId;
        log.info("开始回放 - deviceId: {}, channelId: {}, startTime: {}, endTime: {}", 
                 deviceId, channelId, startTime, endTime);
        mediaStreamService.playbackByTime(streamKey, loginId, channelId, startTime, endTime);
        
        PlayURL playURL = new PlayURL();
        playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" 
                + hikStreamProperties.getRtmp().getPort() + "/playback/" + streamKey);
        playURL.setHttpFlv("http://" + hikStreamProperties.getHttp().getIp() + ":" 
                + hikStreamProperties.getHttp().getPort() + "/playback/" + streamKey + ".live.flv");
        
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
