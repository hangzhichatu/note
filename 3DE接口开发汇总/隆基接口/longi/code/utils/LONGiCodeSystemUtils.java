package longi.code.utils;

import longi.code.pojo.LONGiCode;
import longi.code.pojo.LONGiCodeAttribute;
import longi.code.pojo.LONGiCodeDesc;
import longi.code.pojo.LONGiCodeValidate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.*;

/**
 * @ClassName: LONGiCodeSystemUtils
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-4-27 13:32
 */


public class LONGiCodeSystemUtils {

    public final static Element parseXml(String xmlPath) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(xmlPath);
        Element rootElement = document.getRootElement();
        return rootElement;
    }

    public final static List parseXmlAts(String xmlPath) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(xmlPath);
        Element rootElement = document.getRootElement();
        List atElementsList = getChildElements(rootElement, LONGiCodeSystemConstants.PARAM_ATTRIBUTE);
        return atElementsList;
    }

    public final static String getText(Element e, String tag) {
        Element element = e.element(tag);
        if (element != null)
            return element.getText();
        else
            return null;
    }

    public final static String getTextTrim(Element e, String tag) {
        Element element = e.element(tag);
//        System.out.println("element------------->"+element);
//        System.out.println("null != element------------->"+(null != element));

        if (null != element)
            return element.getTextTrim();
        else
            return null;
    }

    public static Element getChildElement(Element node, String childNode) {
        if (null == node || null == childNode || "".equals(childNode)) {
            return null;
        }
        return node.element(childNode);
    }

    public static List<Element> getChildElements(Element node, String tag) {
        if (null == node) {
            return null;
        }
        List<Element> lists = node.elements(tag);
        return lists;
    }

    public static String getAttribute(Element node, String attr) {
        if (null == node || attr == null || "".equals(attr)) {
            return "";
        }
        return node.attributeValue(attr);
    }

    public static Map<String, String> getAttributes(Element node, String... arg) {
        if (node == null || arg.length == 0) {
            return null;
        }
        Map<String, String> atMap = new HashMap<String, String>();
        for (String at : arg) {
            String atValue = node.attributeValue(at);
            atMap.put(at, atValue);
        }
        return atMap;
    }

    public static List getDynamicAttributeList(String xmlPath) throws Exception {
        List<LONGiCodeAttribute> atsList = new ArrayList<LONGiCodeAttribute>();
        List atsEleList = parseXmlAts(xmlPath);
        LONGiCodeAttribute at = null;
        String atName = "";
        String second_edit = "";
        Element atElement = null;
        Element settingsElement = null;
        Element viewElement = null;
        Element createElement = null;
        Element editElement = null;
        for (Iterator<Element> atIter = atsEleList.iterator(); atIter.hasNext(); ) {
            at = new LONGiCodeAttribute();
            atElement = atIter.next();
            atName = getTextTrim(atElement, LONGiCodeSystemConstants.PARAM_NAME);
            second_edit = getTextTrim(atElement, LONGiCodeSystemConstants.PARAM_SECOND_EDIT);
            at.setName(atName);
            at.setSecondEdit(second_edit);
            settingsElement = getChildElement(atElement, LONGiCodeSystemConstants.PARAM_SETTINGS);
            viewElement = getChildElement(settingsElement, LONGiCodeSystemConstants.PARAM_VIEW);
            if (viewElement == null) {
                continue;
            }
            Map viewSettingMap = new HashMap();
            viewSettingMap = parseSettingMap(viewElement, viewSettingMap);
            at.setViewSettingMap(viewSettingMap);
            createElement = getChildElement(settingsElement, LONGiCodeSystemConstants.PARAM_CREATE);
            Map createSettingMap = new HashMap();
            createSettingMap = parseSettingMap(createElement, viewSettingMap);
            at.setCreateSettingMap(createSettingMap);
            editElement = getChildElement(settingsElement, LONGiCodeSystemConstants.PARAM_EDIT);
            Map editSettingMap = new HashMap();
            editSettingMap = parseSettingMap(editElement, viewSettingMap);
            at.setEditSettingMap(editSettingMap);
            atsList.add(at);
        }
        return atsList;
    }

    public static LONGiCodeValidate parseValidateMap(Element element) {
        LONGiCodeValidate codeValidate = null;
        if (element != null) {
            codeValidate = new LONGiCodeValidate();
            Element fullRegExsElement = getChildElement(element, LONGiCodeSystemConstants.PARAM_FULL_REGEXS);
            if (fullRegExsElement != null) {
                String full_regExs = fullRegExsElement.getTextTrim();
                codeValidate.setFull_regExs(full_regExs);
            }
            Element regExsElement = getChildElement(element, LONGiCodeSystemConstants.PARAM_REGEXS);
            if (regExsElement != null) {
                String regExs = regExsElement.getTextTrim();
                codeValidate.setRegExs(regExs);
            }
            Element suffixElement = getChildElement(element, LONGiCodeSystemConstants.PARAM_SUFFIX);
            if (suffixElement != null) {
                String suffix = suffixElement.getTextTrim();
                codeValidate.setSuffix(suffix);
            }
            Element messageElement = getChildElement(element, LONGiCodeSystemConstants.PARAM_MESSAGE);
            if (messageElement != null) {
                String message = messageElement.getTextTrim();
                codeValidate.setMessage(message);
            }
        }
        return codeValidate;
    }

    public static Map parseSettingMap(Element element, Map map) {
        Map settingMap = new HashMap();
        settingMap.putAll(map);
        Element settingElement = null;
        String settingKey = "";
        String settingValue = "";
        if (element != null) {
            List elementsList = getChildElements(element, LONGiCodeSystemConstants.PARAM_SETTING);
            for (Iterator<Element> settingIter = elementsList.iterator(); settingIter.hasNext(); ) {
                settingElement = settingIter.next();
                settingKey = getAttribute(settingElement, LONGiCodeSystemConstants.PARAM_KEY);
                settingValue = settingElement.getTextTrim();
                settingMap.put(settingKey, settingValue);
            }
        }
        return settingMap;
    }

    public static Map getCodeInfoMap(String xmlPath) throws Exception {
        Map<String, Object> codeMap = new HashMap<String, Object>();

    try {
        List atsList = new ArrayList<LONGiCodeAttribute>();
        List codeList = new ArrayList<LONGiCode>();
        List descList = new ArrayList<LONGiCodeDesc>();
        Set<String> requiredSet = new HashSet<String>();
        Map validateMap = new HashMap<String, LONGiCodeValidate>();

        LONGiCodeAttribute at = null;
        LONGiCode code = null;
        LONGiCodeDesc desc = null;
        LONGiCodeValidate codeValidate = null;

        String second_edit = "";

        List atsElementsList = parseXmlAts(xmlPath);
        for (Iterator<Element> atIter = atsElementsList.iterator(); atIter.hasNext(); ) {
            Element atElement = atIter.next();
            String atName = getTextTrim(atElement, "name");
            Element codeInfoElement = getChildElement(atElement, LONGiCodeSystemConstants.PARAM_CODE_INFO);
            Element descInfoElement = getChildElement(atElement, LONGiCodeSystemConstants.PARAM_DESC_INFO);
            if (null != codeInfoElement) {
                String sequence = getTextTrim(codeInfoElement, LONGiCodeSystemConstants.PARAM_SEQUENCE);
                if (!"".equals(sequence) && sequence != null) {
                    String segment = getTextTrim(codeInfoElement, LONGiCodeSystemConstants.PARAM_SEGMENT);
                    code = new LONGiCode(atName, sequence, segment);
                    codeList.add(code);
                    requiredSet.add(atName);
                }
            }

            if (null != descInfoElement) {
                String sequence = getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_SEQUENCE);
                if (!"".equals(sequence) && sequence != null) {
                    desc = new LONGiCodeDesc();
                    desc.setSequence(sequence);
                    desc.setCn_prefix(getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_CN_PREFIX));
                    desc.setCn_suffix(getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_CN_SUFFIX));
                    desc.setEn_prefix(getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_EN_PREFIX));
                    desc.setEn_suffix(getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_EN_SUFFIX));
                    String segment = getTextTrim(descInfoElement, LONGiCodeSystemConstants.PARAM_SEGMENT);
//                    System.out.println("descInfoElement=====================>"+descInfoElement);
//
//                    System.out.println("segment=====================>"+segment);
                    if ( null != segment && !"".equals(sequence) ) {
                        desc.setSegment(segment);

                    }
                    desc.setName(atName);
                    descList.add(desc);
                    requiredSet.add(atName);
                }
            }
            at = new LONGiCodeAttribute(atName);
            second_edit = getTextTrim(atElement, LONGiCodeSystemConstants.PARAM_SECOND_EDIT);
            if (null!=second_edit&&!"".equals(second_edit)) {
//            if (UIUtil.isNotNullAndNotEmpty(second_edit)) {
                at.setSecondEdit(second_edit);
            }
            Element validateInfoElement = getChildElement(atElement, LONGiCodeSystemConstants.PARAM_VALIDATE);
            if (null != validateInfoElement) {
                codeValidate = parseValidateMap(validateInfoElement);
                validateMap.put(atName, codeValidate);
            }
            atsList.add(at);
        }
        if (codeList.size() > 0) {
            Collections.sort(codeList, new CodeSort());
            codeMap.put(LONGiCodeSystemConstants.PARAM_CODE_LIST, codeList);
        }
        if (descList.size() > 0 && atsList.size() > 0) {
            Collections.sort(descList, new DescSortAll());
            codeMap.put(LONGiCodeSystemConstants.PARAM_DESC_LIST, descList);
            codeMap.put(LONGiCodeSystemConstants.PARAM_ALL, atsList);
            codeMap.put(LONGiCodeSystemConstants.PARAM_VALIDATE_MAP, validateMap);
            codeMap.put(LONGiCodeSystemConstants.PARAM_REQUIRED_SET, requiredSet);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codeMap;
    }
}

class CodeSort implements Comparator<LONGiCode> {
    @Override
    public int compare(LONGiCode o1, LONGiCode o2) {
        if (Integer.parseInt(o1.getSequence()) > Integer.parseInt(o2.getSequence())) {
            return 1;
        }
        return -1;
    }
}

class DescSort implements Comparator<LONGiCodeDesc> {
    @Override
    public int compare(LONGiCodeDesc o1, LONGiCodeDesc o2) {
        if (Integer.parseInt(o1.getSequence()) > Integer.parseInt(o2.getSequence())) {
            return 1;
        }
        return -1;
    }
}


class DescSortAll implements Comparator<LONGiCodeDesc> {
    @Override
    public int compare(LONGiCodeDesc o1, LONGiCodeDesc o2) {
        int cr = 0;
        int a =Integer.parseInt(o1.getSequence()) - Integer.parseInt(o2.getSequence());
        if (a != 0) {
            cr = (a > 0) ? 3 : -1;
        } else {
            a = Integer.parseInt(o1.getSegment()) - Integer.parseInt(o2.getSegment());
            if (a != 0) {
                cr = (a > 0) ? 2 : -2;
            }

        }
        return cr;

    }
}