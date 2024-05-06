/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oldwei.hikisup.sdk.SdkService.SsService;

import com.oldwei.hikisup.util.CommonMethod;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public interface HCISUPSS extends Library {

	/***宏定义***/
	//常量

	public static final int MAX_URL_LEN_SS = 4096; //图片服务器回调URL长度
	public static final int MAX_KMS_USER_LEN = 512; //KMS用户名最大长度
	public static final int MAX_KMS_PWD_LEN = 512; //KMS密码最大长度
	public static final int MAX_CLOUD_AK_SK_LEN = 64; //EHome5.0存储协议AK SK最大长度
	public static final int MAX_PATH = 260; //设备ID长度
	public static final int NET_EHOME_SERIAL_LEN = 12; //设备序列号长度

	//NET_EHOME_SS_MSG_TYPE
	public static final int NET_EHOME_SS_MSG_TOMCAT = 1;//Tomcat回调函数
	public static final int NET_EHOME_SS_MSG_KMS_USER_PWD = 2;//KMS用户名密码校验
	public static final int NET_EHOME_SS_MSG_CLOUD_AK = 3;//EHome5.0存储协议AK回调

	//NET_EHOME_SS_CLIENT_TYPE
	public static final int NET_EHOME_SS_CLIENT_TYPE_TOMCAT = 1; //Tomcat图片上传客户端
	public static final int NET_EHOME_SS_CLIENT_TYPE_VRB = 2;//VRB图片上传客户端
	public static final int NET_EHOME_SS_CLIENT_TYPE_KMS = 3;//KMS图片上传客户端
	public static final int NET_EHOME_SS_CLIENT_TYPE_CLOUD = 4;//EHome5.0存储协议客户端

	//NET_EHOME_SS_INIT_CFG_TYPE
	public static final int NET_EHOME_SS_INIT_CFG_SDK_PATH = 1;//设置SS组件加载路径（仅Linux版本支持）
	public static final int NET_EHOME_SS_INIT_CFG_CLOUD_TIME_DIFF = 2;//设置运存储的请求时间差值,默认15分钟,最小5分钟,最大60分钟

	public enum NET_EHOME_SS_MSG_TYPE {
		NET_EHOME_SS_MSG_TOMCAT, //Tomcat回调函数
		NET_EHOME_SS_MSG_KMS_USER_PWD, //KMS用户名密码校验
		NET_EHOME_SS_MSG_CLOUD_AK //EHome5.0存储协议AK回调
	}

	public enum NET_EHOME_SS_CLIENT_TYPE {
		NET_EHOME_SS_CLIENT_TYPE_TOMCAT, //Tomcat图片上传客户端
		NET_EHOME_SS_CLIENT_TYPE_VRB, //VRB图片上传客户端
		NET_EHOME_SS_CLIENT_TYPE_KMS, //KMS图片上传客户端
		NET_EHOME_SS_CLIENT_TYPE_CLOUD //EHome5.0存储协议客户端
	}

	public enum NET_EHOME_SS_INIT_CFG_TYPE {
		NET_EHOME_SS_INIT_CFG_SDK_PATH, //设置SS组件加载路径（仅Linux版本支持）
		NET_EHOME_SS_INIT_CFG_CLOUD_TIME_DIFF //设置运存储的请求时间差值,默认15分钟,最小5分钟,最大60分钟
	}

	/**
	 * Tomcat图片服务器回调信息
	 */
	public static class NET_EHOME_SS_TOMCAT_MSG extends Structure {
		public byte[] szDevUri = new byte[MAX_URL_LEN_SS]; //设备请求的URI
		public int dwPicNum; //图片数
		public String pPicURLs; //图片URL,每个URL MAX_URL_LEN_SS长度
		public byte[] byRes = new byte[64];
	}

	/**
	 * 监听参数
	 */
	public static class NET_EHOME_SS_LISTEN_PARAM extends Structure {
		public NET_EHOME_IPADDRESS struAddress = new NET_EHOME_IPADDRESS(); //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
		public byte[] szKMS_UserName = new byte[MAX_KMS_USER_LEN]; //KMS用户名
		public byte[] szKMS_Password = new byte[MAX_KMS_PWD_LEN]; //KMS用户名
		public EHomeSSStorageCallBack fnSStorageCb;//图片服务器信息存储回调函数
		public EHomeSSMsgCallBack fnSSMsgCb; //图片服务器信息Tomcat回调函数
		public byte[] szAccessKey = new byte[MAX_CLOUD_AK_SK_LEN]; //EHome5.0存储协议AK
		public byte[] szSecretKey = new byte[MAX_CLOUD_AK_SK_LEN]; //EHome5.0存储协议SK
		public Pointer pUserData; //用户参数
		public byte byHttps; //是否启用HTTPs
		public byte[] byRes1 = new byte[3];
		public EHomeSSRWCallBack fnSSRWCb;//读写回调函数
		public EHomeSSRWCallBackEx fnSSRWCbEx;
		public byte bySecurityMode;
		public byte[] byRes = new byte[51];
	}

	/**
	 * IP地址结构体
	 */
	public static class NET_EHOME_IPADDRESS extends Structure {
		public byte[] szIP = new byte[128]; //IP地址 
		public short wPort; //端口号
		public byte[] byRes = new byte[2]; //保留，置为0 
	}

	/**
	 * 监听Https参数
	 */
	public static class NET_EHOME_SS_LISTEN_HTTPS_PARAM extends Structure {
		public byte byHttps; //0-不启用HTTPS 1-启用HTTPS
		public byte byVerifyMode; //0-单向认证(暂只支持单向认证)
		public byte byCertificateFileType; //证书类型0-pem, 1-ANS1
		public byte byPrivateKeyFileType; //私钥类型0-pem, 1-ANS1
		public byte[] szUserCertificateFile = new byte[MAX_PATH]; //用户名
		public byte[] szUserPrivateKeyFile = new byte[32]; //密码 
		public int dwSSLVersion;//SSL Method版本
		//0 - SSL23, 1 - SSL2, 2 - SSL3, 3 - TLS1.0, 4 - TLS1.1, 5 - TLS1.2
		//SSL23是兼容模式，会协商客户端和服务端使用的最高版本
		public byte[] byRes3 = new byte[360];
	}

	/**
	 * 图片上传客户端参数
	 */
	public static class NET_EHOME_SS_CLIENT_PARAM extends Structure {
		public int enumType; //图片上传客户端类型  NET_EHOME_SS_CLIENT_TYPE
		public NET_EHOME_IPADDRESS struAddress; //图片服务器地址
		public byte byHttps;//是否启用HTTPs
		public byte[] byRes = new byte[63];
	}

	public static class NET_EHOME_SS_LOCAL_SDK_PATH extends Structure {
		public byte[] sPath = new byte[MAX_PATH];
		public byte[] byRes = new byte[128];
	}

	public static class NET_EHOME_SS_RW_PARAM extends Structure {
		public Pointer pFileName;   //文件名
		public Pointer pFileBuf;          //文件内容
		public IntByReference dwFileLen;   //文件大小
		public Pointer pFileUrl;    //文件url
		public Pointer pUser;             //
		public byte byAct; //读写操作：0-写文件，1-读文件
		public byte byUseRetIndex;  //是否使用上层返回的pRetIndex：0-不使用，1-使用
		public byte[] byRes1 = new byte[2];
		public Pointer pRetIndex;     //上层设置的索引，pRetIndex为0时，可不设置，pRetIndex为1时候，设置
		public byte[] byRes = new byte[56];
	}

	public static class StringPointer extends Structure{
		public byte[]  sData;

		public StringPointer(){}

		public StringPointer(int dwLength){
			if(dwLength == 0){
				throw new NullPointerException("Data length can`t be zero");
			}

			this.sData = new byte[dwLength];
		}

		public StringPointer(String sContent) {
			if(sContent == null){
				throw new NullPointerException("Content can`t be null");
			}

			this.sData = new byte[256];
			System.arraycopy(sContent.getBytes(), 0, this.sData, 0, sContent.length());
		}

		public StringPointer(byte[] byData){
			this.sData = new byte[256];
			System.arraycopy(byData, 0, this.sData, 0, byData.length);
		}

		public String GetString(){
			return CommonMethod.byteToString(this.sData);
		}

		public byte[] GetByteArray(){
			return this.sData;
		}

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("sData");
		}
	}

	public static class NET_EHOME_SS_EX_PARAM extends Structure {
                public byte byProtoType;        
                public byte[] byRes = new byte[23];
                public NET_EHOME_SS_Union unionStoreInfo = new NET_EHOME_SS_Union();
	}
        
         public static class NET_EHOME_SS_Union extends Union {	
                public NET_EHOME_SS_CLOUD_PARAM struCloud = new NET_EHOME_SS_CLOUD_PARAM();
	}
         
        public static class NET_EHOME_SS_CLOUD_PARAM extends Structure {	
                public String pPoolId;  
                public byte byPoolIdLength;   
                public int dwErrorCode;   
                public byte[] byRes = new byte[503];                
	} 
         
        
        
	//初始化，反初始化
	boolean NET_ESS_Init();

	boolean NET_ESS_Fini();

	/**
	 * 信息回调函数
	 */
	public static interface EHomeSSMsgCallBack extends Callback {
		public boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
				int dwInLen, Pointer pUser);
	}

	/**
	 * 存储回调函数
	 */
	public static interface EHomeSSStorageCallBack extends Callback {
		public boolean invoke(int iHandle, String pFileName, Pointer pFileBuf, int dwOutLen, Pointer pFilePath,
				Pointer pUser);
	}
        
        /**
	 * 读写回调函数 byAct 0-读 1-写 2-删
	 */
	public static interface EHomeSSRWCallBack extends Callback {
		public boolean invoke(int iHandle, byte byAct, String pFileName, Pointer pFileBuf, int dwFileLen, String pFileUrl,
				Pointer pUser);
	}
        
	public static interface EHomeSSRWCallBackEx extends Callback {
		public boolean invoke(int iHandle, NET_EHOME_SS_RW_PARAM pRwParam, NET_EHOME_SS_EX_PARAM pExStruct);
	}
        

	/**
	 * 获取错误码
	 */
	int NET_ESS_GetLastError();

	/**
	 * 日志
	 * @param iLogLevel
	 * @param strLogDir
	 * @param bAutoDel
	 * @return
	 */
	boolean NET_ESS_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);
        
        boolean NET_ESS_SetSDKInitCfg(int enumType, Pointer lpInBuff);
	/**
	 * 获取版本号
	 * @return
	 */
	int NET_ESS_GetBuildVersion();

	/**
	 * 设置HTTP监听的Https参数
	 * @param pSSHttpsParam
	 * @return
	 */
	boolean NET_ESS_SetListenHttpsParam(NET_EHOME_SS_LISTEN_HTTPS_PARAM pSSHttpsParam);

	/**
	 * 开启监听
	 * @param pSSListenParam
	 * @return
	 */
	int NET_ESS_StartListen(NET_EHOME_SS_LISTEN_PARAM pSSListenParam);

	/**
	 * 关闭监听
	 * @param lListenHandle
	 * @return
	 */
	boolean NET_ESS_StopListen(int lListenHandle);

	/**
	 * 设置初始化参数
	 * @param enumType NET_EHOME_SS_INIT_CFG_TYPE enumType
	 * @param lpInBuff
	 * @return
	 */


	/**
	 * 创建图片上传/下载客户端
	 * @param pClientParam
	 * @return
	 */
	int NET_ESS_CreateClient(NET_EHOME_SS_CLIENT_PARAM pClientParam);

	/**
	 * 设置图片上传/下载客户端超时时间,单位ms,默认为5s
	 * @param lHandle
	 * @param dwSendTimeout
	 * @param dwRecvTimeout
	 * @return
	 */
	boolean NET_ESS_ClientSetTimeout(int lHandle, int dwSendTimeout, int dwRecvTimeout);

	/**
	 * 设置图片上传/下载客户端参数
	 * @param lHandle
	 * @param strParamName
	 * @param strParamVal
	 * @return
	 */
	boolean NET_ESS_ClientSetParam(int lHandle, String strParamName, String strParamVal);

	/**
	 * 图片上传/下载客户端执行上传
	 * @param lHandle
	 * @param strUrl
	 * @param dwUrlLen
	 * @return
	 */
	boolean NET_ESS_ClientDoUpload(int lHandle, byte[] strUrl, int dwUrlLen);

	/**
	 * 图片上传/下载客户端执行下载
	 * @param lHandle
	 * @param strUrl
	 * @param pFileContent
	 * @param dwContentLen
	 * @return
	 */
	boolean NET_ESS_ClientDoDownload(int lHandle, String strUrl, PointerByReference pFileContent, IntByReference dwContentLen);

	/**
	 * 销毁客户端
	 * @param lHandle
	 * @return
	 */
	boolean NET_ESS_DestroyClient(int lHandle);

	//计算HMAC-SHA256
	boolean NET_ESS_HAMSHA256(String pSrc, String pSecretKey, String pSingatureOut, int dwSingatureLen);

}
