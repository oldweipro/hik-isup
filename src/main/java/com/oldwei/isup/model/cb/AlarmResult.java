package com.oldwei.isup.model.cb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AlarmResult {
    private Integer traceIdx;
    private AlarmTargetAttrs targetAttrs;
    private List<AlarmFace> faces;
}

@Data
class AlarmTargetAttrs {
    private String faceTime;
    private String bkgUrl;
    private String contentID;
}

@Data
class AlarmFace {
    private Integer faceId;
    private String contentID;
    @JsonProperty("URL")
    private String URL;
    private List<AlarmIdentify> identify;
}

@Data
class AlarmIdentify {
    private Double maxsimilarity;
    private Integer similarityRange;
    private List<Candidate> candidate;
}

@Data
class Candidate {
    private Integer alarmId;
    private List<HumanData> human_data;
    private Double similarity;
    private ReserveField reserve_field;
    private String customHumanID;
    private String customFaceLibID;
}

@Data
class HumanData {
    private Double similarity;
    private String contentID;
    private String bkg_picurl;
}

@Data
class ReserveField {
    private String name;
    private String gender;
    private String certificateType;
    private String bornTime;
    private String certificateNumber;
}
