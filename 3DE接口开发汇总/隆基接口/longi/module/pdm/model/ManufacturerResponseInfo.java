package longi.module.pdm.model;

import java.io.Serializable;

public class ManufacturerResponseInfo {

    /**
     * esbInfo : {"instId":"a00015d.2518d4e1.1.173273eed9d.N7f8f","returnStatus":"S","returnCode":"000","returnMsg":"[EHR]修改成功","requestTime":"2020-07-07 11:12:39","responseTime":"2020-07-07 11:12:39:000","attr1":"","attr2":"","attr3":""}
     * resultInfo : {"returnStatus":"S","returnCode":"000","returnMsg":"[EHR]修改成功"}
     */

    private EsbInfoBean esbInfo;
    private ResultInfoBean resultInfo;

    public EsbInfoBean getEsbInfo() {
        return esbInfo;
    }

    public void setEsbInfo(EsbInfoBean esbInfo) {
        this.esbInfo = esbInfo;
    }

    public ResultInfoBean getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfoBean resultInfo) {
        this.resultInfo = resultInfo;
    }

    public static class EsbInfoBean implements Serializable {
        /**
         * instId : a00015d.2518d4e1.1.173273eed9d.N7f8f
         * returnStatus : S
         * returnCode : 000
         * returnMsg : [EHR]修改成功
         * requestTime : 2020-07-07 11:12:39
         * responseTime : 2020-07-07 11:12:39:000
         * attr1 :
         * attr2 :
         * attr3 :
         */

        private String instId;
        private String returnStatus;
        private String returnCode;
        private String returnMsg;
        private String requestTime;
        private String responseTime;
        private String attr1;
        private String attr2;
        private String attr3;

        public String getInstId() {
            return instId;
        }

        public void setInstId(String instId) {
            this.instId = instId;
        }

        public String getReturnStatus() {
            return returnStatus;
        }

        public void setReturnStatus(String returnStatus) {
            this.returnStatus = returnStatus;
        }

        public String getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMsg() {
            return returnMsg;
        }

        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }

        public String getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(String requestTime) {
            this.requestTime = requestTime;
        }

        public String getResponseTime() {
            return responseTime;
        }

        public void setResponseTime(String responseTime) {
            this.responseTime = responseTime;
        }

        public String getAttr1() {
            return attr1;
        }

        public void setAttr1(String attr1) {
            this.attr1 = attr1;
        }

        public String getAttr2() {
            return attr2;
        }

        public void setAttr2(String attr2) {
            this.attr2 = attr2;
        }

        public String getAttr3() {
            return attr3;
        }

        public void setAttr3(String attr3) {
            this.attr3 = attr3;
        }
    }

    public static class ResultInfoBean implements Serializable {
        /**
         * returnStatus : S
         * returnCode : 000
         * returnMsg : [EHR]修改成功
         */

        private String returnStatus;
        private String returnCode;
        private String returnMsg;

        public String getReturnStatus() {
            return returnStatus;
        }

        public void setReturnStatus(String returnStatus) {
            this.returnStatus = returnStatus;
        }

        public String getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(String returnCode) {
            this.returnCode = returnCode;
        }

        public String getReturnMsg() {
            return returnMsg;
        }

        public void setReturnMsg(String returnMsg) {
            this.returnMsg = returnMsg;
        }
    }
}
