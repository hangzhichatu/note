package longi.module.pdm.model;

public class LONGiModuleBPMReturnBean {
    //id
    private String objectId = "";
    //流程动作 0 OK |  1 Reject
    private String operationType = "";
    //驳回原因
    private String reasonsForRejection = "";

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getReasonsForRejection() {
        return reasonsForRejection;
    }

    public void setReasonsForRejection(String reasonsForRejection) {
        this.reasonsForRejection = reasonsForRejection;
    }
}
