package com.oldwei.isup.service;

import com.oldwei.isup.model.Device;

public interface IMediaStreamService {
    /**
     * 预览视频
     *
     * @param device
     */
    void preview(Device device);

    void stopPreview(Device device);

    void playbackByTime(Integer deviceId, Integer channelId, String startTime, String endTime);

    void stopPlayBackByTime(Integer device);
}
