package longi.module.pdm.util;

import longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub;
import longi.module.pdm.client.todo.ISysNotifyTodoWebServiceServiceSoapBindingStub;
import longi.module.pdm.constants.LONGiModuleBPMConfig;

import org.apache.axis.AxisFault;

import java.net.MalformedURLException;

import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPHeaderElement;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

public class LONGiModuleToConnectionUtil {


    public static ISysNotifyTodoWebServiceServiceSoapBindingStub getToService() throws MalformedURLException, AxisFault, SOAPException {
        String axisWsdl = LONGiModuleBPMConfig.TODO_AXIS_WSDL;
        Service service = new Service();
        ISysNotifyTodoWebServiceServiceSoapBindingStub stub;
        stub = new ISysNotifyTodoWebServiceServiceSoapBindingStub(
                new java.net.URL(axisWsdl), service);
        // 有些webservice需要登录，登陆后才能进行一些操作，这个需要设置如下两个参数：
        // 添加用户名信息
        SOAPHeaderElement header = new SOAPHeaderElement(LONGiModuleBPMConfig.AUTH_HEADER, "tns:RequestSOAPHeader");
        SOAPElement soap = null;
        soap = header.addChildElement("tns:user");
        soap.addTextNode(LONGiModuleBPMConfig.AUTH_USERNAME);
        soap = header.addChildElement("tns:password");
        soap.addTextNode(LONGiModuleBPMConfig.AUTH_PASSWORD);
        stub.setHeader(header);
        //1、 超时时间
        stub.setTimeout(LONGiModuleBPMConfig.AIXS_TIMEOUT);
        //2、 次数设置true，登录后才能保持登录状态，否则第二次调用ws方法时仍然会提示未登录。
        stub.setMaintainSession(false);
        return stub;
    }

    public static IKmReviewWebserviceServiceServiceSoapBindingStub getBPMService() throws MalformedURLException, AxisFault, SOAPException {
        String axisWsdl = LONGiModuleBPMConfig.ROUTE_AXIS_WSDL;//"http://portaltest.longi.com:8080/sys/webservice/kmReviewWebserviceService?wsdl"
        IKmReviewWebserviceServiceServiceSoapBindingStub stub;
        String actionType = "";
        Service service = new Service();
        stub = new IKmReviewWebserviceServiceServiceSoapBindingStub(
                new java.net.URL(axisWsdl), service);
        SOAPHeaderElement header = new SOAPHeaderElement(LONGiModuleBPMConfig.AUTH_HEADER, "tns:RequestSOAPHeader");
        SOAPElement soap = null;
        soap = header.addChildElement("tns:user");
        soap.addTextNode(LONGiModuleBPMConfig.AUTH_USERNAME);//PDM
        soap = header.addChildElement("tns:password");
        soap.addTextNode(LONGiModuleBPMConfig.AUTH_PASSWORD);//0ab4e1b53bd9f687b0582d69ef783f1a
        stub.setHeader(header);
        // 有些webservice需要登录，登陆后才能进行一些操作，这个需要设置如下两个参数：
        //1、 超时时间
        stub.setTimeout(LONGiModuleBPMConfig.AIXS_TIMEOUT);
        //2、 次数设置true，登录后才能保持登录状态，否则第二次调用ws方法时仍然会提示未登录。
        stub.setMaintainSession(false);
        return stub;
    }
}
