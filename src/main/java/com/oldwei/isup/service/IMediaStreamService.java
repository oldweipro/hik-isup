package com.oldwei.isup.service;

import com.oldwei.isup.model.Device;

public interface IMediaStreamService {
    /**
     * 预览视频
     *
     * @param device 设备对象
     * @param channelId 通道号
     */
    void preview(Device device, Integer channelId);

    void stopPreview(Device device, Integer channelId);

    void playbackByTime(String streamKey, Integer loginId, Integer channelId, String startTime, String endTime);

    void stopPlayBackByTime(Integer loginId);

    void voiceTrans(Integer loginId, String fileFullPath);
}
