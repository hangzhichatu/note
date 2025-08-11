package longi.common.bean.bps;

public class CustomAttr {
	// 部门级别
	private String LGIDEPTLEVEL = "";// L1|L2|L3
	// 部门领导ID
	private String MANAGERID = "";
	// 部门领导名称
	private String MANAGERNAME = "";

	public String getLGIDEPTLEVEL() {
		return LGIDEPTLEVEL;
	}

	public void setLGIDEPTLEVEL(String lGIDEPTLEVEL) {
		LGIDEPTLEVEL = lGIDEPTLEVEL;
	}

	public String getMANAGERID() {
		return MANAGERID;
	}

	public void setMANAGERID(String mANAGERID) {
		MANAGERID = mANAGERID;
	}

	public String getMANAGERNAME() {
		return MANAGERNAME;
	}

	public void setMANAGERNAME(String mANAGERNAME) {
		MANAGERNAME = mANAGERNAME;
	}

}
