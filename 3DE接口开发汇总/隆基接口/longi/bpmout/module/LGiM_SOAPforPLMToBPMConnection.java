package longi.bpmout.module;




import longi.module.pdm.constants.LONGiModuleBPMConfig;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.constants.Style;
import org.apache.axis.message.SOAPHeaderElement;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

public class LGiM_SOAPforPLMToBPMConnection {
    public static void connectBPMAndSentNewSheet(String[] args) {
        try {
            String sentJsonStr = args[0];

            // 创建服务对象
            Service service = new Service();

            // 创建调用对象
            Call call = (Call) service.createCall();

            // 设置 Web 服务的目标地址
            String endpoint = "https://portaltest.longi.com:8080/sys/webservice/kmReviewWebserviceService?wsdl"; // 替换为实际的 WSDL 地址
            call.setTargetEndpointAddress(new java.net.URL(endpoint));

            // 设置操作名和命名空间
            QName qname = new javax.xml.namespace.QName("http://webservice.review.km.kmss.landray.com/", "待更换操作名"); // 替换为实际的命名空间和操作名
            call.setOperation(qname.toString());


            // 设置 SOAP Action
            call.setUseSOAPAction(true);
            call.setSOAPActionURI(""); // 替换为实际的 SOAP Action 参考案例是空

            //设置头部
            SOAPHeaderElement header = new SOAPHeaderElement(LONGiModuleBPMConfig.AUTH_HEADER, "tns:RequestSOAPHeader");
            SOAPElement soap = null;
            soap = header.addChildElement("tns:user");
            soap.addTextNode(LONGiModuleBPMConfig.AUTH_USERNAME);//PDM
            soap = header.addChildElement("tns:password");
            soap.addTextNode(LONGiModuleBPMConfig.AUTH_PASSWORD);//0ab4e1b53bd9f687b0582d69ef783f1a
            call.addHeader(header);
            //设置超时时间
            call.setTimeout(12000);
            //设置登录保持
            call.setMaintainSession(false);
            // 调用 Web 服务操作并获取返回结果
            String response = (String) call.invoke(new Object[]{sentJsonStr}); // 替换为实际参数值
            // 打印返回信息
            System.out.println("Response from Web Service: " + response);

        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
        }
    }
}
