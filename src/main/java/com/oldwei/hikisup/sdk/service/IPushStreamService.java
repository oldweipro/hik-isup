package com.oldwei.hikisup.sdk.service;

public interface IPushStreamService {
    /**
     * 推流
     * @param pushStreamUrl
     * @param data
     * @param size
     */
    void pushMediaStream(String pushStreamUrl, byte[] data, int size);
}
