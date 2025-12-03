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
public class AlarmResultEvent extends DeviceEventBase {
    private List<AlarmResult> alarmResult;
    @JsonProperty("GPS")
    private GPSData gps;
}

