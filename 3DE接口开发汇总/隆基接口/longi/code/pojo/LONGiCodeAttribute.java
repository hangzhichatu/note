package longi.code.pojo;

import java.util.Map;

/**
 * @ClassName: LONGiCodeAttribute
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-4-27 13:25
 */

public class LONGiCodeAttribute {

    private String name;
    private String secondEdit;
    private Map<String, String> createSettingMap;
    private Map<String, String> viewSettingMap;
    private Map<String, String> editSettingMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecondEdit() {
        return secondEdit;
    }

    public void setSecondEdit(String secondEdit) {
        this.secondEdit = secondEdit;
    }

    public Map<String, String> getCreateSettingMap() {
        return createSettingMap;
    }

    public void setCreateSettingMap(Map<String, String> createSettingMap) {
        this.createSettingMap = createSettingMap;
    }

    public Map<String, String> getViewSettingMap() {
        return viewSettingMap;
    }

    public void setViewSettingMap(Map<String, String> viewSettingMap) {
        this.viewSettingMap = viewSettingMap;
    }

    public Map<String, String> getEditSettingMap() {
        return editSettingMap;
    }

    public void setEditSettingMap(Map<String, String> editSettingMap) {
        this.editSettingMap = editSettingMap;
    }

    public LONGiCodeAttribute() {
    }

    public LONGiCodeAttribute(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LONGiCodeAttribute{" +
                "name='" + name + '\'' +
                ", secondEdit='" + secondEdit + '\'' +
                ", createSettingMap=" + createSettingMap +
                ", viewSettingMap=" + viewSettingMap +
                ", editSettingMap=" + editSettingMap +
                '}';
    }
}
