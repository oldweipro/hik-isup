package com.oldwei.isup.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class XmlUtil {

    public static int findXmlChannel(String xmlContent) {
        try {
//            String xmlContent = "<?xml version=\"1.0\" encoding=\"GB2312\"?>\n" +
//                    "<PPVSPMessage>\n" +
//                    "<Version>2.0</Version>\n" +
//                    "<Sequence>5262</Sequence>\n" +
//                    "<CommandType>RESPONSE</CommandType>\n" +
//                    "<WhichCommand>GETDEVICEWORKSTATUS</WhichCommand>\n" +
//                    "<Status>200</Status>\n" +
//                    "<Description>OK</Description>\n" +
//                    "<Params>\n" +
//                    "<DeviceStatusXML>\n" +
//                    "<Run>0</Run>\n" +
//                    "<CPU>9</CPU>\n" +
//                    "<Mem>86</Mem>\n" +
//                    "<DSKStatus/>\n" +
//                    "<CHStatus>\n" +
//                    "<CH>1-0-0-0-5043-0</CH>\n" +
//                    "<CH>2-0-0-0-5043-0</CH>\n" +
//                    "</CHStatus>\n" +
//                    "<AlarmInStatus/>\n" +
//                    "<AlarmOutStatus/>\n" +
//                    "<LocalDisplayStatus>0</LocalDisplayStatus>\n" +
//                    "<ForbidPreview>0</ForbidPreview>\n" +
//                    "<DefenseStatus>1</DefenseStatus>\n" +
//                    "<ArmDelayTime>0</ArmDelayTime>\n" +
//                    "<Remark>test/debug</Remark>\n" +
//                    "</DeviceStatusXML>\n" +
//                    "</Params>\n" +
//                    "</PPVSPMessage>";

            // Parse the XML string
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            // Get the <CH> element
            NodeList chList = doc.getElementsByTagName("CH");
            if (chList.getLength() > 0) {
                Element chElement = (Element) chList.item(0);
                String chValue = chElement.getTextContent();

                // Get the first character
                if (!chValue.isEmpty()) {
                    String value = String.valueOf(chValue.charAt(0));
                    int firstChar = Integer.parseInt(value);
//                    System.out.println("First character of <CH>: " + firstChar);
                    return firstChar;
                } else {
                    System.out.println("<CH> element is empty.");
                }
            } else {
                System.out.println("No <CH> element found in the XML.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
