package longi.code.pojo;

import java.util.Map;

/**
 * @ClassName: LONGiCodeImport
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-8-20 9:02
 */

public class LONGiCodeCreate {
    private String codeType;
    private String name;
    private Map attributeMap;
    private String codeTypeId;

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map attributeMap) {
        this.attributeMap = attributeMap;
    }

    public String getCodeTypeId() {
        return codeTypeId;
    }

    public void setCodeTypeId(String codeTypeId) {
        this.codeTypeId = codeTypeId;
    }

    public LONGiCodeCreate() {
    }

    public LONGiCodeCreate(String codeType, String name, Map attributeMap, String codeTypeId) {
        this.codeType = codeType;
        this.name = name;
        this.attributeMap = attributeMap;
        this.codeTypeId = codeTypeId;
    }

    @Override
    public String toString() {
        return "LONGiCodeCreate{" +
                "codeType='" + codeType + '\'' +
                ", name='" + name + '\'' +
                ", attributeMap=" + attributeMap +
                ", codeTypeId='" + codeTypeId + '\'' +
                '}';
    }
}
