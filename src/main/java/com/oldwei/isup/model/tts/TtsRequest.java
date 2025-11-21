package com.oldwei.isup.model.tts;

import lombok.Data;

import java.util.List;

@Data
public class TtsRequest {
    private List<DataItem> data;
}
