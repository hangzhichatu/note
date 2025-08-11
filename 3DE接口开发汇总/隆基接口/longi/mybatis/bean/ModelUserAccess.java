package longi.mybatis.bean;

import java.io.Serializable;
import java.util.Date;

public class ModelUserAccess implements Serializable {
    private String MODEL_ID;
    private String USER_NAME;
    private String MODEL_NAME;
	private String MODEL_REVISION;
    public String getMODEL_ID() {
		return MODEL_ID;
	}

	public void setMODEL_ID(String mODEL_ID) {
		MODEL_ID = mODEL_ID;
	}

    public String getMODEL_REVISION() {
		return MODEL_REVISION;
	}

	public void setMODEL_REVISION(String mODEL_REVISION) {
		MODEL_REVISION = mODEL_REVISION;
	}

	private String IS_ACTIVE;
    private Date CREATE_DATA;
    private static final long serialVersionUID = -4260619761538644683L;
    public String getUSER_NAME() {
        return USER_NAME;
    }

    public void setUSER_NAME(String USER_NAME) {
        this.USER_NAME = USER_NAME;
    }

    public String getMODEL_NAME() {
        return MODEL_NAME;
    }

    public void setMODEL_NAME(String MODEL_NAME) {
        this.MODEL_NAME = MODEL_NAME;
    }

    public String getIS_ACTIVE() {
        return IS_ACTIVE;
    }

    public void setIS_ACTIVE(String IS_ACTIVE) {
        this.IS_ACTIVE = IS_ACTIVE;
    }

    public Date getCREATE_DATA() {
        return CREATE_DATA;
    }

    public void setCREATE_DATA(Date CREATE_DATA) {
        this.CREATE_DATA = CREATE_DATA;
    }
}
