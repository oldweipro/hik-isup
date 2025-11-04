package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class InputProxyChannelStatus {

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement(name = "id")
    private int id;

    @XmlElement(name = "sourceInputPortDescriptor")
    private SourceInputPortDescriptor sourceInputPortDescriptor;

    @XmlElement(name = "model")
    private String model;

    @XmlElement(name = "online")
    private Boolean online;

    @XmlElementWrapper(name = "streamingProxyChannelIdList")
    @XmlElement(name = "streamingProxyChannelId")
    private List<Integer> streamingProxyChannelIdList;

    @XmlElement(name = "SecurityStatus")
    private SecurityStatus securityStatus;

    @XmlElement(name = "chanDetectResult")
    private String chanDetectResult;

    @XmlElement(name = "bitStreamType")
    private String bitStreamType;

    @XmlElement(name = "rateType")
    private String rateType;

    @XmlElement(name = "codeParam")
    private String codeParam;

}
