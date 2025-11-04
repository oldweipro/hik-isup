package com.oldwei.isup.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("device")
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 7938085843622114094L;
    /**
     * 设备id
     */
    @TableId
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
     * 是否推流
     */
    private Integer isPush;
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
    /**
     * sessionId
     */
    private Integer previewSessionId;
    /**
     * listenHandle
     */
    private Integer previewListenHandle;
    /**
     * previewHandle
     */
    private Integer previewHandle;
}
