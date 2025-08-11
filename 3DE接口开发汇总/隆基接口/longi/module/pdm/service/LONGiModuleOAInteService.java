package longi.module.pdm.service;

import java.lang.Exception;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.program.ProgramCentralUtil;

import longi.module.pdm.client.todo.ISysNotifyTodoWebServiceServiceSoapBindingStub;
import longi.module.pdm.client.todo.NotifyTodoAppResult;
import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiModuleToConnectionUtil;
import matrix.db.Context;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @ClassName: LONGiModuleOAInteService
 * @Description: TODO OA接口的实现类
 * @author: Longi.suwei
 * @date: Jun 29, 2020 5:48:36 PM
 */
public class LONGiModuleOAInteService {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleOAInteService.class);
    private final static Logger interfaceLogger = LoggerFactory.getLogger("module_interface");

    
    
    //    public static String axisWsdl = "http://portaltest.longi-silicon.com:8080/sys/webservice/sysNotifyTodoWebService?wsdl";
//    public static org.apache.axis.client.Service service = new org.apache.axis.client.Service();
    public static ISysNotifyTodoWebServiceServiceSoapBindingStub stub;
    public static String actionType = "";
//    private static void stubInit() throws Exception {
//        // TODO Auto-generated method stub
//        try {
//            stub = new ISysNotifyTodoWebServiceServiceSoapBindingStub(
//                    new java.net.URL(axisWsdl), service);
//            // 有些webservice需要登录，登陆后才能进行一些操作，这个需要设置如下两个参数：
//            // 添加用户名信息
//            SOAPHeaderElement header = new SOAPHeaderElement("http://sys.webservice.client","tns:RequestSOAPHeader");
//            SOAPElement soap = null;
//            soap = header.addChildElement("tns:user");
//            soap.addTextNode("PDM");
//            soap = header.addChildElement("tns:password");
//            soap.addTextNode("0ab4e1b53bd9f687b0582d69ef783f1a");
//            stub.setHeader(header);
//            //1、 超时时间
//            stub.setTimeout(1000 * 60 * 20);
//            //2、 次数设置true，登录后才能保持登录状态，否则第二次调用ws方法时仍然会提示未登录。
//            stub.setMaintainSession(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception(e);
//        }
//    }


    /**
     * @Author: Longi.suwei
     * @Title: sendTodo
     * @Description: TODO 创建待办的接口实现方法
     * @param: @param context
     * @param: @param toDoBean
     * @param: @param string
     * @param: @param id
     * @param: @return
     * @date: Jun 29, 2020 5:47:29 PM
     */
    public static String sendTodo(Context context, longi.module.pdm.client.todo.NotifyTodoSendContext toDoBean, String string, String id) {

        // TODO Auto-generated method stub
        actionType = "sendTodo";
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        toDoBean.setAppName(LONGiModuleBPMConfig.SERVER_APP);
        toDoBean.setModelName(LONGiModuleBPMConfig.SERVER_MODULE);
        toDoBean.setCreateTime(ft.format(date));
        System.out.println("传递的JSON是:" + LONGiModuleFastJsonUtil.beanToJson(toDoBean));
        interfaceLogger.info("OA接口发送报文:"+LONGiModuleFastJsonUtil.beanToJson(toDoBean));
        try {
            stub = LONGiModuleToConnectionUtil.getToService();
            NotifyTodoAppResult result = stub.sendTodo(toDoBean);
            System.out.println("异常返回的结果是" + result.getMessage());
            interfaceLogger.info("OA接口接收报文:" + result.getMessage());
            logger.info("异常返回的结果是" + result.getMessage());
            
            
            if (result != null) {
                logger.info("异常状态编号" + result.getReturnState());
                if (result.getReturnState() != 2 || (result.getMessage()!=null && result.getMessage().equals("(500)Internal Server Error"))) {
                    createExceptionOAToDoInteranl(context, LONGiModuleFastJsonUtil.beanToJson(toDoBean), string, id, Integer.toString(result.getReturnState()), result.getMessage());
                }
            }
//        String response = stub.urgeWorkOrderServiceSheet(requestStr); //调用ws提供的方法
        } catch (Exception e) {
            logger.error("待办待阅创建失败：" + e.toString());
            createExceptionOAToDo(context, LONGiModuleFastJsonUtil.beanToJson(toDoBean), string, id, e);
            e.printStackTrace();
        }
        return "待办已处理";
    }

    /**
     * @Author: Longi.suwei
     * @Title: sendTodoDone
     * @Description: TODO 待办完成的接口实现方法
     * @param: @param context
     * @param: @param toDoBean
     * @param: @param string
     * @param: @param id
     * @param: @return
     * @date: Jun 29, 2020 5:48:01 PM
     */
    public static String sendTodoDone(Context context, NotifyTodoRemoveContext toDoBean, String string, String id) {
        actionType = "sendTodoDone";

        try {
            stub = LONGiModuleToConnectionUtil.getToService();
            toDoBean.setAppName(LONGiModuleBPMConfig.SERVER_APP);
            toDoBean.setModelName(LONGiModuleBPMConfig.SERVER_MODULE);
            System.out.println(LONGiModuleFastJsonUtil.beanToJson(toDoBean));
            NotifyTodoAppResult result = stub.setTodoDone(toDoBean);
            if (result != null) {
                logger.info("异常返回的结果是" + result.getMessage());
                if (result.getReturnState() != 2 || (result.getMessage()!=null && result.getMessage().equals("(500)Internal Server Error"))) {
                    logger.info("异常返回的状态是" + result.getReturnState());
                    if (result.getMessage().toString().equals("未找到对应的待办信息，请检查数据!") && actionType.equals("sendTodoDone")) {
                        logger.info("异常待办TodoDone数据，设置待办为已办的错误，待修复，目前先屏蔽：" + LONGiModuleFastJsonUtil.beanToJson(toDoBean));
                    } else {
                        createExceptionOAToDoInteranl(context, LONGiModuleFastJsonUtil.beanToJson(toDoBean), string, id, Integer.toString(result.getReturnState()), result.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("待办完成失败：" + e.toString());
            e.printStackTrace();
            createExceptionOAToDo(context, LONGiModuleFastJsonUtil.beanToJson(toDoBean), string, id, e);
        }
        return "设置待办为已办已处理";
    }

    /**
     * @Author: Longi.suwei
     * @Title: deleteTodo
     * @Description: TODO 删除待办的接口实现方法
     * @param: @param context
     * @param: @param removeBean
     * @param: @param string
     * @param: @param id
     * @param: @return
     * @date: Jun 29, 2020 5:48:21 PM
     */
    public static String deleteTodo(Context context, NotifyTodoRemoveContext removeBean, String string, String id) {
        actionType = "deleteTodo";
        try {
            stub = LONGiModuleToConnectionUtil.getToService();
            removeBean.setAppName(LONGiModuleBPMConfig.SERVER_APP);
            removeBean.setModelName(LONGiModuleBPMConfig.SERVER_MODULE);
            System.out.println(LONGiModuleFastJsonUtil.beanToJson(removeBean));
            NotifyTodoAppResult result = stub.deleteTodo(removeBean);
            System.out.println("result=" + result.getMessage());
            if (result != null) {
                if (result.getReturnState() != 2 || (result.getMessage()!=null && result.getMessage().equals("(500)Internal Server Error"))) {
                    logger.info("deleteTodo异常返回的结果是" + result.getMessage());

                    if (result.getMessage().toString().equals("未找到对应的待办信息，请检查数据!")) {
                        logger.info("异常待办删除待办数据，待后续优化，目前先不创建队列：" + LONGiModuleFastJsonUtil.beanToJson(removeBean));
                    } else {
                        createExceptionOAToDoInteranl(context, LONGiModuleFastJsonUtil.beanToJson(removeBean), string, id, Integer.toString(result.getReturnState()), result.getMessage());
                    }


                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            //忽略删除的异常
            logger.error("删除待办待阅失败：" + e.toString());
            if (e.toString().equals("未找到对应的待办信息，请检查数据!") && actionType.equals("sendTodoDone")) {
                logger.info("异常待办数据，设置待办为已办的错误，待修复，目前先屏蔽：" + LONGiModuleFastJsonUtil.beanToJson(removeBean));
            } else {
                createExceptionOAToDo(context, LONGiModuleFastJsonUtil.beanToJson(removeBean), string, id, e);
            }
        }
        return "删除待办已处理";

    }

    private static void createExceptionOAToDoInteranl(Context context, String beanToJson, String string, String id, String state, String resultMessage) {
        //创建OA待办队列对象
        DomainObject obj = null;
        try {
            logger.info("创建OA待办队列对象:" + beanToJson);
            ContextUtil.pushContext(context);
            String newId = FrameworkUtil.autoName(context, "type_LONGiOAInteToDoQueue", "policy_LONGiOAInteToDoQueue");
            logger.info("新异常队列数据 newId=" + newId);
            obj = DomainObject.newInstance(context, newId);
            logger.info("obj=" + obj);
            obj.setDescription(context, beanToJson);

            if(!actionType.equals("deleteTodo")){
                if (string.equals("relId")) {
                    String mqlStr = "add connection LONGiQueueItems from " + newId + " torel " + id;
                    logger.info("rel mqlStr=" + mqlStr);
                    //add connection LONGiQueueItems from 21847.44790.55124.25310 torel 21847.44790.37495.18565
                    String res = MqlUtil.mqlCommand(context, false, mqlStr, false);
                    logger.info("res=" + res);
                } else {

                    DomainObject newSeq = DomainObject.newInstance(context,newId);
                    logger.info("队列对象：" + newSeq);
                    DomainObject codeObj = DomainObject.newInstance(context,id);
                    logger.info("申请单对象：" + codeObj);
                    DomainRelationship.connect(context, newSeq, new RelationshipType("LONGiQueueItems"), codeObj);
    //                String mqlStr = "add connection LONGiQueueItems from " + newId + " to " + id;
                    logger.info("关系关联成功");
    //                String res = MqlUtil.mqlCommand(context, false, mqlStr, false);
    //                logger.info("res=" + res);
                }
            }
            obj.setAttributeValue(context, "Title", actionType);
            obj.setAttributeValue(context, "LONGiModuleOAInteErrorState", state);
            obj.setAttributeValue(context, "LONGiModuleOAInteErrorReason", resultMessage);
            ContextUtil.popContext(context);
        } catch (FrameworkException fe) {
            logger.error("创建待办异常队列失败："+fe.toString());
        }
    }

    private static void createExceptionOAToDo(Context context, String bean, String string, String id, Exception e) {
        //创建OA待办队列对象
        String state = "";
        String errorMessage = "";
        //异常分类,便于重发
        Throwable cause = e.getCause();

        if (cause.getMessage().indexOf("Connection refused: connect") > 0) {
            state = "E1";
            errorMessage = "连接异常";
            logger.info("连接异常");
        } else if (cause.getMessage().indexOf("Read timed out") > 0) {
            state = "E2";
            errorMessage = "响应超时";
            logger.info("响应超时");
        } else if (cause.getMessage().indexOf("connect timed out") > 0) {
            state = "E3";
            errorMessage = "连接超时";
            logger.info("连接超时");
        } else if(cause.getMessage().indexOf("Internal Server Error") > 0){
            state = "E4";
            errorMessage = "OA服务器程序异常";
            logger.info("OA服务器程序异常");
        }
        else {
            state = "EE";
            errorMessage = "处理异常";
            logger.info("处理异常");
        }


        createExceptionOAToDoInteranl(context, bean, string, id, state, errorMessage + ":" + e.toString());
    }

    public static void reSendTodo(Context context) {
        try {
            stub = LONGiModuleToConnectionUtil.getToService();
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause.getMessage().indexOf("Connection refused: connect") > 0 || cause.getMessage().indexOf("Read timed out") > 0 || cause.getMessage().indexOf("connect timed out") > 0) {
                logger.debug("服务器仍然未恢复,停止继续重新发送待办");
                return;
            }
        }
//        if (stub == null) {
//
//                stubInit();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Throwable cause = e.getCause();
//                if (cause.getMessage().indexOf("Connection refused: connect") > 0 || cause.getMessage().indexOf("Read timed out") > 0 || cause.getMessage().indexOf("connect timed out") > 0) {
//                    System.out.println("服务器仍然未恢复,停止继续重新发送待办");
//                    return;
//                }
//            }
//        }
        //创建OA待办队列对象
        String typePattern = "LONGiOAInteToDoQueue";
        String vaultPattern = context.getVault().getName();
        String whereExpression = "current=='InActive' && attribute[LONGiModuleOAInteErrorState]!='1'";
        StringList objectSelects = new StringList();
        objectSelects.addElement("name");
        objectSelects.addElement("id");
        objectSelects.addElement("current");
        objectSelects.addElement("attribute[Title]");
        objectSelects.addElement("attribute[LONGiModuleOAInteErrorReason]");
        objectSelects.addElement("attribute[LONGiModuleOAInteErrorState]");
        objectSelects.addElement("description");
        short limit = 0;
        StringList orderBys = new StringList();
        orderBys.addElement("name");
        MapList queueList = null;
        try {
            queueList = DomainObject.findObjects(context, typePattern, vaultPattern, whereExpression, objectSelects, limit, orderBys);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }

        NotifyTodoAppResult result = null;
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < queueList.size(); i++) {
            Map queueMap = (Map) queueList.get(i);
            String id = queueMap.get("id").toString();
            String errorState = queueMap.get("attribute[LONGiModuleOAInteErrorState]").toString();
            String title = queueMap.get("attribute[Title]").toString();
            String description = queueMap.get("description").toString();
            DomainObject obj = null;
            try {
                obj = DomainObject.newInstance(context, id);
            } catch (FrameworkException e) {
                e.printStackTrace();
            }
            if (ProgramCentralUtil.isNotNullString(title) && !errorState.equals("1")) {
                try {
                    if (title.equals("sendTodo")) {
//                        result = service.sendTodo((NotifyTodoSendContext) LONGiModuleJSONUtils.jsonToBean(description, NotifyTodoSendContext.class));
                        result = stub.sendTodo((NotifyTodoSendContext) LONGiModuleJacksonUtil.jsonToBean(description, NotifyTodoSendContext.class));

                    } else if (title.equals("sendTodoDone")) {
                        result = stub.setTodoDone((NotifyTodoRemoveContext) LONGiModuleJacksonUtil.jsonToBean(description, NotifyTodoRemoveContext.class));
                    } else {
                        result = stub.deleteTodo((NotifyTodoRemoveContext) LONGiModuleJacksonUtil.jsonToBean(description, NotifyTodoRemoveContext.class));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Throwable cause = e.getCause();
                    if (cause.getMessage().indexOf("Connection refused: connect") > 0 || cause.getMessage().indexOf("Read timed out") > 0 || cause.getMessage().indexOf("connect timed out") > 0) {
                        System.out.println("服务器仍然未恢复,停止继续重新发送待办");
                        return;
                    }
                }
                if (result != null) {
                    if (result.getReturnState() != 2) {
                        System.out.println("异常返回的结果是" + description + "\n" + result.getMessage());
                        try {
                            obj.setAttributeValue(context, "LONGiModuleOAInteErrorReason", result.getMessage());
                        } catch (FrameworkException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            obj.promote(context);
                        } catch (MatrixException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

//    /**
//     * 调用服务，生成客户端的服务代理
//     *
//     * @param address      WebService的URL
//     * @param serviceClass 服务接口全名
//     * @return 服务代理对象
//     * @throws Exception
//     */
//    public static Object callService(String address, Class serviceClass)
//            throws Exception {
//
//
//        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
//
//        // 记录入站消息
//        factory.getInInterceptors().add(new LoggingInInterceptor());
//        // 记录出站消息
//        factory.getOutInterceptors().add(new LoggingOutInterceptor());
//        // 添加消息头验证信息。如果服务端要求验证用户密码，请加入此段代码
////        factory.getOutInterceptors().add(new LONGiModuleBPMAddSoapHeader());
//        factory.setServiceClass(serviceClass);
//        factory.setAddress(address);
//
//        // 使用MTOM编码处理消息。如果需要在消息中传输文档附件等二进制内容，请加入此段代码
//        // Map<String, Object> props = new HashMap<String, Object>();
//        // props.put("mtom-enabled", Boolean.TRUE);
//        // factory.setProperties(props);
//
//        // 创建服务代理并返回
//        return factory.create();
//    }
}
