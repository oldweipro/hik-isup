package com.oldwei.isup.model.cb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPSData {
    private String divisionEW;
    private Long longitude;
    private String divisionNS;
    private Long latitude;
    private Integer direction;
    private Integer speed;
    private Integer satellites;
    private Integer precision;
    private Integer height;
    private Integer retransFlag;
    private Integer timeZoneIdx;
}

