package com.oldwei.isup.sdk.structure;

import com.sun.jna.Union;

public class NET_EHOME_PLAYBACKMODE extends Union {
    public byte[] byLen = new byte[512];
    public NET_EHOME_PLAYBACKBYNAME struPlayBackbyName;
    public NET_EHOME_PLAYBACKBYTIME struPlayBackbyTime;
}
