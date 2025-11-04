package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "PPVSPMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class PpvspMessage {

    @XmlElement(name = "Version")
    private String version;

    @XmlElement(name = "Sequence")
    private int sequence;

    @XmlElement(name = "CommandType")
    private String commandType;

    @XmlElement(name = "Method")
    private String method;

    @XmlElement(name = "WhichCommand")
    private String whichCommand;

    @XmlElement(name = "Status")
    private int status;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Params")
    private Params params;

}
