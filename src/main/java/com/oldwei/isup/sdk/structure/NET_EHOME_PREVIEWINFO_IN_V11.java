package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PREVIEWINFO_IN_V11 extends Structure {
    public int iChannel;
    public int dwStreamType;
    public int dwLinkMode;
    public NET_EHOME_IPADDRESS struStreamSever;
    public byte byDelayPreview;
    public byte[] byRes = new byte[31];
}
