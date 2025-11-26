package com.oldwei.isup.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PushDataConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -1410394243078911242L;
    /**
     * 主键
     */
    private Long id;
    //    private Integer pushInterval; // 上传间隔，单位秒
//    private Integer pushMode; // 上传模式，0-定时上传，1-事件触发上传
    private String pushPath; // 上传路径
    private Integer enable; // 是否启用,0-否，1-是
}
