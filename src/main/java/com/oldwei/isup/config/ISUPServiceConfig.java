package com.oldwei.isup.config;

import com.oldwei.isup.sdk.service.*;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.util.OsSelect;
import com.sun.jna.Native;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ISUPServiceConfig {

    @Bean
    public IHikISUPAlarm hikISUPAlarm() {
        IHikISUPAlarm hikISUPAlarm = null;
        synchronized (IHikISUPAlarm.class) {
            String strDllPath = "";
            try {
                //System.setProperty("jna.debug_load", "true");
                if (OsSelect.isWindows())
                    //win系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPAlarm.dll";
                else if (OsSelect.isLinux())
                    //Linux系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPAlarm.so";
                hikISUPAlarm = (IHikISUPAlarm) Native.loadLibrary(strDllPath, IHikISUPAlarm.class);
            } catch (Exception ex) {
                log.error("loadLibrary: {} Error: {}", strDllPath, ex.getMessage());
                return hikISUPAlarm;
            }
        }
        if (OsSelect.isWindows()) {
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hikISUPAlarm.NET_EALARM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hikISUPAlarm.NET_EALARM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());

            //报警服务初始化
            boolean bRet = hikISUPAlarm.NET_EALARM_Init();
            if (!bRet) {
                System.out.println("NET_EALARM_Init failed!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hikISUPAlarm.NET_EALARM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());
        } else if (OsSelect.isLinux()) {
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hikISUPAlarm.NET_EALARM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hikISUPAlarm.NET_EALARM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //报警服务初始化
            boolean bRet = hikISUPAlarm.NET_EALARM_Init();
            if (!bRet) {
                System.out.println("NET_EALARM_Init failed!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hikISUPAlarm.NET_EALARM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());
        }
        return hikISUPAlarm;

    }

    @Bean
    public IHikISUPStorage hikISUPStorage() {
        IHikISUPStorage hikISUPStorage = null;
        synchronized (IHikISUPStorage.class) {
            String strDllPath = "";
            try {
                //System.setProperty("jna.debug_load", "true");
                if (OsSelect.isWindows())
                    //win系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPSS.dll";
                else if (OsSelect.isLinux())
                    //Linux系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPSS.so";
                hikISUPStorage = (IHikISUPStorage) Native.loadLibrary(strDllPath, IHikISUPStorage.class);
            } catch (Exception ex) {
                System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                return hikISUPStorage;
            }
        }
        if (OsSelect.isWindows()) {
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            int iPathCryptoLen = strPathCrypto.getBytes().length;
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(iPathCryptoLen + 1);
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, iPathCryptoLen);
            ptrByteArrayCrypto.write();
            System.out.println(new String(ptrByteArrayCrypto.byValue));
            hikISUPStorage.NET_ESS_SetSDKInitCfg(4, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            int iPathSslLen = strPathSsl.getBytes().length;
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(iPathSslLen + 1);
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, iPathSslLen);
            ptrByteArraySsl.write();
            System.out.println(new String(ptrByteArraySsl.byValue));
            hikISUPStorage.NET_ESS_SetSDKInitCfg(5, ptrByteArraySsl.getPointer());

            //设置sqlite3库的路径
            String strPathSqlite = System.getProperty("user.dir") + "\\sdk\\windows\\sqlite3.dll";
            int iPathSqliteLen = strPathSqlite.getBytes().length;
            BYTE_ARRAY ptrByteArraySqlite = new BYTE_ARRAY(iPathSqliteLen + 1);
            System.arraycopy(strPathSqlite.getBytes(), 0, ptrByteArraySqlite.byValue, 0, iPathSqliteLen);
            ptrByteArraySqlite.write();
            System.out.println(new String(ptrByteArraySqlite.byValue));
            hikISUPStorage.NET_ESS_SetSDKInitCfg(6, ptrByteArraySqlite.getPointer());
            //SDK初始化
            boolean sinit = hikISUPStorage.NET_ESS_Init();
            if (!sinit) {
                System.out.println("NET_ESS_Init失败，错误码：" + hikISUPStorage.NET_ESS_GetLastError());
            }
        } else if (OsSelect.isLinux()) {
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.out.println(strPathCrypto);
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hikISUPStorage.NET_ESS_SetSDKInitCfg(4, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.out.println(strPathSsl);
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hikISUPStorage.NET_ESS_SetSDKInitCfg(5, ptrByteArraySsl.getPointer());

            //设置splite3.so所在路径
            BYTE_ARRAY ptrByteArraysplite = new BYTE_ARRAY(256);
            String strPathsplite = System.getProperty("user.dir") + "/sdk/linux/libsqlite3.so";    //Linux版本是libsqlite3.so库文件的路径
            System.out.println(strPathsplite);
            System.arraycopy(strPathsplite.getBytes(), 0, ptrByteArraysplite.byValue, 0, strPathsplite.length());
            ptrByteArraysplite.write();
            hikISUPStorage.NET_ESS_SetSDKInitCfg(6, ptrByteArraysplite.getPointer());
            //SDK初始化
            boolean sinit = hikISUPStorage.NET_ESS_Init();
            if (!sinit) {
                System.out.println("NET_ESS_Init失败，错误码：" + hikISUPStorage.NET_ESS_GetLastError());
            }
        }
        //启用SDK写日志
        boolean logToFile = hikISUPStorage.NET_ESS_SetLogToFile(3, System.getProperty("user.dir") + "/container/EHomeSDKLog", false);
        return hikISUPStorage;
    }

    /**
     * 在这里实例化sdk连接
     *
     * @return
     */
    @Bean
    public HCISUPCMS ihcisupcms() {
        log.info("*************** 初始化 HIK ISUP CMS SDK ***************");
        HCISUPCMS hcisupcms = null;
        synchronized (HCISUPCMS.class) {
            String strDllPath = "";
            try {
                if (OsSelect.isWindows())
                    //win系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPCMS.dll";
                else if (OsSelect.isLinux())
                    //Linux系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPCMS.so";
                hcisupcms = (HCISUPCMS) Native.loadLibrary(strDllPath, HCISUPCMS.class);
            } catch (Exception ex) {
                System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                return hcisupcms;
            }
        }
        // CMS_Init
        if (OsSelect.isWindows()) {
            log.info("*************** 初始化 Windows HIK ISUP CMS SDK ***************");
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcisupcms.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcisupcms.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = hcisupcms.NET_ECMS_Init();
            if (binit) {
                log.info("Windows 初始化成功");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcisupcms.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        } else if (OsSelect.isLinux()) {
            log.info("*************** 初始化 Linux HIK ISUP CMS SDK ***************");
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcisupcms.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcisupcms.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = hcisupcms.NET_ECMS_Init();
            if (binit) {
                log.info("Linux 初始化成功");
            } else {
                log.error("Linux 初始化失败，错误码：{}", hcisupcms.NET_ECMS_GetLastError());
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcisupcms.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        }
        log.info("*************** 初始化 HIK ISUP CMS SDK 完毕 ***************");
        return hcisupcms;
    }

    @Bean
    public IHikISUPStream hikISUPStream() {
        log.info("*************** 初始化 HIK ISUP STREAM SDK ***************");
        IHikISUPStream hikISUPStream = null;
        synchronized (IHikISUPStream.class) {
            String strDllPath = "";
            try {
                if (OsSelect.isWindows())
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPStream.dll";
                else if (OsSelect.isLinux())
                    strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPStream.so";
                hikISUPStream = (IHikISUPStream) Native.loadLibrary(strDllPath, IHikISUPStream.class);
            } catch (Exception ex) {
                log.error("loadLibrary: {}, Error: {}", strDllPath, ex.getMessage());
                return hikISUPStream;
            }
        }
        if (OsSelect.isWindows()) {
            log.info("*************** 初始化 Windows HIK ISUP STREAM SDK ***************");
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                log.error("NET_ESTREAM_SetSDKInitCfg 0 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                log.error("NET_ESTREAM_SetSDKInitCfg 1 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
            //流媒体初始化
            hikISUPStream.NET_ESTREAM_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                log.error("NET_ESTREAM_SetSDKLocalCfg 5 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
        } else if (OsSelect.isLinux()) {
            log.info("*************** 初始化 Linux HIK ISUP STREAM SDK ***************");
            //设置libcrypto.so所在路径
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                log.error("NET_ESTREAM_SetSDKInitCfg 0 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                log.error("NET_ESTREAM_SetSDKInitCfg 1 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
            hikISUPStream.NET_ESTREAM_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hikISUPStream.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                log.error("NET_ESTREAM_SetSDKLocalCfg 5 failed, error: {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
        }
        hikISUPStream.NET_ESTREAM_SetLogToFile(3, "./container/EHomeSDKLog", false);
        log.info("*************** 初始化 HIK ISUP STREAM SDK 完毕 ***************");
        return hikISUPStream;
    }

    @Bean
    public IHikNet hikNet() {
        IHikNet hikNet = null;
        synchronized (IHikISUPStream.class) {
            String strDllPath = "";
            try {
                if (OsSelect.isWindows())
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\netsdk\\HCNetSDK.dll";
                else if (OsSelect.isLinux())
                    strDllPath = System.getProperty("user.dir") + "/sdk/linux/netsdk/libhcnetsdk.so";
                hikNet = (IHikNet) Native.loadLibrary(strDllPath, IHikNet.class);
            } catch (Exception ex) {
                log.error("loadLibrary: {}, Error: {}", strDllPath, ex.getMessage());
                return hikNet;
            }
        }
        hikNet.NET_DVR_Init();
        return hikNet;
    }

}
