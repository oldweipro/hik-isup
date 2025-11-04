package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class SourceInputPortDescriptor {

    @XmlElement(name = "adminProtocol")
    private String adminProtocol;

    @XmlElement(name = "addressingFormatType")
    private String addressingFormatType;

    @XmlElement(name = "ipAddress")
    private String ipAddress;

    @XmlElement(name = "managePortNo")
    private int managePortNo;

    @XmlElement(name = "srcInputPort")
    private int srcInputPort;

    @XmlElement(name = "userName")
    private String userName;

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "firmwareVersion")
    private String firmwareVersion;

    @XmlElement(name = "getSubStream")
    private boolean getSubStream;
}
