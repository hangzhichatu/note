package longi.module.pdm.model;

public class LONGiModuleControlledDocBPMRouteBean {

    /**
     * @Fields appName : 标识待办来源的系统
     */
    private String appName = "Longi";
    /**
     * @Fields modelName : 标识待办来源的模块
     */
    private String modelName = "Module";



    /**
     * @Fields modelId : 标识待办在原系统唯一标识
     */
    private String modelId = "";
    /**
     * @Fields subject : 待办标题
     */
    //标题
    private String Title = "";
    //申请人
    private String LONGiModuleApplicant = "";
    //所在部门
    private String LONGiModuleDepartment = "";
    //联系电话
    private String LONGiModulePhoneNumber = "";
    //申请日期
    private Long LONGiModuleDateOfApplication = null;
    //申请单类型
    private String LONGiModuleApplicationFormType = "";
    //文件性质
    private String LONGiModuleNatureOfFile = "";
    //技术文件类型
    private String LONGiModuleTechDocType = "";
    //新建修订目的和原因
    private String LONGiModuleControlledCause = "";
    //更改前
    private String LONGiModuleChangeBefore = "";
    //更改后
    private String LONGiModuleChangeAfter = "";
    //适用产品类型
    private String LONGiModuleAdaptedProduct = "";
    //适用基地
    private String LONGiModuleAdaptedBase = "";
    //计划切换实施时间
    private String LONGiModulePlanImplementedDate = "";
    //即将流向
    private String LONGiModuleFutureFlow = "";
    //通知紧急程度
    private String LONGiModuleUrgencyLevel = "";

    //技术文件名称
    private String[] LONGiTechDocTitle = {};
    //文件编号
    private String[] LONGiTechDocCode = {};
    //原版本号
    private String[] LONGiTechDocRev =  {};
    //原起草人
    private String[] LONGiTechDocDrafter = {};
    //原生效日期
    private String[] LONGiTechDocEffectiveDate =  {};
    //原批准人
    private String[] LONGiTechDocApprover =  {};
    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    public String getLONGiModuleApplicant() {
        return LONGiModuleApplicant;
    }
    public void setLONGiModuleApplicant(String LONGiModuleApplicant) {
        this.LONGiModuleApplicant = LONGiModuleApplicant;
    }

    public String getLONGiModuleDepartment() {
        return LONGiModuleDepartment;
    }

    public void setLONGiModuleDepartment(String LONGiModuleDepartment) {
        this.LONGiModuleDepartment = LONGiModuleDepartment;
    }

    public String getLONGiModulePhoneNumber() {
        return LONGiModulePhoneNumber;
    }

    public void setLONGiModulePhoneNumber(String LONGiModulePhoneNumber) {
        this.LONGiModulePhoneNumber = LONGiModulePhoneNumber;
    }

    public Long getLONGiModuleDateOfApplication() {
        return LONGiModuleDateOfApplication;
    }

    public void setLONGiModuleDateOfApplication(Long LONGiModuleDateOfApplication) {
        this.LONGiModuleDateOfApplication = LONGiModuleDateOfApplication;
    }

    public String getLONGiModuleApplicationFormType() {
        return LONGiModuleApplicationFormType;
    }

    public void setLONGiModuleApplicationFormType(String LONGiModuleApplicationFormType) {
        this.LONGiModuleApplicationFormType = LONGiModuleApplicationFormType;
    }

    public String getLONGiModuleNatureOfFile() {
        return LONGiModuleNatureOfFile;
    }

    public void setLONGiModuleNatureOfFile(String LONGiModuleNatureOfFile) {
        this.LONGiModuleNatureOfFile = LONGiModuleNatureOfFile;
    }

    public String getLONGiModuleTechDocType() {
        return LONGiModuleTechDocType;
    }

    public void setLONGiModuleTechDocType(String LONGiModuleTechDocType) {
        this.LONGiModuleTechDocType = LONGiModuleTechDocType;
    }

    public String getLONGiModuleControlledCause() {
        return LONGiModuleControlledCause;
    }

    public void setLONGiModuleControlledCause(String LONGiModuleControlledCause) {
        this.LONGiModuleControlledCause = LONGiModuleControlledCause;
    }

    public String getLONGiModuleChangeBefore() {
        return LONGiModuleChangeBefore;
    }

    public void setLONGiModuleChangeBefore(String LONGiModuleChangeBefore) {
        this.LONGiModuleChangeBefore = LONGiModuleChangeBefore;
    }

    public String getLONGiModuleChangeAfter() {
        return LONGiModuleChangeAfter;
    }

    public void setLONGiModuleChangeAfter(String LONGiModuleChangeAfter) {
        this.LONGiModuleChangeAfter = LONGiModuleChangeAfter;
    }

    public String getLONGiModuleAdaptedProduct() {
        return LONGiModuleAdaptedProduct;
    }

    public void setLONGiModuleAdaptedProduct(String LONGiModuleAdaptedProduct) {
        this.LONGiModuleAdaptedProduct = LONGiModuleAdaptedProduct;
    }

    public String getLONGiModuleAdaptedBase() {
        return LONGiModuleAdaptedBase;
    }

    public void setLONGiModuleAdaptedBase(String LONGiModuleAdaptedBase) {
        this.LONGiModuleAdaptedBase = LONGiModuleAdaptedBase;
    }

    public String getLONGiModulePlanImplementedDate() {
        return LONGiModulePlanImplementedDate;
    }

    public void setLONGiModulePlanImplementedDate(String LONGiModulePlanImplementedDate) {
        this.LONGiModulePlanImplementedDate = LONGiModulePlanImplementedDate;
    }

    public String getLONGiModuleFutureFlow() {
        return LONGiModuleFutureFlow;
    }

    public void setLONGiModuleFutureFlow(String LONGiModuleFutureFlow) {
        this.LONGiModuleFutureFlow = LONGiModuleFutureFlow;
    }

    public String getLONGiModuleUrgencyLevel() {
        return LONGiModuleUrgencyLevel;
    }

    public void setLONGiModuleUrgencyLevel(String LONGiModuleUrgencyLevel) {
        this.LONGiModuleUrgencyLevel = LONGiModuleUrgencyLevel;
    }

    public String[] getLONGiTechDocTitle() {
        return LONGiTechDocTitle;
    }

    public void setLONGiTechDocTitle(String[] LONGiTechDocTitle) {
        this.LONGiTechDocTitle = LONGiTechDocTitle;
    }

    public String[] getLONGiTechDocCode() {
        return LONGiTechDocCode;
    }

    public void setLONGiTechDocCode(String[] LONGiTechDocCode) {
        this.LONGiTechDocCode = LONGiTechDocCode;
    }

    public String[] getLONGiTechDocRev() {
        return LONGiTechDocRev;
    }

    public void setLONGiTechDocRev(String[] LONGiTechDocRev) {
        this.LONGiTechDocRev = LONGiTechDocRev;
    }

    public String[] getLONGiTechDocDrafter() {
        return LONGiTechDocDrafter;
    }

    public void setLONGiTechDocDrafter(String[] LONGiTechDocDrafter) {
        this.LONGiTechDocDrafter = LONGiTechDocDrafter;
    }

    public String[] getLONGiTechDocEffectiveDate() {
        return LONGiTechDocEffectiveDate;
    }

    public void setLONGiTechDocEffectiveDate(String[] LONGiTechDocEffectiveDate) {
        this.LONGiTechDocEffectiveDate = LONGiTechDocEffectiveDate;
    }

    public String[] getLONGiTechDocApprover() {
        return LONGiTechDocApprover;
    }

    public void setLONGiTechDocApprover(String[] LONGiTechDocApprover) {
        this.LONGiTechDocApprover = LONGiTechDocApprover;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
