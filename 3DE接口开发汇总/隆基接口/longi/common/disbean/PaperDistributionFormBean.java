package longi.common.disbean;



import javax.validation.constraints.NotNull;

/**
 * 纸本分发
 */

public class PaperDistributionFormBean {
    /**
     * 文档ID
     */
    private String fdSourceId;

    /**
     * 接收人ID
     */
    @NotNull
    private String fdTargetId;

    /**
     * 分发数量
     */
    @NotNull
    private Integer count;

    /**
     * 分发类型
     */
    @NotNull
    private String fdType;

    /**
     * 备注
     */
    private String fdRemark;

	public String getFdSourceId() {
		return fdSourceId;
	}

	public void setFdSourceId(String fdSourceId) {
		this.fdSourceId = fdSourceId;
	}

	public String getFdTargetId() {
		return fdTargetId;
	}

	public void setFdTargetId(String fdTargetId) {
		this.fdTargetId = fdTargetId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getFdType() {
		return fdType;
	}

	public void setFdType(String fdType) {
		this.fdType = fdType;
	}

	public String getFdRemark() {
		return fdRemark;
	}

	public void setFdRemark(String fdRemark) {
		this.fdRemark = fdRemark;
	}
    
    
}
