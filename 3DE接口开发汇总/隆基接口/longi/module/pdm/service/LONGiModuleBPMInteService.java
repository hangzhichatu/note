package longi.module.pdm.service;

import com.alibaba.fastjson.JSON;

import longi.module.pdm.client.route.AttachmentForm;
import longi.module.pdm.client.route.IKmReviewWebserviceServiceServiceSoapBindingStub;
import longi.module.pdm.client.route.KmReviewParamterForm;
import longi.module.pdm.model.LONGiModuleKmReviewParamterFormDocBean;
import longi.module.pdm.model.LONGiModuleKmReviewParamterFormDocInternalBean;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiModuleToConnectionUtil;

import javax.xml.soap.SOAPException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * @ClassName: LONGiModuleBPMInteService
 * @Description: TODO BPM流程集成类
 * @author: Longi.suwei
 * @date: Jun 29, 2020 5:49:01 PM
 */
public class LONGiModuleBPMInteService {
//    private static String axisWsdl = "http://portaltest.longi-silicon.com:8080/sys/webservice/kmReviewWebserviceService?wsdl";
//    public static org.apache.axis.client.Service service = new org.apache.axis.client.Service();
    public static IKmReviewWebserviceServiceServiceSoapBindingStub stub;
//    public static String actionType = "";
//    public static void main(String[] args) throws Exception {
//        //drafter_abandon
//        String id = "";
//        try {
//            stub = new IKmReviewWebserviceServiceServiceSoapBindingStub(
//                    new java.net.URL(axisWsdl), service);
//            SOAPHeaderElement header = new SOAPHeaderElement("http://sys.webservice.client", "tns:RequestSOAPHeader");
//            SOAPElement soap = null;
//            soap = header.addChildElement("tns:user");
//            soap.addTextNode("PDM");
//            soap = header.addChildElement("tns:password");
//            soap.addTextNode("0ab4e1b53bd9f687b0582d69ef783f1a");
//            stub.setHeader(header);
//            stub.setTimeout(1000 * 60 * 20);
//            stub.setMaintainSession(false);
//            id = stub.addReview(createForm());
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception(e);
//        }
//        // 有些webservice需要登录，登陆后才能进行一些操作，这个需要设置如下两个参数：
//        //1、 超时时间
//        //2、 次数设置true，登录后才能保持登录状态，否则第二次调用ws方法时仍然会提示未登录。
//    }


//    public static IKmReviewWebserviceServiceServiceSoapBindingStub getStub() {
//
//        String actionType = "";
//        try {
//            Service service = new Service();
//            if (stub == null) {
//                stub = new IKmReviewWebserviceServiceServiceSoapBindingStub(
//                        new java.net.URL(axisWsdl), service);
//                SOAPHeaderElement header = new SOAPHeaderElement("http://sys.webservice.client", "tns:RequestSOAPHeader");
//                SOAPElement soap = null;
//                soap = header.addChildElement("tns:user");
//                soap.addTextNode("PDM");
//                soap = header.addChildElement("tns:password");
//                soap.addTextNode("0ab4e1b53bd9f687b0582d69ef783f1a");
//                stub.setHeader(header);
//                // 有些webservice需要登录，登陆后才能进行一些操作，这个需要设置如下两个参数：
//                //1、 超时时间
//                stub.setTimeout(1000 * 60 * 20);
//                //2、 次数设置true，登录后才能保持登录状态，否则第二次调用ws方法时仍然会提示未登录。
//                stub.setMaintainSession(false);
//            }
//
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (SOAPException e) {
//            e.printStackTrace();
//        }
//        return stub;
//    }

    public static String createRoute(KmReviewParamterForm form) throws MalformedURLException, RemoteException, SOAPException {
        System.out.println("调用create接口"+ LONGiModuleFastJsonUtil.beanToJson(form));
        String id = "";
        String actionType = "";

        stub = LONGiModuleToConnectionUtil.getBPMService();
        id = stub.addReview(form);
        System.out.println("id=" + id);
        return id;
    }

    public static String updateRoute(KmReviewParamterForm form) throws MalformedURLException, RemoteException, SOAPException {
        System.out.println("调用update接口"+ LONGiModuleFastJsonUtil.beanToJson(form));
        String id = "";
        String actionType = "";
        stub = LONGiModuleToConnectionUtil.getBPMService();
        id = stub.updateReviewInfo(form);
        return id;
    }

    public static String approveRoute(KmReviewParamterForm form) throws RemoteException, MalformedURLException, SOAPException {
        System.out.println("调用approve接口:"+ LONGiModuleFastJsonUtil.beanToJson(form));
        String id = "";
        stub = LONGiModuleToConnectionUtil.getBPMService();

        id = stub.approveProcess(form);
        return id;
    }

/*    *//**
     * @Author: Longi.suwei
     * @Title: createForm
     * @Description: TODO 创建文档及流程数据
     * @param: @return
     * @param: @throws Exception
     * @date: Jun 29, 2020 5:49:22 PM
     *//*
    static KmReviewParamterForm createForm() throws Exception {
        KmReviewParamterForm form = new KmReviewParamterForm();
        // 文档模板id
        form.setFdTemplateId("172dfbb3af3f0d1cea8263645dda3fdb");

        // 文档标题
        form.setDocSubject("测试文件内容申请单17");

        // 流程发起人
        form.setDocCreator("{\"LoginName\": \"jiaofk\"}");

        // 文档关键字
        form.setFdKeyword("[\"受控\",\"技术文件\", \"临时\"]");

        *//*
         * 申请单类型
         * fd_3541e906a5e52a
         *
         *
         *
         * *//*

        // 流程表单
        LONGiModuleKmReviewParamterFormDocInternalBean internalFormBean = new LONGiModuleKmReviewParamterFormDocInternalBean();
        internalFormBean.setDept("隆基股份_组件事业部_产品研发中心_组件设计中心_研究四室");
        internalFormBean.setFormType("1");
        internalFormBean.setFormNature("2");
        internalFormBean.setTechFileType("2");
        internalFormBean.setModifyType("1");
        internalFormBean.setModifyReasonDesc("测试 新建、修订 、废止目的和原因");
        internalFormBean.setBeforeChangeDesc("");
        internalFormBean.setAfterChangeDesc("");
        internalFormBean.setRemarks("这是个测试流程");


//        byte[] fileByte = file2bytes("D:\\suwei3\\Desktop\\TEMP\\test.pdf");
//        
//        internalFormBean.setFiles(fileByte);
//        

        //fd_336b39b04689cc  文件

        LONGiModuleKmReviewParamterFormDocBean formDocBean = new LONGiModuleKmReviewParamterFormDocBean(internalFormBean);
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map map = LONGiModuleFastJsonUtil.jsonToMap(LONGiModuleFastJsonUtil.beanToJson(formDocBean));
//        String[] fileAttr1 = {"文件编号1"};
//        String[] fileAttr2 = {"原版本号1"};
//        String[] fileAttr3 = {"原起草人1"};
//        String[] fileAttr4 = {ft.format(date)};
//        String[] fileAttr5 = {"原批准人1"};
//        String[] fileAttr6 = {"wenjian1111"};
        map.put("fd_fileno1", "文件编号1");
        map.put("fd_n10", "plmcodeuser");
        map.put("fd_n20", "yangbo19");


//        map.put("fd_3897439d8696ac.fd_3541ea6192035c", fileAttr2);
//        map.put("fd_3897439d8696ac.fd_3541ea72d73f10", fileAttr3);
//        map.put("fd_3897439d8696ac.fd_3541ea7a17f410", fileAttr4);
//        map.put("fd_3897439d8696ac.fd_3541ea7cee4762", fileAttr5);
//        map.put("fd_3897439d8696ac.fd_38974405db480c", fileAttr6);

        System.out.println("map=" + map);


        String formValues = JSON.toJSONString(map);

//        String formValues = JSON.toJSONString(formDocBean);
        form.setFormValues(formValues);
        // 流程参数
//        String flowParam = "{auditNode:\"请审核\", futureNodeId:\"N10\", changeNodeHandlers:[\"N10:16cba7cb29a453e30a1a09e4ef5919f6;15bc97653f219c7e864018644bfa498e\",\"N20:16cba7cb29a453e30a1a09e4ef5919f6\"]}";
//        System.out.println(flowParam);
//        Thread.sleep(3000);

        //,"N20:16df2bd338e493d6be7c43e49509bfe4"
//        form.setFlowParam(flowParam);

//        List<AttachmentForm> attForms = createAllAtts();
        AttachmentForm[] attForms = createAllAtts();
//        form.getAttachmentForms().addAll(attForms);
//        form.
        form.setAttachmentForms(attForms);
        return form;
    }*/

    /**
     * @Author: Longi.suwei
     * @Title: createAllAtts
     * @Description: TODO 创建附件列表
     * @param: @return
     * @param: @throws Exception
     * @date: Jun 29, 2020 5:49:38 PM
     */
//    List<AttachmentForm> createAllAtts() throws Exception {
//
//        List<AttachmentForm> attForms = new ArrayList<AttachmentForm>();
//
//        String fileName = "ucbug.com-NetDrive.zip";
//        AttachmentForm attForm01 = createAtt(fileName);
////        attForm01.setFdKey("fd_3897439d8696ac.0.fd_38974405db480c");
//        attForm01.setFdKey("fd_336b39b04689cc");
////        attForm01.setFdKey("fd_336b39b04689cc");
////        File file01 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
////        DataHandler dataHandler01 = new DataHandler(new FileDataSource(file01));
////        attForm01.setFdAttachment(dataHandler01);
//        fileName = "commons-collections-3.2.2.jar";
//        AttachmentForm attForm02 = createAtt(fileName);
//        attForm02.setFdKey("fd_336b39b04689cc");
////        File file02 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
////        DataHandler dataHandler02 = new DataHandler(new FileDataSource(file02));
////        attForm02.setFdAttachment(dataHandler02);
//        attForms.add(attForm01);
//        attForms.add(attForm02);
//
//        return attForms;
//    }
    /*
    static AttachmentForm[] createAllAtts() throws Exception {

        AttachmentForm[] attForms = new AttachmentForm[2];

        String fileName = "ucbug.com-NetDrive.zip";
//        AttachmentForm attForm01 = createAtt(fileName);
//        attForm01.setFdKey("fd_3897439d8696ac.0.fd_38974405db480c");
//        attForm01.setFdKey("fd_336b39b04689cc");
//        attForm01.setFdKey("fd_336b39b04689cc");
//        File file01 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
//        DataHandler dataHandler01 = new DataHandler(new FileDataSource(file01));
//        attForm01.setFdAttachment(dataHandler01);
        fileName = "commons-collections-3.2.2.jar";
        AttachmentForm attForm02 = createAtt(fileName);
        attForm02.setFdKey("fd_attr1");
//        File file02 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
//        DataHandler dataHandler02 = new DataHandler(new FileDataSource(file02));
//        attForm02.setFdAttachment(dataHandler02);
//        attForms[0] = attForm01;
        attForms[1] = attForm02;
//        attForms.add(attForm01);
//        attForms.add(attForm02);

        return attForms;
    }

    *//**
     * @Author: Longi.suwei
     * @Title: createAtt
     * @Description: TODO 创建附件对象
     * @param: @param fileName
     * @param: @return
     * @param: @throws Exception
     * @date: Jun 29, 2020 5:49:45 PM
     *//*
    static AttachmentForm createAtt(String fileName) throws Exception {
        AttachmentForm attForm = new AttachmentForm();
        attForm.setFdFileName(fileName);
        // 设置附件关键字，表单模式下为附件控件的id
        byte[] data = file2bytes("G:\\98_download\\" + fileName);
//        File file = new File("G:\\98_download\\" + fileName);

//        DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        attForm.setFdAttachment(data);
        return attForm;
    }

    *//**
     * 将文件转换为字节编码
     *//*
    static byte[] file2bytes(String fileName) throws Exception {
        InputStream in = new FileInputStream(fileName);
        byte[] data = new byte[in.available()];

        try {
            in.read(data);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }

        return data;
    }
*/

}
