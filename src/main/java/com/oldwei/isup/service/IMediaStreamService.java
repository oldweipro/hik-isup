package com.oldwei.isup.service;

import com.oldwei.isup.model.Device;

import java.io.InputStream;

public interface IMediaStreamService {
    /**
     * 预览视频
     *
     * @param device
     */
    void preview(Device device);

    void stopPreview(Device device);

    void playbackByTime(String deviceId, Integer loginId, Integer channelId, String startTime, String endTime);

    void stopPlayBackByTime(Integer device);

    void voiceTrans(Integer loginId, InputStream fileFullPath);
}
