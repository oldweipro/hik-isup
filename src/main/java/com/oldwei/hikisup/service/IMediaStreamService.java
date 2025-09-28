package com.oldwei.hikisup.service;

public interface IMediaStreamService {
    /**
     * 预览视频
     *
     * @param lLoginID
     * @param lChannel
     * @param deviceId
     * @param randomPort
     */
    void preview(int lLoginID, int lChannel, String deviceId, String randomPort);

    void stopPreview(String deviceId, int lLoginID);
}
