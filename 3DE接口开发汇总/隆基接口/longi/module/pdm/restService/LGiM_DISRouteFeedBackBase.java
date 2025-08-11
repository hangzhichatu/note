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

public class LGiM_DISRouteFeedBackBase extends RestService {
    private final static Logger logger = LoggerFactory.getLogger("module_interface");

    public LGiM_DISRouteFeedBackBase() {
    }

    /**
     * Gets the connected user.
     */
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateRouteStatus(@Context HttpServletRequest request, @RequestBody String routeActionContext)
            throws Exception {
        System.out.println(">>>=======" + routeActionContext);
        logger.info("接收到DIS更改受控单状态的接口请求，请求报文：" + routeActionContext);
        matrix.db.Context context = ContextUtil.getAnonymousContext();
        JSONObject jsonObj = new JSONObject(routeActionContext);
        String strLGiM_DISMappingID = jsonObj.getString("ProcessInstID");
        String strOperation = jsonObj.getString("OperationType");
        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
        ContextUtil.pushContext(context, LONGiModuleConfig.CTX_USERNAME, LONGiModuleConfig.CTX_PASSWORD, "eService Production");
        try {
            if (UIUtil.isNullOrEmpty(strLGiM_DISMappingID)) {
                jObjBuilder.add("returnState", "1");
                jObjBuilder.add("message", "受控流程ID不能为空");
                return Response.status(200).entity(jObjBuilder.build().toString()).build();
            } else {
                StringList objectSelects = new StringList();
                objectSelects.addElement("id");
                objectSelects.addElement("current");
                objectSelects.addElement("type");
                // 根据ID得到受控单
                MapList mlDISOrdr = DomainObject.findObjects(context,
                        "LGiM_FMSOrder,LGiM_CodeFMSOrder",
                        "*",
                        "attribute[LGiM_DISMappingID].value=='" + strLGiM_DISMappingID + "'",
                        objectSelects);
                if (mlDISOrdr.size() > 0) {
                    for (Iterator iterator = mlDISOrdr.iterator(); iterator.hasNext(); ) {
                        Map next = (Map) iterator.next();
                        String oderId = next.get("id").toString();
                        String current = next.get("current").toString();
                        DomainObject orderObj = DomainObject.newInstance(context, oderId);
                        String strType = next.get("type").toString();
                        String strSendToFMS = orderObj.getAttributeValue(context, "LGiM_SendToFMS");
                        logger.info("strSendToFMS=========:" + strSendToFMS);
                        // LGiM_FMSOrder（快速方案） LGiM_CodeFMSOrder（受控发布单）
                        if (current.equals("Check") || ("LGiM_CodeFMSOrder".equals(strType) && "Review".equals(current))) {
                            if ("0".equals(strOperation)) {
                                if ("LGiM_CodeFMSOrder".equals(strType)) {
                                    //FMS审批标记为S,审批通过
                                    orderObj.setAttributeValue(context, "LGiM_SendToFMS", "S");
                                }
                                orderObj.promote(context);
                                jObjBuilder.add("returnState", "0");
                                jObjBuilder.add("message", "操作成功");

                            } else {
                                if ("LGiM_CodeFMSOrder".equals(strType)) {
                                    //FMS审批标记为N,审批被拒绝
                                    orderObj.setAttributeValue(context, "LGiM_SendToFMS", "N");
                                }
                                orderObj.demote(context);
                                orderObj.setAttributeValue(context, "LGiM_FMSActivityInstID", "Y");

                                jObjBuilder.add("returnState", "0");
                                jObjBuilder.add("message", "操作成功");
                            }
                        } else {
                            jObjBuilder.add("returnState", "1");
                            jObjBuilder.add("message", "当前受控单状态不是审核状态");
                        }
                    }
                } else {
                    jObjBuilder.add("returnState", "1");
                    jObjBuilder.add("message", "对应受控流程ID:'" + strLGiM_DISMappingID + "'的受控单不存在");
                }
                logger.info("返回给DIS接口数据--->" + jObjBuilder.build().toString());
                return Response.status(200).entity(jObjBuilder.build().toString()).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            jObjBuilder.add("returnState", "1");
            jObjBuilder.add("message", "对应受控流程ID:'" + strLGiM_DISMappingID + "'的受控单操作失败,PDM系统内部错误,错误日志:" + e.toString());
            logger.error("接口处理异常：" + e.toString() + "" + "返回给DIS数据：" + jObjBuilder.build().toString());

            return Response.status(500).entity(jObjBuilder.build().toString()).build();

        }finally {
        	ContextUtil.popContext(context);
        }

    }
}
