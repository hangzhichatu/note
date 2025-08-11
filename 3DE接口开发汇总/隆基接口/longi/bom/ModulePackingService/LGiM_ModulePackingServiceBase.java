package longi.bom.ModulePackingService;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.util.ContextUtil;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class LGiM_ModulePackingServiceBase extends RestService {
    private final static Logger interfaceLogger = LoggerFactory.getLogger("module_interface");

    @POST // 调用方式
    @Path("/UpdateManagementProcessMessage")
    @Consumes({MediaType.APPLICATION_JSON}) // 输入格式
    @Produces({MediaType.APPLICATION_JSON}) // 输出格式
    public Response updateManagementProcessMessage(@Context HttpServletRequest request, @RequestBody String requestJson) throws Exception {
        System.out.println("UpdateManagementProcessMessage.." + requestJson);
        interfaceLogger.info("BPM包装方案流程请求入参：" + requestJson);
        matrix.db.Context context = ContextUtil.getAnonymousContext();
        JsonObjectBuilder respJsonObj = Json.createObjectBuilder();
        try {
            Map argsMap = new HashMap();
            argsMap.put("requestJson", requestJson);
            respJsonObj = (JsonObjectBuilder) JPO.invoke(context, "LGiM_ModulePacking", null, "updateProcess", JPO.packArgs(argsMap), JsonObjectBuilder.class);
            System.out.println("updateManagementProcessMessage..end : " + respJsonObj);
            interfaceLogger.info("BPM包装方案流程请求返回：" + respJsonObj.toString());
            return Response.status(200).entity(respJsonObj.build().toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            respJsonObj.add("msg", "BPM包装方案流程更新接口异常：" + e);
            interfaceLogger.info("BPM包装方案流程请求失败返回：" + respJsonObj.toString());
            return Response.status(500).entity(respJsonObj.build().toString()).build();
        }
    }

}
