package com.oldwei.hikisup.util;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
    private String configPath = null;

    /**
     * 配置文件对象
     */
    private Properties props = null;

    /**
     * 默认构造函数，用于sh运行，自动找到classpath下的config.properties。
     */
    public PropertiesUtil(String configPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(configPath));
        props = new Properties();
        props.load(bufferedReader);

    }

    /**
     * 根据key值读取配置的值
     *
     * @param key key值
     * @return key 键对应的值
     * @throws IOException
     */
    public String readValue(String key) {
        return props.getProperty(key);
    }

    /**
     * 读取properties的全部信息
     *
     * @throws FileNotFoundException 配置文件没有找到
     * @throws IOException           关闭资源文件，或者加载配置文件错误
     */
    public Map<String, String> readAllProperties() throws FileNotFoundException, IOException {
        //保存所有的键值
        Map<String, String> map = new HashMap<String, String>();
        Enumeration en = props.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String Property = props.getProperty(key);
            map.put(key, Property);
        }
        return map;
    }

    /**
     * 设置某个key的值,并保存至文件。
     *
     * @param key key值
     * @return key 键对应的值
     * @throws IOException
     */
    public void setValue(String key, String value) throws IOException {
        Properties prop = new Properties();
        InputStream fis = new FileInputStream(".\\config.properties");
        // 从输入流中读取属性列表（键和元素对）
        prop.load(fis);
        // 调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
        // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
        OutputStream fos = new FileOutputStream(".\\config.properties");
        prop.setProperty(key, value);
        // 以适合使用 load 方法加载到 Properties 表中的格式，
        // 将此 Properties 表中的属性列表（键和元素对）写入输出流
        prop.store(fos, "last update");
        //关闭文件
        fis.close();
        fos.close();


    }
}
