package com.oldwei.isup.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Device implements Serializable {

    @Serial
    private static final long serialVersionUID = 7938085843622114094L;
    /**
     * 设备ID（唯一标识）
     */
    private String deviceId;
    /**
     * 设备类型：DVR、NVR、IPCamera等
     */
    private String deviceType;
    /**
     * 是否在线：0-离线，1-在线
     */
    private Integer isOnline;
    /**
     * 登录句柄
     */
    private Integer loginId;
    /**
     * 设备通道列表
     */
    private List<Channel> channels = new ArrayList<>();
    
    /**
     * 获取第一个在线通道ID（兼容旧代码）
     */
    public Integer getChannel() {
        return channels.stream()
                .filter(ch -> ch.getIsOnline() != null && ch.getIsOnline() == 1)
                .map(Channel::getChannelId)
                .findFirst()
                .orElse(channels.isEmpty() ? 1 : channels.get(0).getChannelId());
    }
    
    /**
     * 设备通道信息
     */
    @Data
    public static class Channel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        
        /**
         * 通道号
         */
        private Integer channelId;
        /**
         * 通道是否在线：0-离线，1-在线
         */
        private Integer isOnline;
        
        public Channel() {}
        
        public Channel(Integer channelId, Integer isOnline) {
            this.channelId = channelId;
            this.isOnline = isOnline;
        }
    }
}
