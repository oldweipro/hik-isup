package com.oldwei.isup.model.cb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceCapture {
    private String faceTime;
    private TargetAttrs targetAttrs;
    private List<Face> faces;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TargetAttrs {
    private String faceTime;
    private String bkgUrl;
    private String contentID;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Face {
    private Integer faceId;
    private Age age;
    private Gender gender;
    private Glass glass;
    private String contentID;
    @JsonProperty("URL")
    private String url;
    private Integer faceScore;
    private Boolean captureEndMark;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Age {
    private Integer range;
    private Integer value;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Gender {
    private String value;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Glass {
    private String value;
}