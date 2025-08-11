package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;

import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.model.LONGiModuleBPMReturnBean;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
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

//For the traces

@Path("/updateRouteStatus")

public class LONGiModuleBPMRouteFeedBackBase extends RestService {
//    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleBPMRouteFeedBackBase.class);
    private final static Logger logger = LoggerFactory.getLogger("module_interface");

    public LONGiModuleBPMRouteFeedBackBase() {
    }

    /**
     * Gets the connected user.
     */
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateRouteStatus(@Context HttpServletRequest request, @RequestBody String routeActionContext)
            throws Exception {
        // For log information restFul
//        declareServiceName("/LONGiModulePDM/restFul/BPMRouteAction");
//https://dev.atoz.com/internal/resources/LONGiModulePDM/restFul/BPMRouteAction?routeActionContext={%22objectId%22:%221732d4644365f5ad441f4d54662bf9f0%22,%22operationType%22:%220%22,%22reasonsForRejection%22:%220%22}
        logger.info("接收到BPM更改受控单状态的接口请求，请求报文：" + routeActionContext);
//        matrix.db.Context context = LONGiModuleUserContext.getContext();
        matrix.db.Context context = ContextUtil.getAnonymousContext();
//        matrix.db.Context context = LONGiModuleUserContext.getContext();
//        matrix.db.Context context = ContextUtil.getAnonymousContext();
//        ContextUtil.pushContext(context);
        ContextUtil.pushContext(context, LONGiModuleConfig.CTX_USERNAME, LONGiModuleConfig.CTX_PASSWORD, "eService Production");
//        ContextUtil.pushContext(context);
        //ContextUtil.startTransaction(context, true);
        LONGiModuleBPMReturnBean bean = LONGiModuleJacksonUtil.jsonToBean(routeActionContext, LONGiModuleBPMReturnBean.class);
        logger.debug("转换Bean成功：" + bean);
        String typePattern = "LONGiModuleProjectDocumentControlledOrder";
        String vaultPattern = context.getVault().getName();
//        context.setVault("eService Production");
        String whereExpression = "attribute[LONGiModuleRelBPMRouteId]=='" + bean.getObjectId() + "'";
        logger.debug("搜索条件如下：whereExpression" + whereExpression);
        StringList objectSelects = new StringList();
        objectSelects.addElement("id");
        objectSelects.addElement("attribute[LONGiModuleRelBPMRouteId]");
        objectSelects.addElement("current");
//        "temp query bus * LONGiModuleProjectDocumentControlledOrder * where attribute[LONGiModuleRelBPMRouteId]==17400b316a2ba377482055b437e80d34;";
        short limit = 0;
        StringList orderBys = new StringList();
        //Build the answer
        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
        try {
            MapList orderList = DomainObject.findObjects(context, typePattern, vaultPattern, whereExpression, objectSelects, limit, orderBys);
            logger.info("查询到对应的受控单数据：" + orderList);
            for (Iterator iterator = orderList.iterator(); iterator.hasNext(); ) {
                Map next = (Map) iterator.next();
                String oderId = next.get("id").toString();
                String current = next.get("current").toString();
                DomainObject orderObj = DomainObject.newInstance(context, oderId);
                orderObj.open(context);

                if (current.equals("Check")) {
                    if (bean.getOperationType().equals("0")) {
                        orderObj.promote(context);
                    } else {
                        orderObj.demote(context);
                    }
                    jObjBuilder.add("returnState", "0");
                    jObjBuilder.add("message", "");
                } else {
                    jObjBuilder.add("returnState", "1");
                    jObjBuilder.add("message", "当前受控单状态不是审核状态");
                }
            }
            if (orderList == null || orderList.size() == 0) {
                jObjBuilder.add("returnState", "1");
                jObjBuilder.add("message", "对应BPM-id:'" + bean.getObjectId() + "'的受控单不存在");
            }
            logger.info("返回给BPM接口数据：" + jObjBuilder.toString());
            ContextUtil.popContext(context);
            return Response.status(200).entity(jObjBuilder.build().toString()).build();
        } catch (Exception e) {
            jObjBuilder.add("returnState", "1");
            jObjBuilder.add("message", "处理对应BPM-id:'" + bean.getObjectId() + "'的受控单失败,PDM系统内部错误,错误日志:" + e.toString());

            logger.error("接口处理异常：" + e.toString() + "" + "返回给BPM数据：" + jObjBuilder.build().toString());
            //ContextUtil.abortTransaction(context);
            return Response.status(500).entity(jObjBuilder.build().toString()).build();

            //            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // Pass always here before previous try/catch


//        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
//        jObjBuilder.add("returnState", "1");
//        return Response.status(200).entity(jObjBuilder.build().toString()).build();
    }


}
