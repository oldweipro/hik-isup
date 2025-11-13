package com.oldwei.isup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/playback")
@RequiredArgsConstructor
public class PlaybackController {

    private final IMediaStreamService mediaStreamService;
    private final IDeviceService deviceService;

    @GetMapping("start")
    public void playbackByTime(String deviceId, String startTime, String endTime) {
        // Implementation for playback by time
        Optional<Device> deviceOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getDeviceId, deviceId));
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            Integer loginId = device.getLoginId();
            mediaStreamService.playbackByTime(loginId, device.getChannel(), startTime, endTime);
            while (!StreamManager.stopPlayBackFlag) {

                try {
                    Thread.sleep(1000); // 每秒检查一次
                } catch (InterruptedException e) {
                    // 如果线程被中断，通常需要清除中断状态
                    Thread.currentThread().interrupt();
                    System.out.println("Playback was interrupted");
                    break; // 当前线程被中断时退出循环
                }
            }
            mediaStreamService.stopPlayBackByTime(loginId);
        } else {
            log.info("Device not found for playback");
        }
    }

//    public void stopPlayBackByTime() {
//        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StopPlayBack(lLoginID, backSessionID)) {
//            log.info("NET_ECMS_StopPlayBack failed,err = " + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
//            return;
//        }
//        log.info("CMS发送回放停止请求");
//        if (!hCEhomeStream.NET_ESTREAM_StopPlayBack(m_lPlayBackLinkHandle)) {
//            log.info("NET_ESTREAM_StopPlayBack failed,err = " + hCEhomeStream.NET_ESTREAM_GetLastError());
//            return;
//        }
//        log.info("停止回放Stream服务的实时流转发");
//    }
}
