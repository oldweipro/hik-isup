package com.oldwei.isup.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 XML 反序列化工具，支持忽略命名空间（xmlns）
 * 适用于：Hikvision ISAPI / 海康 SDK / 任意动态 XML 命名空间格式
 * <p>
 * 示例：
 * DeviceInfo info = XmlUtils.fromXml(xmlString, DeviceInfo.class);
 */
@Slf4j
public class XmlUtil {

    // JAXBContext 缓存，避免重复创建（性能优化）
    private static final Map<Class<?>, JAXBContext> CONTEXT_CACHE = new ConcurrentHashMap<>();

    /**
     * 从 XML 字符串反序列化为对象（忽略命名空间）
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        if (xml == null || xml.isEmpty()) {
            log.warn("XmlUtils.fromXml: XML 字符串为空");
            return null;
        }
        try {
            JAXBContext context = CONTEXT_CACHE.computeIfAbsent(clazz, c -> {
                try {
                    return JAXBContext.newInstance(c);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            });
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // 使用 SAX 过滤器去除命名空间
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            XMLFilterImpl nsFilter = new XMLFilterImpl(xmlReader) {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts)
                        throws org.xml.sax.SAXException {
                    super.startElement("", localName, qName, atts);
                }

                @Override
                public void endElement(String uri, String localName, String qName)
                        throws org.xml.sax.SAXException {
                    super.endElement("", localName, qName);
                }
            };

            InputSource inputSource = new InputSource(new StringReader(xml));
            SAXSource saxSource = new SAXSource(nsFilter, inputSource);

            @SuppressWarnings("unchecked")
            T result = (T) unmarshaller.unmarshal(saxSource);
            return result;

        } catch (Exception e) {
            log.error("XmlUtils.fromXml 反序列化失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象序列化为 XML 字符串
     */
    public static String toXml(Object obj) {
        if (obj == null) return "";
        try {
            JAXBContext context = CONTEXT_CACHE.computeIfAbsent(obj.getClass(), c -> {
                try {
                    return JAXBContext.newInstance(c);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            });
            java.io.StringWriter writer = new java.io.StringWriter();
            context.createMarshaller().marshal(obj, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("XmlUtils.toXml 序列化失败: {}", e.getMessage(), e);
            return "";
        }
    }
}
