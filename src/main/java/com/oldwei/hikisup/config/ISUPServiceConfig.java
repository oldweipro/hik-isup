package com.oldwei.hikisup.config;

import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.structure.BYTE_ARRAY;
import com.oldwei.hikisup.util.OsSelect;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.sun.jna.Native;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ISUPServiceConfig {

    @Bean
    public PropertiesUtil propertiesUtil() {
        String configPath = "./config.properties";
        PropertiesUtil propertiesUtil  = null;
        try {
            propertiesUtil = new PropertiesUtil(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("+++++++++++++ 初始化 配置文件 +++++++++++++++++");
        return propertiesUtil;
    }

    /**
     * 在这里实例化sdk连接
     * @return
     */
    @Bean
    public IHCISUPCMS ihcisupcms() {
        IHCISUPCMS ihcisupcms = null;
        synchronized (IHCISUPCMS.class) {
            String strDllPath = "";
            try {
                if (OsSelect.isWindows())
                    //win系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPCMS.dll";
                else if (OsSelect.isLinux())
                    //Linux系统加载库路径(路径不要带中文)
                    strDllPath = System.getProperty("user.dir")+"/sdk/linux/libHCISUPCMS.so";
                ihcisupcms = (IHCISUPCMS) Native.loadLibrary(strDllPath, IHCISUPCMS.class);
            } catch (Exception ex) {
                System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
            }
        }
        assert ihcisupcms != null;
        // cMS_Init
        if (OsSelect.isWindows()) {
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            ihcisupcms.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            ihcisupcms.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = ihcisupcms.NET_ECMS_Init();
            if (binit) {
                System.out.println("初始化成功");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            ihcisupcms.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        } else if (OsSelect.isLinux()) {
            BYTE_ARRAY ptrByteArrayCrypto = new BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            ihcisupcms.NET_ECMS_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            BYTE_ARRAY ptrByteArraySsl = new BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            ihcisupcms.NET_ECMS_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //注册服务初始化
            boolean binit = ihcisupcms.NET_ECMS_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            BYTE_ARRAY ptrByteArrayCom = new BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            ihcisupcms.NET_ECMS_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());

        }
        log.info("+++++++++++++ 初始化 CMS +++++++++++++++++");
        return ihcisupcms;
    }

    @Bean
    public IHikISUPStream hikISUPStream() {
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
            }
        }
        assert hikISUPStream != null;
        if (OsSelect.isWindows()) {
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
        log.info("+++++++++++++ 初始化 流媒体 +++++++++++++++++");
        return hikISUPStream;
    }
}
