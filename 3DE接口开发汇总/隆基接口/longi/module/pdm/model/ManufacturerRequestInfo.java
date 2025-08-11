package longi.module.pdm.model;

import java.io.Serializable;
import java.util.List;

public class ManufacturerRequestInfo {

    /**
     * esbInfo : {"instId":"","requestTime":"","Attr1":"","Attr2":"","Attr3":""}
     * requestInfo : {"proClusters":{"proCluster":[{"Manufacturer_ID":"","Manufacturer_NAME":"","Description":"","Attr1":"","Attr2":"","Attr3":"","Attr4":""}]}}
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
         * instId :
         * requestTime :
         * Attr1 :
         * Attr2 :
         * Attr3 :
         */

        private String instId;
        private String requestTime;
        private String Attr1;
        private String Attr2;
        private String Attr3;

        public String getInstId() {
            return instId;
        }

        public void setInstId(String instId) {
            this.instId = instId;
        }

        public String getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(String requestTime) {
            this.requestTime = requestTime;
        }

        public String getAttr1() {
            return Attr1;
        }

        public void setAttr1(String Attr1) {
            this.Attr1 = Attr1;
        }

        public String getAttr2() {
            return Attr2;
        }

        public void setAttr2(String Attr2) {
            this.Attr2 = Attr2;
        }

        public String getAttr3() {
            return Attr3;
        }

        public void setAttr3(String Attr3) {
            this.Attr3 = Attr3;
        }
    }

    public static class RequestInfoBean implements Serializable {
        /**
         * proClusters : {"proCluster":[{"Manufacturer_ID":"","Manufacturer_NAME":"","Description":"","Attr1":"","Attr2":"","Attr3":"","Attr4":""}]}
         */

        private ProClustersBean proClusters;

        public ProClustersBean getProClusters() {
            return proClusters;
        }

        public void setProClusters(ProClustersBean proClusters) {
            this.proClusters = proClusters;
        }

        public static class ProClustersBean implements Serializable {
            private List<ProClusterBean> proCluster;

            public List<ProClusterBean> getProCluster() {
                return proCluster;
            }

            public void setProCluster(List<ProClusterBean> proCluster) {
                this.proCluster = proCluster;
            }

            public static class ProClusterBean implements Serializable {
                /**
                 * Manufacturer_ID :
                 * Manufacturer_NAME :
                 * Description :
                 * Attr1 :
                 * Attr2 :
                 * Attr3 :
                 * Attr4 :
                 */

                private String ManufacturerID;
                private String ManufacturerNAME;
                private String Description;
                private String Attr1;
                private String Attr2;
                private String Attr3;
                private String Attr4;

                public String getManufacturerID() {
                    return ManufacturerID;
                }

                public void setManufacturerID(String Manufacturer_ID) {
                    this.ManufacturerID = Manufacturer_ID;
                }

                public String getManufacturerNAME() {
                    return ManufacturerNAME;
                }

                public void setManufacturerNAME(String ManufacturerNAME) {
                    this.ManufacturerNAME = ManufacturerNAME;
                }

                public String getDescription() {
                    return Description;
                }

                public void setDescription(String Description) {
                    this.Description = Description;
                }

                public String getAttr1() {
                    return Attr1;
                }

                public void setAttr1(String Attr1) {
                    this.Attr1 = Attr1;
                }

                public String getAttr2() {
                    return Attr2;
                }

                public void setAttr2(String Attr2) {
                    this.Attr2 = Attr2;
                }

                public String getAttr3() {
                    return Attr3;
                }

                public void setAttr3(String Attr3) {
                    this.Attr3 = Attr3;
                }

                public String getAttr4() {
                    return Attr4;
                }

                public void setAttr4(String Attr4) {
                    this.Attr4 = Attr4;
                }

                @Override
                public String toString() {
                    return "ProClusterBean{" +
                            "ManufacturerID='" + ManufacturerID + '\'' +
                            ", ManufacturerNAME='" + ManufacturerNAME + '\'' +
                            ", Description='" + Description + '\'' +
                            ", Attr1='" + Attr1 + '\'' +
                            ", Attr2='" + Attr2 + '\'' +
                            ", Attr3='" + Attr3 + '\'' +
                            ", Attr4='" + Attr4 + '\'' +
                            '}';
                }
            }
        }
    }
}
