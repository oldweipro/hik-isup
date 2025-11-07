package com.oldwei.isup.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlayURL implements Serializable {
    @Serial
    private static final long serialVersionUID = 3992050009557341065L;
    private String wsFlv;
    private String rtmp;
    private String httpFlv;
}
