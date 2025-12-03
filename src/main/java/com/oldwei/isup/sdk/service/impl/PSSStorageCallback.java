package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.EHomeSSStorageCallBack;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@Service("pssStorageCallback")
public class PSSStorageCallback implements EHomeSSStorageCallBack {

    public boolean invoke(int iHandle, String pFileName, Pointer pFileBuf, int dwFileLen, Pointer pFilePath, Pointer pUser) {
//        log.info("进入存储信息回调函数：{}", dwFileLen);
        String strPath = System.getProperty("user.dir") + "/container/ISUPPicServer/";
        String strFilePath = strPath + pFileName;

        //若此目录不存在，则创建之
        File myPath = new File(strPath);
        if (!myPath.exists()) {
            myPath.mkdir();
            log.info("创建文件夹路径为：{}", strPath);
        }

        if (dwFileLen > 0 && pFileBuf != null) {
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(strFilePath);
                //将字节写入文件
                long offset = 0;
                ByteBuffer buffers = pFileBuf.getByteBuffer(offset, dwFileLen);
                byte[] bytes = new byte[dwFileLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        pFilePath.write(0, strFilePath.getBytes(), 0, strFilePath.getBytes().length);

        return true;
    }
}
