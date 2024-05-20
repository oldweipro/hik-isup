package com.oldwei.hikisup.sdk.DemoApp;

import com.oldwei.hikisup.sdk.SdkService.AlarmService.AlarmDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.SsService.SsDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.VoiceDemo;

import java.io.IOException;
import java.util.Scanner;


public class IsupTest {

    // FIXME demo逻辑中默认只支持一台设备的功能演示，多台设备需要自行调整这里设备登录后的句柄信息
    static int lLoginID = -1;

    public IsupTest() throws IOException {
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        //初始化报警服务
        AlarmDemo alarmDemo = new AlarmDemo();
        alarmDemo.eAlarm_Init();
        alarmDemo.startAlarmListen();

        //初始化存储服务
        SsDemo ssDemo = new SsDemo();
        ssDemo.eSS_Init();
        ssDemo.startSsListen();

        //初始化流媒体服务(需要预览取流时使用)
//        StreamDemo streamDemo = new StreamDemo(null);
//        streamDemo.eStream_Init();

        //初始化语音流媒体服务(需要语音对讲时使用)
        VoiceDemo voiceDemo = new VoiceDemo();
        voiceDemo.voice_Init();
        voiceDemo.startVoiceServeListen();

        //初始化注册服务
        CmsDemo cmsDemo = new CmsDemo();
        cmsDemo.cMS_Init();
        cmsDemo.startCmsListen();

        //等待设备注册上线(单独调试存储服务不需要此步骤)
        while (lLoginID == -1) {
            Thread.sleep(500);
        }

        /**
         * ISUP5.0语音转发模块(需要设备在线, 需要确定设备是否支持此功能, 需要实现前面初始化语音流媒体服务的代码)
         */
/*        voiceDemo.StartVoiceTrans();
        voiceDemo.StopVoiceTrans();*/

        for (boolean exit = false; !exit; ) {
            System.out.println("请输入您想要执行的demo实例! （退出请输入yes）");
            Scanner input = new Scanner(System.in);
            String str = input.next();
            // 转换为标准输入
            str = str.toLowerCase();
            if (str.equals("yes")) {
                // 退出程序
                exit = true;
                break;
            }

            // 这里指令前缀第一位为16进制 0~F
            switch (str.substring(0, 1)) {
                case "f": {
                    /**
                     * F0001~F9999 预留【ISUP SDK服务】示例代码
                     */
                    System.out.println("\n[Module]通用的sdk服务实例代码");
                    SdkFunctionDemo.dispatch(str, lLoginID,
                            cmsDemo, alarmDemo, ssDemo, null, voiceDemo);
                    break;
                }
                case "1": {
                    /**
                     * 10001~19999 预留【门禁系统】相关的代码实现
                     * 门禁设备相关业务接口
                     */
                    System.out.println("\n[Module]门禁系统相关的demo示例代码");
                    break;
                }
                case "2": {
                    /**
                     * 20001~29999 预留【出入口系统】相关的代码实现
                     * 出入口设备相关业务接口
                     */
                    System.out.println("\n[Module]出入口系统相关的demo示例代码");
                    ParkingFunctionDemo.dispatch(str, lLoginID);
                    break;
                }
                default: {
                    System.out.println("\n未知的指令操作!请重新输入!\n");
                }
            }
        }

        //停止监听释放SDK
        if (CmsDemo.CmsHandle >= 0) {
            System.out.println("停止注册CMS服务");
            CmsDemo.hCEhomeCMS.NET_ECMS_StopListen(CmsDemo.CmsHandle);
            CmsDemo.hCEhomeCMS.NET_ECMS_Fini();
        }
        if (AlarmDemo.AlarmHandle >= 0) {
            System.out.println("停止报警Alarm服务");
            AlarmDemo.hcEHomeAlarm.NET_EALARM_StopListen(AlarmDemo.AlarmHandle);
            AlarmDemo.hcEHomeAlarm.NET_EALARM_Fini();
        }
        if (SsDemo.SsHandle >= 0) {
            System.out.println("停止存储SS服务");
            SsDemo.hCEhomeSS.NET_ESS_StopListen(SsDemo.SsHandle);
            SsDemo.hCEhomeSS.NET_ESS_Fini();
        }
        if (StreamDemo.StreamHandle >= 0) {
            System.out.println("停止流媒体Stream服务");
            StreamDemo.hCEhomeStream.NET_ESTREAM_StopListenPreview(StreamDemo.StreamHandle);
            StreamDemo.hCEhomeStream.NET_ESTREAM_Fini();
        }
        if (StreamDemo.m_lPlayBackListenHandle >= 0) {
            System.out.println("停止回放流媒体Stream服务");
            StreamDemo.hCEhomeStream.NET_ESTREAM_StopListenPlayBack(StreamDemo.m_lPlayBackListenHandle);
            StreamDemo.hCEhomeStream.NET_ESTREAM_Fini();
        }
        if (VoiceDemo.VoicelServHandle >= 0) {
            System.out.println("停止语音流媒体VoiceStream服务");
            VoiceDemo.hCEhomeVoice.NET_ESTREAM_StopListenVoiceTalk(VoiceDemo.VoicelServHandle);
            VoiceDemo.hCEhomeVoice.NET_ESTREAM_Fini();
        }
        return;
    }

}





