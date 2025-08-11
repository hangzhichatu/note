package longi.mybatis.bean;

import longi.bom.utils.LONGiModuleJacksonCommonUtils;

import java.io.Serializable;

public class ActionAccess implements Serializable {
    private Integer ID;
    private String ACTION_NAME;
    private String ACTION_ID;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getACTION_NAME() {
        return ACTION_NAME;
    }

    public void setACTION_NAME(String ACTION_NAME) {
        this.ACTION_NAME = ACTION_NAME;
    }

    public String getACTION_ID() {
        return ACTION_ID;
    }

    public void setACTION_ID(String ACTION_ID) {
        this.ACTION_ID = ACTION_ID;
    }

    @Override
    public String toString() {
        return LONGiModuleJacksonCommonUtils.obj2String(this);
    }
}
