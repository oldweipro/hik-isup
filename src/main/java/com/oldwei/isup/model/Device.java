package com.oldwei.isup.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 7938085843622114094L;
    /**
     * 设备id
     */
    private Long id;
    /**
     * 父级ID
     */
    private Long parentId;
    /**
     * 设备ID
     */
    private String deviceId;
    /**
     * 是否在线
     */
    private Integer isOnline;
    /**
     * 登录句柄
     */
    private Integer loginId;
    /**
     * 通道号
     */
    private Integer channel;
}
