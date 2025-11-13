package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PLAYBACKBYTIME extends Structure {
    public NET_EHOME_TIME struStartTime;  // 按时间回放的开始时间
    public NET_EHOME_TIME struStopTime;   // 按时间回放的结束时间
    public byte byLocalOrUTC;           //0-设备本地时间，即设备OSD时间  1-UTC时间
    public byte byDuplicateSegment;     //byLocalOrUTC为1时无效 0-重复时间段的前段 1-重复时间段后端
}
