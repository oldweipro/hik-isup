package com.oldwei.isup.model.cb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GPSUploadEvent extends DeviceEventBase {

    @JsonProperty("GPS")
    private GPSData gps;
}

