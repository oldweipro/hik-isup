package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@Data
@XmlRootElement(name = "DeviceInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceInfo {

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement(name = "deviceName")
    private String deviceName;


    @XmlElement(name = "deviceID")
    private String deviceID;

    @XmlElement(name = "deviceDescription")
    private String deviceDescription;

    @XmlElement(name = "deviceLocation")
    private String deviceLocation;

    @XmlElement(name = "systemContact")
    private String systemContact;

    @XmlElement(name = "model")
    private String model;

    @XmlElement(name = "serialNumber")
    private String serialNumber;

    @XmlElement(name = "macAddress")
    private String macAddress;

    @XmlElement(name = "firmwareVersion")
    private String firmwareVersion;

    @XmlElement(name = "firmwareReleasedDate")
    private String firmwareReleasedDate;

    @XmlElement(name = "encoderVersion")
    private String encoderVersion;

    @XmlElement(name = "encoderReleasedDate")
    private String encoderReleasedDate;

    @XmlElement(name = "bootVersion")
    private String bootVersion;

    @XmlElement(name = "bootReleasedDate")
    private String bootReleasedDate;

    @XmlElement(name = "hardwareVersion")
    private String hardwareVersion;

    @XmlElement(name = "deviceType")
    private String deviceType;

    @XmlElement(name = "telecontrolID")
    private String telecontrolID;

    @XmlElement(name = "supportBeep")
    private Boolean supportBeep;

    @XmlElement(name = "supportVideoLoss")
    private Boolean supportVideoLoss;

    @XmlElement(name = "firmwareVersionInfo")
    private String firmwareVersionInfo;

    @XmlElement(name = "manufacturer")
    private String manufacturer;

    @XmlElement(name = "subSerialNumber")
    private String subSerialNumber;

    @XmlElement(name = "OEMCode")
    private Integer oemCode;
}
