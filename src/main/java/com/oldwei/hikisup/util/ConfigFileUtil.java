package com.oldwei.hikisup.util;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author zhengxiaohui
 * @date 2023/8/15 19:07
 * @desc 配置文件处理工具
 */
public class ConfigFileUtil {

    /**
     * 获取请求数据报文内容
     * @param templateFilePath 报文模板格式文件位置,位于resources文件夹下面（conf/--/--.xx）
     * @param parameter 模板中可以替换的占位参数信息
     * @return
     */
    public static String getReqBodyFromTemplate(String templateFilePath, Map<String, Object> parameter) {
        String templateContent = ConfigFileUtil.readFileContent(templateFilePath);
        return ConfigFileUtil.replace(templateContent, parameter);
    }

    /**
     * 读取xml配置文件
     *
     * @param filePath 文件相对于resources文件夹的相对路径
     * @return
     */
    public static String readFileContent(String filePath) {
        String resourcePath = CommonMethod.getResFileAbsPath(filePath);

        // 读取指定文件路径的文件内容
        String contentStr = "";
        try {
            Path path = Paths.get(resourcePath);
            contentStr = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentStr;
    }

    /**
     * 替换 占位符变量固定为 ${}格式
     *
     * @param source    源内容
     * @param parameter 占位符参数
     *                  <p>
     *                  转义符默认为'$'。如果这个字符放在一个变量引用之前，这个引用将被忽略，不会被替换 如$${a}将直接输出${a}
     * @return
     */
    public static String replace(String source, Map<String, Object> parameter) {
        return replace(source, parameter, "${", "}", false);
    }

    /**
     * 替换
     *
     * @param source                        源内容
     * @param parameter                     占位符参数
     * @param prefix                        占位符前缀 例如:${
     * @param suffix                        占位符后缀 例如:}
     * @param enableSubstitutionInVariables 是否在变量名称中进行替换 例如:${system-${版本}}
     *                                      <p>
     *                                      转义符默认为'$'。如果这个字符放在一个变量引用之前，这个引用将被忽略，不会被替换 如$${a}将直接输出${a}
     * @return
     */
    public static String replace(String source, Map<String, Object> parameter, String prefix, String suffix, boolean enableSubstitutionInVariables) {
        //StrSubstitutor不是线程安全的类
        StrSubstitutor strSubstitutor = new StrSubstitutor(parameter, prefix, suffix);
        //是否在变量名称中进行替换
        strSubstitutor.setEnableSubstitutionInVariables(enableSubstitutionInVariables);
        return strSubstitutor.replace(source);
    }

}
