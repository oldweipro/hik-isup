package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预览流处理器 - 每个预览会话独立的回调实例
 * 注意：不使用 @Component/@Service 注解，避免单例模式导致的状态混乱
 */
@Slf4j
public class PreviewStreamHandler implements PREVIEW_DATA_CB {
    // 存储每个预览句柄对应的连接信息
    private static class RtpConnection {
        java.net.Socket rtpSocket;
        java.io.OutputStream rtpOutputStream;
        int count = 0;
        int seqNum = 0;
        int timestamp = 0;
    }
    
    // 使用线程安全的 Map 存储每个句柄对应的连接
    private final Map<Integer, RtpConnection> connectionMap = new ConcurrentHashMap<>();

    @Override
    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        // 通过 iPreviewHandle 获取 sessionID
        Integer sessionID = StreamManager.previewHandSAndSessionIDandMap.get(iPreviewHandle);
        if (sessionID == null) {
            log.error("未找到预览句柄 {} 对应的 sessionID", iPreviewHandle);
            return;
        }
        
        // 通过 sessionID 获取对应的 RTP 端口
        Integer rtpPort = StreamManager.sessionIDAndRtpPortMap.get(sessionID);
        if (rtpPort == null) {
            log.error("未找到 sessionID {} 对应的 RTP 端口", sessionID);
            return;
        }
        
        // 获取或创建该句柄对应的连接
        RtpConnection connection = connectionMap.computeIfAbsent(iPreviewHandle, handle -> {
            RtpConnection conn = new RtpConnection();
            try {
                // 使用动态分配的端口
                conn.rtpSocket = new java.net.Socket("127.0.0.1", rtpPort);
                conn.rtpOutputStream = conn.rtpSocket.getOutputStream();
                log.info("预览句柄: {} ==== RTP Socket创建成功，端口: {}, sessionID: {}", iPreviewHandle, rtpPort, sessionID);
            } catch (Exception e) {
                log.error("创建RTP Socket失败，句柄: {}, 端口: {}", iPreviewHandle, rtpPort, e);
                return null;
            }
            return conn;
        });
        
        if (connection == null || connection.rtpOutputStream == null) {
            log.error("RTP连接不可用，句柄: {}", iPreviewHandle);
            return;
        }
        
        byte[] dataStream = pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen);
        connection.count++;
        if (dataStream != null && dataStream.length > 0) {
//            Integer sessionID = StreamManager.previewHandSAndSessionIDandMap.get(iPreviewHandle);
            if (connection.count > 100) {
                log.info("预览数据回调：预览句柄={}, 数据长度={}", iPreviewHandle, dataStream.length);
                connection.count = 0;
            }

            if (pPreviewCBMsg.byDataType == 2) {
                int dwBufSize = pPreviewCBMsg.dwDataLen;
                Pointer pBuffer = pPreviewCBMsg.pRecvdata;
                byte[] rtpPacket = new byte[1456];
                //ps
                byte pt = 96;
                //以25帧算 以90000采样 需要自己作重置
                connection.timestamp += 3600;
                byte[] timestampBytes = intToBytes(connection.timestamp);
                //自定义同步源
                byte[] ssrc = new byte[]{0x00, 0x00, 0x00, 0x01};
                byte[] rtpHeader = new byte[]{0x00, 0x00, (byte) 0x80, (byte) (pt & 0xFF), 0x00, 0x00, timestampBytes[0], timestampBytes[1], timestampBytes[2], timestampBytes[3], ssrc[0], ssrc[1], ssrc[2], ssrc[3]};
                System.arraycopy(rtpHeader, 0, rtpPacket, 0, rtpHeader.length);
                int datasize = dwBufSize;
                int offset = 0;
                int useDataSize = rtpPacket.length - rtpHeader.length;
                while (datasize > 0) {
                    byte[] seqNumBytes = shortToBytes(++connection.seqNum);
                    try {
                        if (datasize <= useDataSize) {
                            pBuffer.read(offset, rtpPacket, rtpHeader.length, datasize);
                            byte[] dataLengthBytes = shortToBytes(rtpHeader.length + datasize - 2);
                            rtpPacket[0] = dataLengthBytes[0];
                            rtpPacket[1] = dataLengthBytes[1];
                            rtpPacket[3] = (byte) (rtpPacket[3] | 0x80);
                            rtpPacket[4] = seqNumBytes[0];
                            rtpPacket[5] = seqNumBytes[1];
                            connection.rtpOutputStream.write(rtpPacket, 0, rtpHeader.length + datasize);
                            connection.rtpOutputStream.flush();
                            break;
                        } else {
                            pBuffer.read(offset, rtpPacket, rtpHeader.length, useDataSize);
                            byte[] dataLengthBytes = shortToBytes(rtpPacket.length - 2);
                            rtpPacket[0] = dataLengthBytes[0];
                            rtpPacket[1] = dataLengthBytes[1];
                            rtpPacket[4] = seqNumBytes[0];
                            rtpPacket[5] = seqNumBytes[1];
                            connection.rtpOutputStream.write(rtpPacket);
                            connection.rtpOutputStream.flush();
                            offset += useDataSize;
                            datasize -= useDataSize;
                        }
                    } catch (IOException e) {
                        log.error("写入RTP数据失败，句柄: {}", iPreviewHandle, e);
                        // 发生异常时清理该连接
                        closeConnection(iPreviewHandle);
                    }

                }
            }

//            if (streamHandler != null) {
//                // 如果报错，应该关闭预览
//                streamHandler.processStream(dataStream);
//            }
        }
    }

    /**
     * 关闭指定句柄的RTP连接
     * @param iPreviewHandle 预览句柄
     */
    public void closeConnection(int iPreviewHandle) {
        RtpConnection connection = connectionMap.remove(iPreviewHandle);
        if (connection != null) {
            try {
                if (connection.rtpOutputStream != null) {
                    connection.rtpOutputStream.close();
                }
                if (connection.rtpSocket != null) {
                    connection.rtpSocket.close();
                }
                log.info("关闭RTP连接成功，句柄: {}", iPreviewHandle);
            } catch (IOException e) {
                log.error("关闭RTP连接失败，句柄: {}", iPreviewHandle, e);
            }
        }
    }
    
    /**
     * 关闭所有RTP连接
     */
    public void closeAllConnections() {
        connectionMap.keySet().forEach(this::closeConnection);
        log.info("已关闭所有RTP连接");
    }
    
    /**
     * 将int值转换为4字节的字节数组 大端序
     *
     * @param value 要转换的int值
     * @return 包含该int值的4字节表示形式的字节数组
     */
    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    /**
     * 将short值转换为2字节的字节数组 大端序
     *
     * @param value 要转换的int值
     * @return 包含该int值的2字节表示形式的字节数组
     */
    private byte[] shortToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 8),
                (byte) value
        };
    }
}
