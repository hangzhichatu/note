package longi.mybatis.bean;

import java.util.Date;

import longi.bom.utils.LONGiModuleJacksonCommonUtils;

public class ModelAccess {
	private String MODEL_NAME;
	private String IS_ACTIVE;
	private Date CREATE_DATE;
	private String MODEL_REVISION;
	private String MODEL_ID;
	private String MODEL_TYPE;
	private String MODEL_SECRET;
	public String getMODEL_NAME() {
		return MODEL_NAME;
	}
	public void setMODEL_NAME(String mODEL_NAME) {
		MODEL_NAME = mODEL_NAME;
	}
	public String getIS_ACTIVE() {
		return IS_ACTIVE;
	}
	public void setIS_ACTIVE(String iS_ACTIVE) {
		IS_ACTIVE = iS_ACTIVE;
	}
	public Date getCREATE_DATE() {
		return CREATE_DATE;
	}
	public void setCREATE_DATE(Date cREATE_DATE) {
		CREATE_DATE = cREATE_DATE;
	}
	public String getMODEL_REVISION() {
		return MODEL_REVISION;
	}
	public void setMODEL_REVISION(String mODEL_REVISION) {
		MODEL_REVISION = mODEL_REVISION;
	}
	public String getMODEL_ID() {
		return MODEL_ID;
	}
	public void setMODEL_ID(String mODEL_ID) {
		MODEL_ID = mODEL_ID;
	}
	public String getMODEL_TYPE() {
		return MODEL_TYPE;
	}
	public void setMODEL_TYPE(String mODEL_TYPE) {
		MODEL_TYPE = mODEL_TYPE;
	}
	public String getMODEL_SECRET() {
		return MODEL_SECRET;
	}
	public void setMODEL_SECRET(String mODEL_SECRET) {
		MODEL_SECRET = mODEL_SECRET;
	}
	@Override
    public String toString() {
		return LONGiModuleJacksonCommonUtils.obj2String(this);
    	
    }
}
