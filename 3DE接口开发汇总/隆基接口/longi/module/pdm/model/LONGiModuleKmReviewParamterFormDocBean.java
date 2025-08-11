package longi.module.pdm.model;


public class LONGiModuleKmReviewParamterFormDocBean {
    public LONGiModuleKmReviewParamterFormDocBean() {
    }
    public LONGiModuleKmReviewParamterFormDocBean(LONGiModuleKmReviewParamterFormDocInternalBean internalBean) {

        this.fd_343f647599279e = internalBean.getDept();
        this.fd_3541e906a5e52a = internalBean.getFormType();
        this.fd_389744b0fabc14 = internalBean.getFormNature();
        this.fd_389744c9c7ca8c = internalBean.getTechFileType();
        this.fd_389747f27db0cc = internalBean.getModifyType();
        this.fd_3541ea06a01fda = internalBean.getModifyReasonDesc();
        this.fd_3541eaa5214756 = internalBean.getBeforeChangeDesc();
        this.fd_36775f62fb0d9a = internalBean.getAfterChangeDesc();
        this.fd_336b29502f3bba = internalBean.getRemarks();
//        this.fd_336b39b04689cc = internalBean.getFiles();
    }
    private String fd_343f647599279e = "";
    private String fd_3541e906a5e52a = "";
    private String fd_389744b0fabc14 = "";
    private String fd_389744c9c7ca8c = "";
    //原因,新产品（项目名称） 变更项目（项目名称） 仅技术资料变更 其他（内容描述） *
    private String fd_389747f27db0cc = "";
    //原因,文本
    private String fd_3541ea06a01fda = "";
    //更改前：
    //文件新增、废止无需填写
    private String fd_3541eaa5214756 = "";
    //更改后：
    //文件新增、废止无需填写
    private String fd_36775f62fb0d9a = "";

    //备注
    private String fd_336b29502f3bba = "";

//    //附件
//    private byte[] fd_336b39b04689cc = null;
//    
//    public byte[] getFd_336b39b04689cc() {
//		return fd_336b39b04689cc;
//	}
//	public void setFd_336b39b04689cc(byte[] fd_336b39b04689cc) {
//		this.fd_336b39b04689cc = fd_336b39b04689cc;
//	}
	public String getFd_343f647599279e() {
        return fd_343f647599279e;
    }

    public void setFd_343f647599279e(String fd_343f647599279e) {
        this.fd_343f647599279e = fd_343f647599279e;
    }
    public String getFd_3541e906a5e52a() {
        return fd_3541e906a5e52a;
    }

    public void setFd_3541e906a5e52a(String fd_3541e906a5e52a) {
        this.fd_3541e906a5e52a = fd_3541e906a5e52a;
    }

    public String getFd_389744b0fabc14() {
        return fd_389744b0fabc14;
    }

    public void setFd_389744b0fabc14(String fd_389744b0fabc14) {
        this.fd_389744b0fabc14 = fd_389744b0fabc14;
    }

    public String getFd_389744c9c7ca8c() {
        return fd_389744c9c7ca8c;
    }

    public void setFd_389744c9c7ca8c(String fd_389744c9c7ca8c) {
        this.fd_389744c9c7ca8c = fd_389744c9c7ca8c;
    }

    public String getFd_389747f27db0cc() {
        return fd_389747f27db0cc;
    }

    public void setFd_389747f27db0cc(String fd_389747f27db0cc) {
        this.fd_389747f27db0cc = fd_389747f27db0cc;
    }

    public String getFd_3541ea06a01fda() {
        return fd_3541ea06a01fda;
    }

    public void setFd_3541ea06a01fda(String fd_3541ea06a01fda) {
        this.fd_3541ea06a01fda = fd_3541ea06a01fda;
    }

    public String getFd_3541eaa5214756() {
        return fd_3541eaa5214756;
    }

    public void setFd_3541eaa5214756(String fd_3541eaa5214756) {
        this.fd_3541eaa5214756 = fd_3541eaa5214756;
    }

    public String getFd_36775f62fb0d9a() {
        return fd_36775f62fb0d9a;
    }

    public void setFd_36775f62fb0d9a(String fd_36775f62fb0d9a) {
        this.fd_36775f62fb0d9a = fd_36775f62fb0d9a;
    }

    public String getFd_336b29502f3bba() {
        return fd_336b29502f3bba;
    }

    public void setFd_336b29502f3bba(String fd_336b29502f3bba) {
        this.fd_336b29502f3bba = fd_336b29502f3bba;
    }


}
