package com.oldwei.hikisup.util;

import java.util.Observable;
import java.util.Observer;

// 观察者类，用于处理数据
class DataHandler implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof byte[]) {
            // 这里写处理数据的逻辑，例如将数据写入管道流等
            byte[] data = (byte[]) arg;
            System.out.println("Received data: " + data.length + " bytes");
            // 在这里加入管道流的操作
        }
    }
}