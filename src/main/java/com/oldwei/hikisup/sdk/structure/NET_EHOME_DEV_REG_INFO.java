package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_DEV_REG_INFO extends Structure {
    public int dwSize;
    public int dwNetUnitType;
    public byte[] byDeviceID = new byte[256];//设备ID长度
    public byte[] byFirmwareVersion = new byte[24];
    public NET_EHOME_IPADDRESS struDevAdd;
    public int dwDevType;
    public int dwManufacture;
    public byte[] byPassWord = new byte[32];
    public byte[] sDeviceSerial = new byte[12]; //序列号长度
    public byte byReliableTransmission;
    public byte byWebSocketTransmission;
    public byte bySupportRedirect;               //设备支持重定向注册 0-不支持 1-支持
    public byte[] byDevProtocolVersion = new byte[6];         //设备协议版本
    public byte[] bySessionKey = new byte[16];//Ehome5.0设备SessionKey
    public byte byMarketType; //0-无效（未知类型）,1-经销型，2-行业型
    public byte[] byRes = new byte[26];
}