package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceStatusXML {

    @XmlElement(name = "Run")
    private int run;

    @XmlElement(name = "CPU")
    private int cpu;

    @XmlElement(name = "Mem")
    private int mem;

    @XmlElement(name = "DSKStatus")
    private String dskStatus;

    @XmlElement(name = "CHStatus")
    private CHStatus chStatus;

    @XmlElement(name = "AlarmInStatus")
    private String alarmInStatus;

    @XmlElement(name = "AlarmOutStatus")
    private String alarmOutStatus;

    @XmlElement(name = "LocalDisplayStatus")
    private int localDisplayStatus;

    @XmlElement(name = "ForbidPreview")
    private int forbidPreview;

    @XmlElement(name = "DefenseStatus")
    private int defenseStatus;

    @XmlElement(name = "ArmDelayTime")
    private int armDelayTime;

    @XmlElement(name = "Remark")
    private String remark;
}
