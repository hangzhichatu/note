package longi.module.pdm.util;


//import com.webxml.QqOnlineWebService;
//import com.webxml.QqOnlineWebServiceSoap;
public class LONGiModuleWebServiceUtils {
	
	public static void main(String[] args) {
//		QqOnlineWebService ser= new QqOnlineWebService();
//		QqOnlineWebServiceSoap serSoap = ser.getQqOnlineWebServiceSoap();
//		String statuString = serSoap.qqCheckOnline("394993601");
//        System.out.println(statuString);
		
//		WebService cfg = null;
//		
//		  WebService cfg = WebServiceConfig.getInstance();
//		  ISysNotifyTodoWebService service = (ISysNotifyTodoWebService)callService(cfg.getAddress(), cfg.getServiceClass());
//		  // 请在此处添加业务代码
//		  NotifyTodoSendContext context = new NotifyTodoSendContext();
//		  context.setAppName("待办来源");
//		  context.setModelName("模块名");
//		  context.setSubject("测试待办webservice~~~");
//		  context.setLink("http://news.sina.com.cn/");
//		  context.setType(2);
//		  context.setKey("sinaNews");
//		  context.setModelId("123456789");
//		  // 待办对应接收人，数据格式为JSON，格式描述请查看"《2.1 组织架构数据说明》"
//		  context.setTargets("{\"PersonNo\":\"001\"}");
//		  context.setCreateTime("2012-03-22 09:25:09");
//		  JSONObject nodeJson = new JSONObject();
//		  nodeJson.accumulate("key", "lbpmCurrNode");
//		  nodeJson.accumulate("titleMsgKey","sys-lbpmservice:lbpmSupport.STATUS_RUNNING");
//		  nodeJson.accumulate("value", ((LbpmNode) taskInstance.getFdNode()).getFdFactNodeName());
//		  JSONArray ctxExt = new JSONArray();
//		  ctxExt.add(nodeJson);
//		  context.setExtendContent(ctxExt.toString());
//		  NotifyTodoAppResult result = service.sendTodo(context);
//		  if (result != null) {
//		    if (result.getReturnState() != 2)
//		      System.out.println(result.getMessage());
//		  }
	}
	
	/**
	* 调用服务，生成客户端的服务代理
	*
	* @param address WebService的URL
	* @param serviceClass 服务接口全名
	* @return 服务代理对象
	* @throws Exception
	*/
//	public static Object callService(String address, Class serviceClass)
//	throws Exception {
//	  JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
//	  // 记录入站消息
//	  factory.getInInterceptors().add(new LoggingInInterceptor());
//	  // 记录出站消息
//	  factory.getOutInterceptors().add(new LoggingOutInterceptor());
//	  // 添加消息头验证信息。如果服务端要求验证用户密码，请加入此段代码
//	  // factory.getOutInterceptors().add(new AddSoapHeader());
//	  factory.setServiceClass(serviceClass);
//	  factory.setAddress(address);
//	  // 使用MTOM编码处理消息。如果需要在消息中传输文档附件等二进制内容，请加入此段代码
//	  // Map props = new HashMap();
//	  // props.put("mtom-enabled", Boolean.TRUE);
//	  // factory.setProperties(props);
//	  // 创建服务代理并返回
//	  return factory.create();
//	}
}
