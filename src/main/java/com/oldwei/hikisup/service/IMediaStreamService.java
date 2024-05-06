package com.oldwei.hikisup.service;

public interface IMediaStreamService {

    void openStream(int lLoginID, int lChannel, String deviceId);
    void openStreamCV(int lLoginID, int lChannel, String deviceId, String liveAddress);

    void deleteStreamCV(int lLoginID);

    void saveStream(int lLoginID, int lChannel, String deviceId);
}
