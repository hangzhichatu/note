package longi.module.pdm.restService;


import com.dassault_systemes.platform.restServices.RestService;
import com.dassault_systemes.smaslm.matrix.common.json.JSONObject;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;

import longi.module.pdm.constants.LONGiModuleConfig;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;


@Path("/processOperation")

public class LGiM_FMSRouteFeedBackBase extends RestService {
    private final static Logger logger = LoggerFactory.getLogger("module_interface");

    public LGiM_FMSRouteFeedBackBase() {
    }

    /**
     * Gets the connected user.
     */
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateRouteStatus(@Context HttpServletRequest request, @RequestBody String routeActionContext)
            throws Exception {
    	System.out.println("接收到FMS更改受控单状态的接口请求，请求报文：>>>======="+routeActionContext);
        logger.info("接收到FMS更改受控单状态的接口请求，请求报文：" + routeActionContext);
        matrix.db.Context context = ContextUtil.getAnonymousContext();
    	JSONObject jsonObj = new JSONObject(routeActionContext);
    	String strFMSProcessInstID=jsonObj.getString("ProcessInstID");
    	String strOperation=jsonObj.getString("OperationType");
        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
        ContextUtil.pushContext(context, LONGiModuleConfig.CTX_USERNAME, LONGiModuleConfig.CTX_PASSWORD, "eService Production");
        try {
        
        	if(UIUtil.isNullOrEmpty(strFMSProcessInstID)){
        		 jObjBuilder.add("returnState", "1");
                 jObjBuilder.add("message", "受控流程ID不能为空");
                 return Response.status(200).entity(jObjBuilder.build().toString()).build();
        	}else {
        		StringList objectSelects = new StringList();
                objectSelects.addElement("id");
                objectSelects.addElement("current");
                //add by qubibo start
                objectSelects.addElement("type");
                //add by qubibo end
                MapList mlFMSOrdr = DomainObject.findObjects(context,
                        //add by qubibo start
                        "LGiM_FMSOrder,LGiM_CodeFMSOrder",
                        //add by qubibo end
                        "*",
                        "attribute[LGiM_FMSProcessInstID].value=='"+strFMSProcessInstID+"'",
                        objectSelects );
//                System.out.println("mlFMSOrdr------------->"+mlFMSOrdr);
            	if(mlFMSOrdr.size()>0) {
            	       for (Iterator iterator = mlFMSOrdr.iterator(); iterator.hasNext(); ) {
                           Map next = (Map) iterator.next();
                           String oderId = next.get("id").toString();
                           String current = next.get("current").toString();
                           DomainObject orderObj = DomainObject.newInstance(context, oderId);
                           //modify by qubibo 20230201 start
                           String strType = next.get("type").toString();
                          // if (current.equals("Check")) {
                           if (current.equals("Check") || ("LGiM_CodeFMSOrder".equals(strType) && "Review".equals(current))) {
                            //modify by qubibo 20230201 end
                               if("0".equals(strOperation)){
                        		   String strItemID=jsonObj.getString("ItemID");
                        		   String strDocNo=jsonObj.getString("DocNo");
                        		   if(UIUtil.isNotNullAndNotEmpty(strItemID)||UIUtil.isNotNullAndNotEmpty(strDocNo)){
                        			   orderObj.setAttributeValue(context, "LGiM_FMSItemID", strItemID);
                        			   orderObj.setAttributeValue(context, "LGiM_FMSDocNo", strDocNo);

                                       //modify by qubibo 20230201 start
                                       if("LGiM_CodeFMSOrder".equals(strType) ){
                                           //FMS审批标记为S,审批通过
                                           orderObj.setAttributeValue(context, "LGiM_SendToFMS", "S");
                                       }
                                       //modify by qubibo 20230201 end

                        			   orderObj.promote(context);                       			
                        			   jObjBuilder.add("returnState", "0");
                                       jObjBuilder.add("message", "操作成功");
                        		   }else {
                        			   jObjBuilder.add("returnState", "1");
                                       jObjBuilder.add("message", "ItemID或者DocNo为空");
                        		   }
                        	   }else {                       		   
                        		   //LGiM_FMSActivityInstID
                        		   //String strActivityInstID=jsonObj.getString("ActivityInstID");
                        		   //if(UIUtil.isNotNullAndNotEmpty(strActivityInstID)){
                                   //modify by qubibo 20230201 start
                                   if("LGiM_CodeFMSOrder".equals(strType) ){
                                       //FMS审批标记为N,审批被拒绝
                                       orderObj.setAttributeValue(context, "LGiM_SendToFMS", "N");
                                   }
                                   //modify by qubibo 20230201 end

                        			   orderObj.demote(context);
                        			   orderObj.setAttributeValue(context, "LGiM_FMSActivityInstID", "Y");
                        			   jObjBuilder.add("returnState", "0");
                                       jObjBuilder.add("message", "操作成功");
                        		   //}else {
                        			   //jObjBuilder.add("returnState", "1");
                                       //jObjBuilder.add("message", "ActivityInstID为空");
                        		   //}
                        		 
                        	   }                                                  
                              
                           } else {
                               jObjBuilder.add("returnState", "1");
                               jObjBuilder.add("message", "当前受控单状态不是审核状态");
                           }
                       }           		
            	}else {
            		  jObjBuilder.add("returnState", "1");
                      jObjBuilder.add("message", "对应受控流程ID:'" + strFMSProcessInstID + "'的受控单不存在");
            	}
                logger.info("返回给FMS接口数据：" + jObjBuilder.toString());
                return Response.status(200).entity(jObjBuilder.build().toString()).build();
        	}
        	
        } catch (Exception e) {
        	e.printStackTrace();
            jObjBuilder.add("returnState", "1");
            jObjBuilder.add("message", "对应受控流程ID:'" + strFMSProcessInstID + "'的受控单操作失败,PDM系统内部错误,错误日志:" + e.toString());
            logger.error("接口处理异常：" + e.toString() + "" + "返回给FMS数据：" + jObjBuilder.build().toString());
            
            return Response.status(500).entity(jObjBuilder.build().toString()).build();

        }finally {
        	ContextUtil.popContext(context);
        }

    }
}
