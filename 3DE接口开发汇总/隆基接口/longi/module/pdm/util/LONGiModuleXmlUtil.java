package longi.module.pdm.util;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LONGiModuleXmlUtil {
    private static final Logger logger = Logger.getLogger(LONGiModuleXmlUtil.class);

    /**
     * 获得根据节点信息
     * @param xmlPath               xml字符串
     * @return
     */
    public static Element getRootElement(String xmlPath) {
        SAXReader saxReader = new SAXReader();
        Document doc = null;
        Element root = null;

        try {
//            doc = DocumentHelper.parseText(xml);
            doc = saxReader.read(xmlPath);
            root = doc.getRootElement();

        } catch (Exception ex) {
            // TODO: handle exception  
            logger.error("解释xml文件出现异常:" + ex.getMessage());
        }
        return root;
    }

    /** 
     * 获得指定元素下所有节点属性及值 
     * @param element 
     * @return 
     */  
    public static Map getNodeValues(Element element) {
        Element root = null;
        Map map = new HashMap();
        try {
            List list = element.elements();
            Element e = null;
            for (int i = 0; i < list.size(); i++) {
                e = (Element) list.get(i);
                map.put(e.getName(), e.getText());
            }
        } catch (Exception ex) {
            // TODO: handle exception  
            logger.error("获得指定元素下所有节点属性及值出现异常：" + ex.getMessage());
        }
        return map;
    }

    /** 
     * 获得指定节点指定属性值 
     * @param element                   元素名称 
     * @param attributeName             属性名称 
     * @return 
     */  
    public static String getElementAttributeValue(Element element, String attributeName) {
        String value = "";
        try {
            value = element.attributeValue("attributeName");
        } catch (Exception ex) {
            // TODO: handle exception  
            logger.error("获得指定节点指定属性值出现异常： " + ex.getMessage());
        }
        return value;
    }


    public static void main(String[] args) {
        try {
            String xmlPath = "F:\\sd_data\\eclipse_Workspace\\LongiPDM_2019linux\\src\\xml\\NonStandardProductVaultsStructure.xml";
			
//            Map map = LONGiModuleXmlUtil.getNodeValues(LONGiModuleXmlUtil.getRootElement(xmlPath));
            Element element = LONGiModuleXmlUtil.getRootElement(xmlPath);
            List<Element> listsElements = element.elements("folder");
            for (Iterator<Element> iterator = listsElements.iterator(); iterator.hasNext(); ) {
                Element next =  iterator.next();
                String seqString = next.attributeValue("seq");
                String titleString = next.elementText("title");
                System.out.println(seqString);
                System.out.println(titleString);
            }
            System.out.println(listsElements.size());
//            String result = (String) map.get("Result");
//            System.out.println("map = " + map);

//            Element mo = LONGiModuleXmlUtil.getRootElement(xmlPath).element("Mo");
//            System.out.println("mo = " + mo);
//            int num = Integer.parseInt(mo.attributeValue("num"));
//            System.out.println("size = " + num);
//            Element item = null;
//            List list = mo.elements();
//
//            for (int i = 0; i < list.size(); i++) {
//                item = (Element) list.get(i);
//                System.out.println("id = " + item.attributeValue("id") + " content = " + item.attributeValue("content") + " from_mobile = " + item.attributeValue("from_mobile") + " to_port = " + item.attributeValue("to_port") + " rec_time = " + item.attributeValue("rec_time"));
//            }

        } catch (Exception e) {
            // TODO: handle exception  
            System.out.println("ERROR:" + e.getMessage());
        }

    }

}  