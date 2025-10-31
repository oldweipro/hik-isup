package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

/**
 * 车载客流统计的GPS定位信息
 */
public class NET_EHOME_MPGPS extends Structure {
    public int iLongitude;
    public int iLatitude;
    public int iSpeed;
    public int iDirection;
}
