package com.oldwei.isup.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UploadData implements Serializable {
    @Serial
    private static final long serialVersionUID = 7632941126425093560L;
    private String dataType;
    private String data;
}
