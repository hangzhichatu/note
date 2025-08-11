package longi.module.pdm.model;


import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;

public class LONGiModuleKmReviewParamterFormDocInternalBean {


    private String dept = "";
    private String formType = "";
    private String formNature = "";
    private String techFileType = "";
    //原因,新产品（项目名称） 变更项目（项目名称） 仅技术资料变更 其他（内容描述） *
    private String modifyType = "";
    //原因,文本
    private String modifyReasonDesc = "";
    //更改前：
    //文件新增、废止无需填写
    private String beforeChangeDesc = "";
    //更改后：
    //文件新增、废止无需填写
    private String afterChangeDesc = "";
    private byte[] files = null;

    public byte[] getFiles() {
        return files;
    }

    public void setFiles(byte[] files) {
        this.files = files;
    }

    public LONGiModuleKmReviewParamterFormDocInternalBean() {
    }

    public LONGiModuleKmReviewParamterFormDocInternalBean(String dept, String formType, String formNature, String techFileType, String modifyType, String modifyReasonDesc, String beforeChangeDesc, String afterChangeDesc, String remarks, byte[] files) {
        this.dept = dept;
        this.formType = formType;
        this.formNature = formNature;
        this.techFileType = techFileType;
        this.modifyType = modifyType;
        this.modifyReasonDesc = modifyReasonDesc;
        this.beforeChangeDesc = beforeChangeDesc;
        this.afterChangeDesc = afterChangeDesc;
        this.remarks = remarks;
        this.files = files;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        if (formType.equals("Add")) {
            this.formType = "1";
        } else {
            this.formType = "2";
        }
    }

    public String getFormNature() {
        return formNature;
    }

    public void setFormNature(String formNature) {
        if (formNature.equals("Formal")) {
            this.formNature = "1";
        } else {
            this.formNature = "2";
        }
    }

    public String getTechFileType() {
        return techFileType;
    }

    public void setTechFileType(String techFileType) {
        this.techFileType = Integer.toString(LONGiModuleBPMConfig.TECH_FILE_TYPE_LIST.indexOf(techFileType) + 1);
    }

    public String getModifyType() {
        return modifyType;
    }

    public void setModifyType(String modifyType) {
        int tempInt = LONGiModuleBPMConfig.SOLVE_PORGRAM_LIST.indexOf(modifyType) + 1;
        if(tempInt==0){
            tempInt = 5;
        }
        this.modifyType = Integer.toString(tempInt);
    }

    public String getModifyReasonDesc() {
        return modifyReasonDesc;
    }

    public void setModifyReasonDesc(String modifyReasonDesc) {
        this.modifyReasonDesc = modifyReasonDesc;
    }

    public String getBeforeChangeDesc() {
        return beforeChangeDesc;
    }

    public void setBeforeChangeDesc(String beforeChangeDesc) {
        this.beforeChangeDesc = beforeChangeDesc;
    }

    public String getAfterChangeDesc() {
        return afterChangeDesc;
    }

    public void setAfterChangeDesc(String afterChangeDesc) {
        this.afterChangeDesc = afterChangeDesc;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    //备注
    private String remarks = "";


}
