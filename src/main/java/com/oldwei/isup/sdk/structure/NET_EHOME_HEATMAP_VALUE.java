package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_HEATMAP_VALUE extends Structure {
    public int dwMaxHeatMapValue;    //最大热度值
    public int dwMinHeatMapValue;    //最小热度值
    public int dwTimeHeatMapValue;   //平均热度值
}
