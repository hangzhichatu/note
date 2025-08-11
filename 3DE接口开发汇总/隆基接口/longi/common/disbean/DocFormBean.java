package longi.common.disbean;


import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 文档模型
 */

public class DocFormBean {

    /**
     * 发布类型 (0:新建 1:修订 2:修改 4:再次提交)
     */
    @NotNull
    private Integer pubType;

    /**
     * 是否计划内(制度文件使用)(0:否 1:是)
     */
    private Integer docWithPlan;
    /**
     * 文档建设计划项ID(制度文件使用)
     */
    private String docPlanId;

    /**
     * 适用范围(制度文件使用):0 集团(不含海外) 1集团(含海外)
     */
    private String docAppScope;
    /**
     * 流程架构
     */
    private String flowStructure;

    /**
     * 流程文件 责任部门编号
     */
    private String responsibleDept;
    /**
     * 流程文件 发版日期
     */
    private Date docPublishTime;


    /**
     * 制度文件类型(制度文件)
     */
    private String systemFileType;

    /**
     * 生效时间(制度文件)
     */
    private Date fdEffTime;

    /**
     * 文档英文名(制度文件)
     */
    private String docSubjectEn;

    /**
     * 下级文件(制度文件)
     */
    private List<String> lowerFiles;
    /**
     * 关联术语（制度文件|流程文件使用）
     */
    private List<String> refTerms;
    /**
     * 同时废止相关制度（制度文件使用）
     */
    private List<String> refRepeals;

    /**
     * 上个版本的文件ID(修订时不可为空)
     */
    private String docOriginDocId;

    /**
     * 中心/分公司ID(技术文档需要指定)
     */
    private String orgCenterId;

    /**
     * 创建人编号
     */
    @NotNull
    private String docCreatorId;
    /**
     * 所属部门编号
     */
    private String docDeptId;
    /**
     * 创建时间
     */
    @NotNull
    private Date docCreateTime;
    /**
     * 文件大类(技术文件指定)
     */
    private String docType;
    /**
     * 文件小类(技术文件指定)
     */
    private String docSubType;
    /**
     * 文件编号
     */
    private String docNumber;
    /**
     * 主版本号
     */
    private Integer docMainVersion;
    /**
     * 辅版本号
     */
    private Integer docAuxiVersion;


    /**
     * 适用组织范围(组织编号)
     */
    private List<String> fdDeptScopes;

    /**
     * 文件密级(0:普通 1:A级商密 2:AA级商密 3:AAA级商密)
     */
    private Integer fdSecret;

    /**
     * 文档是否是有英文版主文档
     */
    private Integer fdHasEnVer;

    /**
     * 是否临时文件:0否 1是
     */
    private Integer fdIsTemporary;

    /**
     * 临时文件过期周期(1-12个)月
     */
    private Integer fdTempExpCycle;

    /**
     * 主文档标题
     */
    @NotNull
    private String docSubject;

    /**
     * 建设原因及摘要
     */
    private String fdDescription;
    /**
     * 是否参考(0:否,1:是)
     */
    private Integer isRef;
    /**
     * 参考文件ID列表
     */
    private List<String> refDocs;
    /**
     * 是否引用(0:否,1:是)
     */
    private Integer isQuote;
    /**
     * 引用文件ID列表
     */
    private List<String> quoteDocs;

    /**
     * 文件引用
     */
    private List<AttFileGroupFormBean> attFiles;
    
    private List<AttFileGroupFormBean> attFileNos;
    /**
     * 是否线下评审:0否 1是
     */
    private Integer fdIsOfflineReview;
    /**
     * 是否与变更关联:0否 1是
     */
    private Integer fdIsRelEcn;
    /**
     * ECN_NO
     */
    private String ecnNo;
    /**
     * 技术文件直接 负责人编号|流程文件 责任人编号
     */
    private String dri;
    /**
     * 部门领导编号
     */
    private String deptLeader;
    /**
     * 批准人编号
     */
    private String fdApprover;

    /**
     * 平行签核人员编号列表
     */
    private List<String> parallelSignOff;

    /**
     * 是否培训:0否 1是
     */
    private Integer fdIsTraining;
    /**
     * 培训方式:0线上培训 1线下培训
     */
    private Integer trainingType;

    /**
     * 是否纸本分发:0否 1是
     */
    private Integer fdIsPaper;

    /**
     * 纸本分发
     */
    private List<PaperDistributionFormBean> paperDistribution;
    /**
     * 可查看人员编号列表
     */
    private List<OrgElement> allReader;
    /**
     * 可编辑人员编号列表
     */
    private List<OrgElement> allEditor;
    /**
     * 可下载人员编号列表
     */
    private List<OrgElement> allDownloader;
    /**
     * 可打印人员编号列表
     */
    private List<OrgElement> allPrinter;

    //begin chexm 增加模版id
    /**
     * 文档分类id，值为枚举值
     */
    @NotNull
    private String fdTemplateId;
    //end chexm 增加模版id

    //begin chexm 增加文档实例id，修改，再次提交时使用
    /**
     * 文档实例id，文档修改业务、再次提交业务使用
     */
    private String isoDocId;

    /**
     * 标识数据的唯一性，用于多次重试调用时，保证多次处理结果的幂等性
     */
    @NotNull
    private String conditionId;
    //end chexm 增加文档实例id，修改，再次提交时使用
    
    //begin chexm 增加是否最新版验证
    /**
     * 文件大类描述
     */
    private String docTypeStr;

    /**
     * 文件细分类型描述
     */
    private String docSubTypeStr;

    /**
     * 线下评审附件id
     */
    private List<String> fdIsOfflineReviewFileId;

    /**
     * 支撑性材料
     */
    private List<String> supportingMaterialsFIleId;
    //begin chexm 增加是否最新版验证

    /**
     * 目录路径(技术文件使用) 以/分隔,不区分大小写
     */
    private String directoryPath;

	/**
	* PLM地址链接
	* */
	private String plmUrlKey;

	/**
	* 方案等级
	* */
	private String planLevel;
    
    
    private List<ReferencedFilenosBean> referencedFilenosBeans;

	public Integer getPubType() {
		return pubType;
	}

	public void setPubType(Integer pubType) {
		this.pubType = pubType;
	}

	public Integer getDocWithPlan() {
		return docWithPlan;
	}

	public void setDocWithPlan(Integer docWithPlan) {
		this.docWithPlan = docWithPlan;
	}

	public String getDocPlanId() {
		return docPlanId;
	}

	public void setDocPlanId(String docPlanId) {
		this.docPlanId = docPlanId;
	}

	public String getDocAppScope() {
		return docAppScope;
	}

	public void setDocAppScope(String docAppScope) {
		this.docAppScope = docAppScope;
	}

	public String getFlowStructure() {
		return flowStructure;
	}

	public void setFlowStructure(String flowStructure) {
		this.flowStructure = flowStructure;
	}

	public String getResponsibleDept() {
		return responsibleDept;
	}

	public void setResponsibleDept(String responsibleDept) {
		this.responsibleDept = responsibleDept;
	}

	public Date getDocPublishTime() {
		return docPublishTime;
	}

	public void setDocPublishTime(Date docPublishTime) {
		this.docPublishTime = docPublishTime;
	}

	public String getSystemFileType() {
		return systemFileType;
	}

	public void setSystemFileType(String systemFileType) {
		this.systemFileType = systemFileType;
	}

	public Date getFdEffTime() {
		return fdEffTime;
	}

	public void setFdEffTime(Date fdEffTime) {
		this.fdEffTime = fdEffTime;
	}

	public String getDocSubjectEn() {
		return docSubjectEn;
	}

	public void setDocSubjectEn(String docSubjectEn) {
		this.docSubjectEn = docSubjectEn;
	}

	public List<String> getLowerFiles() {
		return lowerFiles;
	}

	public void setLowerFiles(List<String> lowerFiles) {
		this.lowerFiles = lowerFiles;
	}

	public List<String> getRefTerms() {
		return refTerms;
	}

	public void setRefTerms(List<String> refTerms) {
		this.refTerms = refTerms;
	}

	public List<String> getRefRepeals() {
		return refRepeals;
	}

	public void setRefRepeals(List<String> refRepeals) {
		this.refRepeals = refRepeals;
	}

	public String getDocOriginDocId() {
		return docOriginDocId;
	}

	public void setDocOriginDocId(String docOriginDocId) {
		this.docOriginDocId = docOriginDocId;
	}

	public String getOrgCenterId() {
		return orgCenterId;
	}

	public void setOrgCenterId(String orgCenterId) {
		this.orgCenterId = orgCenterId;
	}

	public String getDocCreatorId() {
		return docCreatorId;
	}

	public void setDocCreatorId(String docCreatorId) {
		this.docCreatorId = docCreatorId;
	}

	public String getDocDeptId() {
		return docDeptId;
	}

	public void setDocDeptId(String docDeptId) {
		this.docDeptId = docDeptId;
	}

	public Date getDocCreateTime() {
		return docCreateTime;
	}

	public void setDocCreateTime(Date docCreateTime) {
		this.docCreateTime = docCreateTime;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocSubType() {
		return docSubType;
	}

	public void setDocSubType(String docSubType) {
		this.docSubType = docSubType;
	}

	public String getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	public Integer getDocMainVersion() {
		return docMainVersion;
	}

	public void setDocMainVersion(Integer docMainVersion) {
		this.docMainVersion = docMainVersion;
	}

	public Integer getDocAuxiVersion() {
		return docAuxiVersion;
	}

	public void setDocAuxiVersion(Integer docAuxiVersion) {
		this.docAuxiVersion = docAuxiVersion;
	}

	public List<String> getFdDeptScopes() {
		return fdDeptScopes;
	}

	public void setFdDeptScopes(List<String> fdDeptScopes) {
		this.fdDeptScopes = fdDeptScopes;
	}

	public Integer getFdSecret() {
		return fdSecret;
	}

	public void setFdSecret(Integer fdSecret) {
		this.fdSecret = fdSecret;
	}

	public Integer getFdHasEnVer() {
		return fdHasEnVer;
	}

	public void setFdHasEnVer(Integer fdHasEnVer) {
		this.fdHasEnVer = fdHasEnVer;
	}

	public Integer getFdIsTemporary() {
		return fdIsTemporary;
	}

	public void setFdIsTemporary(Integer fdIsTemporary) {
		this.fdIsTemporary = fdIsTemporary;
	}

	public Integer getFdTempExpCycle() {
		return fdTempExpCycle;
	}

	public void setFdTempExpCycle(Integer fdTempExpCycle) {
		this.fdTempExpCycle = fdTempExpCycle;
	}

	public String getDocSubject() {
		return docSubject;
	}

	public void setDocSubject(String docSubject) {
		this.docSubject = docSubject;
	}

	public String getFdDescription() {
		return fdDescription;
	}

	public void setFdDescription(String fdDescription) {
		this.fdDescription = fdDescription;
	}

	public Integer getIsRef() {
		return isRef;
	}

	public void setIsRef(Integer isRef) {
		this.isRef = isRef;
	}

	public List<String> getRefDocs() {
		return refDocs;
	}

	public void setRefDocs(List<String> refDocs) {
		this.refDocs = refDocs;
	}

	public Integer getIsQuote() {
		return isQuote;
	}

	public void setIsQuote(Integer isQuote) {
		this.isQuote = isQuote;
	}

	public List<String> getQuoteDocs() {
		return quoteDocs;
	}

	public void setQuoteDocs(List<String> quoteDocs) {
		this.quoteDocs = quoteDocs;
	}

	public List<AttFileGroupFormBean> getAttFiles() {
		return attFiles;
	}

	public void setAttFiles(List<AttFileGroupFormBean> attFiles) {
		this.attFiles = attFiles;
	}

	
	
	public List<AttFileGroupFormBean> getAttFileNos() {
		return attFileNos;
	}

	public void setAttFileNos(List<AttFileGroupFormBean> attFileNos) {
		this.attFileNos = attFileNos;
	}

	public Integer getFdIsOfflineReview() {
		return fdIsOfflineReview;
	}

	public void setFdIsOfflineReview(Integer fdIsOfflineReview) {
		this.fdIsOfflineReview = fdIsOfflineReview;
	}

	public Integer getFdIsRelEcn() {
		return fdIsRelEcn;
	}

	public void setFdIsRelEcn(Integer fdIsRelEcn) {
		this.fdIsRelEcn = fdIsRelEcn;
	}

	public String getEcnNo() {
		return ecnNo;
	}

	public void setEcnNo(String ecnNo) {
		this.ecnNo = ecnNo;
	}

	public String getDri() {
		return dri;
	}

	public void setDri(String dri) {
		this.dri = dri;
	}

	public String getDeptLeader() {
		return deptLeader;
	}

	public void setDeptLeader(String deptLeader) {
		this.deptLeader = deptLeader;
	}

	public String getFdApprover() {
		return fdApprover;
	}

	public void setFdApprover(String fdApprover) {
		this.fdApprover = fdApprover;
	}

	public List<String> getParallelSignOff() {
		return parallelSignOff;
	}

	public void setParallelSignOff(List<String> parallelSignOff) {
		this.parallelSignOff = parallelSignOff;
	}

	public Integer getFdIsTraining() {
		return fdIsTraining;
	}

	public void setFdIsTraining(Integer fdIsTraining) {
		this.fdIsTraining = fdIsTraining;
	}

	public Integer getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(Integer trainingType) {
		this.trainingType = trainingType;
	}

	public Integer getFdIsPaper() {
		return fdIsPaper;
	}

	public void setFdIsPaper(Integer fdIsPaper) {
		this.fdIsPaper = fdIsPaper;
	}

	public List<PaperDistributionFormBean> getPaperDistribution() {
		return paperDistribution;
	}

	public void setPaperDistribution(List<PaperDistributionFormBean> paperDistribution) {
		this.paperDistribution = paperDistribution;
	}

	public List<OrgElement> getAllReader() {
		return allReader;
	}

	public void setAllReader(List<OrgElement> allReader) {
		this.allReader = allReader;
	}

	public List<OrgElement> getAllEditor() {
		return allEditor;
	}

	public void setAllEditor(List<OrgElement> allEditor) {
		this.allEditor = allEditor;
	}

	public List<OrgElement> getAllDownloader() {
		return allDownloader;
	}

	public void setAllDownloader(List<OrgElement> allDownloader) {
		this.allDownloader = allDownloader;
	}

	public List<OrgElement> getAllPrinter() {
		return allPrinter;
	}

	public void setAllPrinter(List<OrgElement> allPrinter) {
		this.allPrinter = allPrinter;
	}

	public String getFdTemplateId() {
		return fdTemplateId;
	}

	public void setFdTemplateId(String fdTemplateId) {
		this.fdTemplateId = fdTemplateId;
	}

	public String getIsoDocId() {
		return isoDocId;
	}

	public void setIsoDocId(String isoDocId) {
		this.isoDocId = isoDocId;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void setConditionId(String conditionId) {
		this.conditionId = conditionId;
	}

	public String getDocTypeStr() {
		return docTypeStr;
	}

	public void setDocTypeStr(String docTypeStr) {
		this.docTypeStr = docTypeStr;
	}

	public String getDocSubTypeStr() {
		return docSubTypeStr;
	}

	public void setDocSubTypeStr(String docSubTypeStr) {
		this.docSubTypeStr = docSubTypeStr;
	}

	public List<String> getFdIsOfflineReviewFileId() {
		return fdIsOfflineReviewFileId;
	}

	public void setFdIsOfflineReviewFileId(List<String> fdIsOfflineReviewFileId) {
		this.fdIsOfflineReviewFileId = fdIsOfflineReviewFileId;
	}

	public List<String> getSupportingMaterialsFIleId() {
		return supportingMaterialsFIleId;
	}

	public void setSupportingMaterialsFIleId(List<String> supportingMaterialsFIleId) {
		this.supportingMaterialsFIleId = supportingMaterialsFIleId;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public List<ReferencedFilenosBean> getReferencedFilenosBeans() {
		return referencedFilenosBeans;
	}

	public void setReferencedFilenosBeans(List<ReferencedFilenosBean> referencedFilenosBeans) {
		this.referencedFilenosBeans = referencedFilenosBeans;
	}

	public String getPlmUrlKey() {
		return plmUrlKey;
	}

	public void setPlmUrlKey(String plmUrlKey) {
		this.plmUrlKey = plmUrlKey;
	}

	public String getPlanLevel() {
		return planLevel;
	}

	public void setPlanLevel(String planLevel) {
		this.planLevel = planLevel;
	}
    
    
    
}
