package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.JPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName LGiM_ManufacturerBase
 * @Description
 * @Author lqt
 * @Date 2024/02/01
 **/
@Path("/insert")
public class LGiM_ManufacturerBase extends RestService {

    private final static Logger logger = LoggerFactory.getLogger("module_interface");

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response insert(@Context HttpServletRequest request, @RequestBody String strRequestContent)
            throws Exception {
        String strMessageId = request.getHeader("Message-Id");
        System.out.println("strMessageId>>>=======" + strMessageId);
        if (UIUtil.isNullOrEmpty(strMessageId)) {
            strMessageId = "";
        }
        JsonObjectBuilder respJsonBuilder = Json.createObjectBuilder();
        System.out.println(">>>=======" + strRequestContent);
        logger.info("接收到MDM创建供应商接口请求，MessageId：" + strMessageId);
        logger.info("请求报文：" + strRequestContent);
        if (UIUtil.isNullOrEmpty(strRequestContent)) {
            respJsonBuilder.add("status", "E");
            respJsonBuilder.add("message", "接收报文异常。");
            return Response.status(4000).entity(respJsonBuilder.build().toString()).build();
        }
        matrix.db.Context context = ContextUtil.getAnonymousContext();
        // JSONObject reqJson = JSON.parseObject(strRequestContent);
        try {
            Map argMap = new HashMap();
            argMap.put("RequestContent", strRequestContent);
            String[] args = JPO.packArgs(argMap);
            String strJpo = "LGiM_Manufacturer";
            String strMethod = "createManufacturer";
            ContextUtil.pushContext(context);
            JPO.invoke(context, strJpo, null, strMethod, args);
            respJsonBuilder.add("status", "S");
            respJsonBuilder.add("code", "200");
            respJsonBuilder.add("message", "创建成功");
            respJsonBuilder.add("messageId", strMessageId);
            respJsonBuilder.add("requeue", "false");
        } catch (Exception ex) {
            respJsonBuilder.add("status", "E");
            respJsonBuilder.add("code", "500");
            respJsonBuilder.add("message", "创建供应商发生异常,PDM系统内部错误,错误日志:" + ex.toString());
            respJsonBuilder.add("messageId", strMessageId);
            respJsonBuilder.add("requeue", "false");
            logger.error("接口处理异常：" + ex.toString() + "" + "返回给MDM报文：" + respJsonBuilder.build().toString());
            return Response.status(500).entity(respJsonBuilder.build().toString()).build();
        } finally {
            ContextUtil.popContext(context);
        }
        logger.info("接口处理成功，返回给MDM报文：" + respJsonBuilder.toString());
        return Response.status(200).entity(respJsonBuilder.build().toString()).build();
    }

}