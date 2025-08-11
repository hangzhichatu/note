package longi.mybatis.bean;

import java.io.Serializable;

import longi.bom.utils.LONGiModuleJacksonCommonUtils;

public class UserInfo implements Serializable{
	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = 1L;
	private String OPR_ID;
	private String NAME;
	private String EMPL_ID;
	private String CREATE_DATE;
	private String PERSON_STATUS;
	private String POSNDESCR;
	private String GPSLONGDESCR;
	private String PERSON_ACTIONS_NAME;
	private String PERSON_ACTIONS_ID;
	public String getPERSON_ACTIONS_ID() {
		return PERSON_ACTIONS_ID;
	}
	public void setPERSON_ACTIONS_ID(String pERSON_ACTIONS_ID) {
		PERSON_ACTIONS_ID = pERSON_ACTIONS_ID;
	}
	public String getPERSON_ACTIONS_NAME() {
		return PERSON_ACTIONS_NAME;
	}
	public void setPERSON_ACTIONS_NAME(String pERSON_ACTIONS_NAME) {
		PERSON_ACTIONS_NAME = pERSON_ACTIONS_NAME;
	}
	public String getOPR_ID() {
		return OPR_ID;
	}
	public void setOPR_ID(String oPR_ID) {
		OPR_ID = oPR_ID;
	}
	public String getNAME() {
		return NAME;
	}
	public void setNAME(String nAME) {
		NAME = nAME;
	}
	public String getEMPL_ID() {
		return EMPL_ID;
	}
	public void setEMPL_ID(String eMPL_ID) {
		EMPL_ID = eMPL_ID;
	}
	public String getCREATE_DATE() {
		return CREATE_DATE;
	}
	public void setCREATE_DATE(String cREATE_DATE) {
		CREATE_DATE = cREATE_DATE;
	}
	public String getPERSON_STATUS() {
		return PERSON_STATUS;
	}
	public void setPERSON_STATUS(String pERSON_STATUS) {
		PERSON_STATUS = pERSON_STATUS;
	}
	public String getPOSNDESR() {
		return POSNDESCR;
	}
	public void setPOSNDESR(String pOSNDESR) {
		POSNDESCR = pOSNDESR;
	}
	public String getGPSLONGDESCR() {
		return GPSLONGDESCR;
	}

	public void setGPSLONGDESCR(String gPSLONGDESCR) {
		GPSLONGDESCR = gPSLONGDESCR;
	}
	/**
	* @Author: Longi.suwei
	* @Title: setGPSLONGDESCR
	* @Description: 重写JSON转字符串
	* @param: 
	* @date: 2023年10月19日 下午7:17:29
	*/
    public String toString() {
		return LONGiModuleJacksonCommonUtils.obj2String(this);
    	
    }
}
