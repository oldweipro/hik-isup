package com.oldwei.isup.model.xml;


import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "EventNotificationAlert")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventNotificationAlert {

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement
    private String ipAddress;

    @XmlElement
    private String ipv6Address;

    @XmlElement
    private Integer portNo;

    @XmlElement
    private String macAddress;

    @XmlElement
    private String protocol;

    @XmlElement
    private Integer dynChannelID;

    @XmlElement
    private Integer channelID;

    @XmlElement
    private String dateTime;

    @XmlElement
    private Integer activePostCount;

    @XmlElement
    private String eventType;

    @XmlElement
    private String eventDescription;

    @XmlElement
    private String eventState;

    @XmlElement
    private String channelName;

    @XmlElement
    private String deviceID;

    @XmlElement
    private ANPR ANPR;

    @XmlElement
    private DetectionBackgroundImageResolution detectionBackgroundImageResolution;

    @XmlElement
    private Integer picNum;

    @XmlElement
    private Boolean isDataRetransmission;

    @XmlElement
    private String monitoringSiteID;

    @XmlElement
    private String monitorDescription;
    // ---------- 内部类部分 ----------

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ANPR {
        private Integer country;
        private String licensePlate;
        private Integer line;
        private String direction;
        private Integer confidenceLevel;
        private String plateType;
        private String plateColor;
        private Integer licenseBright;
        private String pilotsafebelt;
        private String vicepilotsafebelt;
        private String pilotsunvisor;
        private String vicepilotsunvisor;
        private String envprosign;
        private String dangmark;
        private String uphone;
        private String pendant;
        private String pdvs;
        private String tissueBox;
        private String frontChild;
        private String label;
        private String decoration;
        private String smoking;
        private String perfumeBox;
        private String helmet;
        private String twoWheelVehicle;
        private String threeWheelVehicle;
        private String playMobilePhone;
        private String plateCharBelieve;

        private IllegalInfo illegalInfo;
        private String vehicleType;
        private String postPicFileName;
        private String featurePicFileName;
        private Integer detectDir;
        private Integer dwIllegalTime;
        private VehicleInfo vehicleInfo;
        private PictureInfoList pictureInfoList;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IllegalInfo {
        private Integer illegalCode;
        private String illegalName;
        private String illegalDescription;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VehicleInfo {
        private Integer index;
        private Integer vehicleType;
        private Integer colorDepth;
        private String color;
        private Integer speed;
        private Integer length;
        private Integer vehicleLogoRecog;
        private Integer vehileSubLogoRecog;
        private Integer vehileModel;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PictureInfoList {
        @XmlElement(name = "pictureInfo")
        private List<PictureInfo> pictureInfo;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PictureInfo {
        private String fileName;
        private String type;
        private Integer dataType;
        private String vehicleHead;
        private String absTime;
        private PlateRect plateRect;
        private VehicelRect vehicelRect;
        private String pictureURL;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PlateRect {
        private Integer X;
        private Integer Y;
        private Integer width;
        private Integer height;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VehicelRect {
        private Integer X;
        private Integer Y;
        private Integer width;
        private Integer height;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DetectionBackgroundImageResolution {
        private Integer height;
        private Integer width;
    }
}
