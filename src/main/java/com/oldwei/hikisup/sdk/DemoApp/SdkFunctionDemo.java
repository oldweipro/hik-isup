package com.oldwei.hikisup.sdk.DemoApp;

import com.oldwei.hikisup.util.CommonMethod;
import com.oldwei.hikisup.sdk.SdkService.AlarmService.AlarmDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.SsService.SsDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.VoiceDemo;
import com.oldwei.hikisup.sdk.UIModule.PlaybackVideoUI;
import com.oldwei.hikisup.sdk.UIModule.PreviewVideoUI;

import java.io.UnsupportedEncodingException;

/**
 * @author zhengxiaohui
 * @date 2023/8/19 14:44
 * @desc Isup SDK 通用框架层的示例代码模块
 */
public class SdkFunctionDemo {

    public static void dispatch(String command, int lLoginID,
                                CmsDemo cmsDemo, AlarmDemo alarmDemo, SsDemo ssDemo, StreamDemo streamDemo, VoiceDemo voiceDemo) throws UnsupportedEncodingException, InterruptedException {
        switch (command) {
            /**
             * 约定： f0001 ~ f0999 预留给 [cms] 模块的代码示例
             */
            case "f0001": {
                System.out.println("\n[Function]CMS控制信令透传(需要设备在线, 获取NVR设备工作状态、远程升级等)");
                cmsDemo.CMS_XMLRemoteControl(0);
                break;
            }
            case "f0002": {
                System.out.println("\n[Function]ISUP透传接口(云存储配置))");
                cmsDemo.CMS_ISAPIPassThrough(0);
                break;
            }
            /**
             * 约定： f1000 ~ f1999 预留给 [ams] 模块的代码示例
             */
            /**
             * 约定： f2000 ~ f2999 预留给 [ss] 模块的代码示例
             */
            case "f2000": {
                System.out.println("\n[Function]上传图片至存储服务器(返回图片URL可以用于人脸下发)");
                //上传图片至存储服务器(返回图片URL可以用于人脸下发)
                String fileAbsPath = CommonMethod.getResFileAbsPath("pics/FDLibNew.jpg");
                ssDemo.ssCreateClient(fileAbsPath);
                ssDemo.ssUploadPic();
                ssDemo.ssDestroyClient();
                break;
            }
            /**
             * 约定： f3000 ~ f3999 预留给 [sms] 模块的代码示例
             */
            case "f3001": {
                /**
                 * 实时预览模块(需要设备在线, 需要实现前面初始化流媒体服务(StreamDemo.eStream_Init)的代码)
                 */
                System.out.println("\n[Function]取流预览模块(有预览窗口)");
                PreviewVideoUI.jRealWinInit();

                streamDemo.startRealPlayListen_Win();
                // FIXME 注意这里的IChannel，不同设备类型可能不太一样
                streamDemo.RealPlay(0,1);

                // 这里只预览20s, 方便demo示例代码的效果演示
                Thread.sleep(60 * 1000);

                streamDemo.StopRealPlay(0);

                //关闭预览窗口
                PreviewVideoUI.jRealWinDestroy();
                break;
            }
            case "f3002": {
                /**
                 * 实时预览模块(需要设备在线, 需要实现前面初始化流媒体服务(StreamDemo.eStream_Init)的代码)
                 */
                System.out.println("\n[Function]取流预览模块(保存到本地文件)");
                streamDemo.startRealPlayListen_File("out.mp4");
                // FIXME 注意这里的IChannel，不同设备类型可能不太一样
                streamDemo.RealPlay(0,1);

                // 这里只预览20s, 方便demo示例代码的效果演示
                Thread.sleep(20 * 1000);

                streamDemo.StopRealPlay(0);
                break;
            }
            case "f3003": {
                /**
                 * 按时间回放模块(需要设备在线, 需要实现前面初始化流媒体服务(StreamDemo.eStream_Init)的代码)
                 */
                System.out.println("\n[Function]按时间回放模块(有回放窗口)");
                PlaybackVideoUI.jRealWinInit();
                // 回放监听函数调用
                streamDemo.startPlayBackListen_WIN();
                // FIXME 注意这里的IChannel，不同设备类型可能不太一样
                // 开启回放预览
                streamDemo.PlayBackByTime(0,1);

                // 这里只预览20s, 方便demo示例代码的效果演示
                Thread.sleep(20 * 1000);

                //停止回放预览
                streamDemo.stopPlayBackByTime(0);

                //关闭预览窗口
                PlaybackVideoUI.jRealWinDestroy();
                break;
            }
            case "f3004": {
                /**
                 * 按时间回放模块(需要设备在线, 需要实现前面初始化流媒体服务(StreamDemo.eStream_Init)的代码)
                 */
                System.out.println("\n[Function]按时间回放模块(保存到本地文件)");
                // 回放监听函数调用
                streamDemo.startPlayBackListen_FILE();
                // FIXME 注意这里的IChannel，不同设备类型可能不太一样
                // 开启回放预览
                streamDemo.PlayBackByTime(0,1);

                // 这里只预览20s, 方便demo示例代码的效果演示
                Thread.sleep(20 * 1000);

                //停止回放预览
                streamDemo.stopPlayBackByTime(0);

                break;
            }
            default: {
                System.out.println("未定义的指令类型!" + command);
            }
        }
    }
}
