package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_SS_LISTEN_HTTPS_PARAM extends Structure {
    public byte byHttps; //0-不启用HTTPS 1-启用HTTPS
    public byte byVerifyMode; //0-单向认证(暂只支持单向认证)
    public byte byCertificateFileType; //证书类型0-pem, 1-ANS1
    public byte byPrivateKeyFileType; //私钥类型0-pem, 1-ANS1
    public byte[] szUserCertificateFile = new byte[260]; //用户名
    public byte[] szUserPrivateKeyFile = new byte[32]; //密码
    public int dwSSLVersion;//SSL Method版本
    //0 - SSL23, 1 - SSL2, 2 - SSL3, 3 - TLS1.0, 4 - TLS1.1, 5 - TLS1.2
    //SSL23是兼容模式，会协商客户端和服务端使用的最高版本
    public byte[] byRes3 = new byte[360];
}
