package com.oldwei.isup.model.cb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FaceCaptureEvent extends DeviceEventBase {
    private List<FaceCapture> faceCapture;
    @JsonProperty("GPS")
    private GPSData gps; // 有时 faceCapture 事件也带 GPS
}

