package com.oldwei.isup.model.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityStatus {

    @XmlElement(name = "PasswordStatus")
    private String passwordStatus;

}
