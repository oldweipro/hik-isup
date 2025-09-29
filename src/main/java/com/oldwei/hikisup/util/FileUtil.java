package com.oldwei.hikisup.util;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static void writeFile(String fileName, byte[] content) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile(); // Create the file if it doesn't exist
                System.out.println("文件不存在，已创建新文件：" + fileName);
            }

            try (FileOutputStream fos = new FileOutputStream(fileName, true)) {
                fos.write(content); // Append the byte array to the file
//                System.out.println("字节流数组已成功追加写入文件。");
            } catch (IOException e) {
                System.out.println("写入文件时发生错误：" + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("创建文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void formatFile(String fileName, byte[] content) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(fileName);
            grabber.start();

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("outputFilePath.mp4", grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.setFormat("avi");
            recorder.start();

            while (true) {
                recorder.record(grabber.grab());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("文件已成功删除：" + fileName);
            } else {
                System.out.println("无法删除文件：" + fileName);
            }
        } else {
            System.out.println("文件不存在：" + fileName);
        }
    }

    public static void writeFile(File file, byte[] content) {
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(content); // 将字节流数组追加写入文件
            fos.close();
            System.out.println("字节流数组已成功追加写入文件。");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAsync(byte[] bytes) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Async task completed!" + bytes.length);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
