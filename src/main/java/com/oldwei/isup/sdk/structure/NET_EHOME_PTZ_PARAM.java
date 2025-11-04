package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PTZ_PARAM extends Structure {
    public int dwSize;
    public byte byPTZCmd; //云台命令，参见EN_PTZ_CMD
    public byte byAction; //云台动作，0-开始云台动作，1-停止云台动作
    public byte bySpeed;  //云台速度，0-7，数值越大速度越快
    public byte[] byRes = new byte[29];
}
