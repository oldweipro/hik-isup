package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.model.R;
import com.oldwei.isup.model.vo.PlayURL;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/playback")
@RequiredArgsConstructor
public class PlaybackController {

    private final IMediaStreamService mediaStreamService;
    private final IDeviceService deviceService;
    private final HikStreamProperties hikStreamProperties;

    @GetMapping("start")
    public R<PlayURL> playbackByTime(String deviceId, String startTime, String endTime) {
        if (deviceId == null || deviceId.isEmpty()) {
            return R.fail("Device ID cannot be null or empty");
        }
        try {
            // 防止传 null 或未编码的情况
            if (startTime != null) {
                startTime = URLDecoder.decode(startTime, StandardCharsets.UTF_8);
            }
            if (endTime != null) {
                endTime = URLDecoder.decode(endTime, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // 解码异常不应中断逻辑，可记录日志方便排查
            log.warn("时间参数解码失败: startTime={}, endTime={}", startTime, endTime, e);
        }
        String httpFlv = "http://" + hikStreamProperties.getHttp().getIp() + ":" + hikStreamProperties.getHttp().getPort() + "/playback/" + deviceId + ".live.flv";
        log.info("回放httpFlv播放地址：{}", httpFlv);
        // Implementation for playback by time
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            Integer loginId = device.getLoginId();
            Integer sessionId = StreamManager.playbackUserIDandSessionMap.get(loginId);
            if (sessionId == null) {
                mediaStreamService.playbackByTime(deviceId, loginId, device.getChannel(), startTime, endTime);
            } else {
                StreamHandler streamHandler = StreamManager.playbackConcurrentMap.get(sessionId);
                if (streamHandler == null) {
                    mediaStreamService.playbackByTime(deviceId, loginId, device.getChannel(), startTime, endTime);
                }
            }
        } else {
            log.info("Device not found for playback");
            return R.fail("Failed to start playback");
        }
        PlayURL playURL = new PlayURL();
//        playURL.setWsFlv("ws://192.168.2.235:9002/?playKey=" + deviceId);
        playURL.setRtmp("rtmp://" + hikStreamProperties.getRtmp().getIp() + ":" + hikStreamProperties.getRtmp().getPort() + "/playback/" + deviceId);
        playURL.setHttpFlv(httpFlv);
        return R.ok(playURL);
    }

    @GetMapping("stop")
    public R<String> stopPlayBackByTime(String deviceId) {
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            Integer loginId = device.getLoginId();
            mediaStreamService.stopPlayBackByTime(loginId);
        }
        return R.ok();
    }
}
