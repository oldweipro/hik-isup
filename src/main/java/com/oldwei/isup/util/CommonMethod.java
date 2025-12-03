package com.oldwei.isup.util;

import com.sun.jna.Pointer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonMethod {
    public static void WriteBuffToPointer(byte[] byData, Pointer pInBuffer) {
        pInBuffer.write(0, byData, 0, byData.length);
    }

    public static String byteToString(byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return "";
        }
        int iLengthOfBytes = 0;
        for (byte st : bytes) {
            if (st != 0) {
                iLengthOfBytes++;
            } else
                break;
        }
        String strContent = "";
        try {
            strContent = new String(bytes, 0, iLengthOfBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strContent;
    }

    /**
     * utf8字节数组转gbk字节数组
     *
     * @param utf8Bytes
     * @return
     */
    public static byte[] UTF8toGBK(byte[] utf8Bytes) {
        String utf8Str = new String(utf8Bytes, StandardCharsets.UTF_8);
        byte[] gbkBytes = utf8Str.getBytes(Charset.forName("GBK"));
        return gbkBytes;
    }

    /**
     * utf8字节数组转gbk字符串
     *
     * @param utf8Bytes
     * @return
     */
    public static String UTF8toGBKStr(byte[] utf8Bytes) {
        return new String(UTF8toGBK(utf8Bytes), Charset.forName("GBK"));
    }

    /**
     * 获取resource文件夹下的文件绝对路径
     *
     * @param filePath 文件相对于resources文件夹的相对路径, 格式描述举例为 conf/XX/XX.json
     * @return
     */
    public static String getResFileAbsPath(String filePath) {
        if (filePath == null) {
            throw new RuntimeException("filePath null error!");
        }
        if (OsSelect.isWindows()) {
            return System.getProperty("user.dir") + "\\" + filePath;
        }
        return System.getProperty("user.dir") + "/" + filePath;
    }

    /**
     * 输出信息到文件中
     *
     * @param fileName    文件名
     * @param postFix     文件后缀
     * @param fileContent 文件内容
     */
    public static void outputToFile(String fileName, String postFix, String fileContent) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss_SSS");
        String folder = "";
        if (OsSelect.isWindows()) {
            folder = System.getProperty("user.dir") + "\\container\\outputFiles\\event\\";
        }
        if (OsSelect.isLinux()) {
            folder = System.getProperty("user.dir") + "/container/outputFiles/event/";
        }
        File directory = new File(folder);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.out.println(folder + "_文件夹创建失败！");
            }
        }

        String filePath = folder + fileName + "_" + format.format(new Date()) + postFix;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(fileContent.getBytes());
            fos.close();
        } catch (IOException e) {
            System.out.println("输出到文件出现异常：" + e.getMessage());
        }
    }
}
