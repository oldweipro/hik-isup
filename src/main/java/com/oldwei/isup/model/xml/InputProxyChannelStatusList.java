package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "InputProxyChannelStatusList")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputProxyChannelStatusList {

    @XmlAttribute
    private String version;

    @XmlElement(name = "InputProxyChannelStatus")
    private List<InputProxyChannelStatus> channels;

}
