package longi.module.pdm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class LONGiModuleESBUpdatePassBean {

    /**
     * esbInfo : {"requestTime":"2020-08-26 02:37:09","instId":"20879829514133946","attr2":null,"attr1":null,"attr3":null}
     * requestInfo : {"Employees":{"SourceCode":"ESC","Employee":[{"NewPassword":"f+2xlViGQOlaBJQCPzEEUw==","Longi_oprid":"lvbo3","EMPLID":"194241"}],"SysCode":"xaplm"}}
     */

    private EsbInfoBean esbInfo;
    private RequestInfoBean requestInfo;

    public EsbInfoBean getEsbInfo() {
        return esbInfo;
    }

    public void setEsbInfo(EsbInfoBean esbInfo) {
        this.esbInfo = esbInfo;
    }

    public RequestInfoBean getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfoBean requestInfo) {
        this.requestInfo = requestInfo;
    }

    public static class EsbInfoBean implements Serializable {
        /**
         * requestTime : 2020-08-26 02:37:09
         * instId : 20879829514133946
         * attr2 : null
         * attr1 : null
         * attr3 : null
         */

        private String requestTime;
        private String instId;
        private Object attr2;
        private Object attr1;
        private Object attr3;

        public String getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(String requestTime) {
            this.requestTime = requestTime;
        }

        public String getInstId() {
            return instId;
        }

        public void setInstId(String instId) {
            this.instId = instId;
        }

        public Object getAttr2() {
            return attr2;
        }

        public void setAttr2(Object attr2) {
            this.attr2 = attr2;
        }

        public Object getAttr1() {
            return attr1;
        }

        public void setAttr1(Object attr1) {
            this.attr1 = attr1;
        }

        public Object getAttr3() {
            return attr3;
        }

        public void setAttr3(Object attr3) {
            this.attr3 = attr3;
        }
    }

    public static class RequestInfoBean implements Serializable {
        /**
         * Employees : {"SourceCode":"ESC","Employee":[{"NewPassword":"f+2xlViGQOlaBJQCPzEEUw==","Longi_oprid":"lvbo3","EMPLID":"194241"}],"SysCode":"xaplm"}
         */
        @JsonProperty("Employees")
        private EmployeesBean Employees;

        public EmployeesBean getEmployees() {
            return Employees;
        }

        public void setEmployees(EmployeesBean Employees) {
            this.Employees = Employees;
        }

        public static class EmployeesBean implements Serializable {
            /**
             * SourceCode : ESC
             * Employee : [{"NewPassword":"f+2xlViGQOlaBJQCPzEEUw==","Longi_oprid":"lvbo3","EMPLID":"194241"}]
             * SysCode : xaplm
             */
            @JsonProperty("SourceCode")
            private String SourceCode;
            @JsonProperty("SysCode")
            private String SysCode;
            @JsonProperty("Employee")
            private List<EmployeeBean> Employee;

            public String getSourceCode() {
                return SourceCode;
            }

            public void setSourceCode(String SourceCode) {
                this.SourceCode = SourceCode;
            }

            public String getSysCode() {
                return SysCode;
            }

            public void setSysCode(String SysCode) {
                this.SysCode = SysCode;
            }

            public List<EmployeeBean> getEmployee() {
                return Employee;
            }

            public void setEmployee(List<EmployeeBean> Employee) {
                this.Employee = Employee;
            }

            public static class EmployeeBean implements Serializable {
                /**
                 * NewPassword : f+2xlViGQOlaBJQCPzEEUw==
                 * Longi_oprid : lvbo3
                 * EMPLID : 194241
                 */
                @JsonProperty("NewPassword")
                private String NewPassword;
                @JsonProperty("Longi_oprid")
                private String Longi_oprid;
                @JsonProperty("EMPLID")
                private String EMPLID;

                public String getNewPassword() {
                    return NewPassword;
                }

                public void setNewPassword(String NewPassword) {
                    this.NewPassword = NewPassword;
                }

                public String getLongi_oprid() {
                    return Longi_oprid;
                }

                public void setLongi_oprid(String Longi_oprid) {
                    this.Longi_oprid = Longi_oprid;
                }

                public String getEMPLID() {
                    return EMPLID;
                }

                public void setEMPLID(String EMPLID) {
                    this.EMPLID = EMPLID;
                }
            }
        }
    }
}
