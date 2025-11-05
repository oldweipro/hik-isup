package com.oldwei.isup.model.cb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeviceEventParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static DeviceEventBase parse(String json) throws Exception {
        JsonNode root = mapper.readTree(json);
        String eventType = root.path("eventType").asText();

        return switch (eventType) {
            case "faceCapture" -> mapper.readValue(json, FaceCaptureEvent.class);
            case "GPSUpload" -> mapper.readValue(json, GPSUploadEvent.class);
            default ->
                // 未知类型默认解析为基础类
                    mapper.readValue(json, DeviceEventBase.class);
        };
    }
}

