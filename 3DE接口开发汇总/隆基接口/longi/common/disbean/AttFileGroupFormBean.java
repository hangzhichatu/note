package longi.common.disbean;



import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author che
 */

public class AttFileGroupFormBean {

    /**
     * 文件编号
     */
    private String fileNo;

    /**
     * 文件版本
     */
    private String version;

    /**
     * 附件编码分类
     */
    private String fdCodeType;

    /**
     * 文件类型（0：主文件 1：附件 2：附表 3：评审材料 4：支撑性材料）
     */
    @NotNull
    private Integer type;

    /**
     * 附件信息集合
     */
    private List<AttFileFormBean> attFileFormBeans;

    /**
     * 附表保留年限(单位:年)
     */
    private Integer fdRetentionYear;
    
    private Integer fdCanDownload;
    
    private String fdFileUpdateType;

	public String getFileNo() {
		return fileNo;
	}

	public void setFileNo(String fileNo) {
		this.fileNo = fileNo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFdCodeType() {
		return fdCodeType;
	}

	public void setFdCodeType(String fdCodeType) {
		this.fdCodeType = fdCodeType;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public List<AttFileFormBean> getAttFileFormBeans() {
		return attFileFormBeans;
	}

	public void setAttFileFormBeans(List<AttFileFormBean> attFileFormBeans) {
		this.attFileFormBeans = attFileFormBeans;
	}

	public Integer getFdRetentionYear() {
		return fdRetentionYear;
	}

	public void setFdRetentionYear(Integer fdRetentionYear) {
		this.fdRetentionYear = fdRetentionYear;
	}

	public Integer getFdCanDownload() {
		return fdCanDownload;
	}

	public void setFdCanDownload(Integer fdCanDownload) {
		this.fdCanDownload = fdCanDownload;
	}

	public String getFdFileUpdateType() {
		return fdFileUpdateType;
	}

	public void setFdFileUpdateType(String fdFileUpdateType) {
		this.fdFileUpdateType = fdFileUpdateType;
	}
    
    
}
