package longi.module.pdm.restService;

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

import com.matrixone.apps.domain.util.ContextUtil;

import com.dassault_systemes.platform.restServices.RestService;

import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.JPO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/insert")
public class LGiM_ModuleGetOaBatteryInformationBase extends RestService {

    private final static Logger interfaceLogger = LoggerFactory.getLogger("module_interface");

    @POST // 调用方式
    @Consumes({MediaType.APPLICATION_JSON}) // 输入格式
    @Produces({MediaType.APPLICATION_JSON}) // 输出格式
    public Response postBatteryInformation(@Context HttpServletRequest request, @RequestBody String requestJson) throws Exception {
        return insertBatteryBenchmarkCode(request, requestJson);
    }

    @SuppressWarnings("unchecked")
    public Response insertBatteryBenchmarkCode(HttpServletRequest request, String strRequestContent) throws Exception {
        System.out.println("insertBatteryBenchmarkCode.." + strRequestContent);
        JsonObjectBuilder respJsonBuilder = Json.createObjectBuilder();
        if (UIUtil.isNullOrEmpty(strRequestContent)) {
            respJsonBuilder.add("returnState", "1");
            respJsonBuilder.add("message", "接收报文为空。");
            return Response.status(500).entity(respJsonBuilder.build().toString()).build();
        }
        matrix.db.Context context = ContextUtil.getAnonymousContext();
        try {
            Map argMap = new HashMap();
            argMap.put("RequestContent", strRequestContent);
            String[] args = JPO.packArgs(argMap);
            String strJpo = "LGiM_ModuleBatteryCode";
            String strMethod = "createOaBatteryFormData";
            Map dataMap = JPO.invoke(context, strJpo, null, strMethod, args, Map.class);
            String strErrorMessage = (String) dataMap.get("ErrorMessage");
            if (UIUtil.isNullOrEmpty(strErrorMessage)) {
                respJsonBuilder.add("returnState", "0");
                respJsonBuilder.add("message", "success");
                interfaceLogger.info("OA电池编码信息返回成功报文:" + respJsonBuilder.build().toString());
                return Response.status(200).entity(respJsonBuilder.build().toString()).build();
            } else {
                respJsonBuilder.add("returnState", "1");
                respJsonBuilder.add("message", strErrorMessage);
                interfaceLogger.info("OA电池编码信息返回失败报文:" + respJsonBuilder.build().toString());
                return Response.status(500).entity(respJsonBuilder.build().toString()).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            respJsonBuilder.add("returnState", "1");
            respJsonBuilder.add("message", "" + ex);
            interfaceLogger.info("OA电池编码信息返回失败报文:" + respJsonBuilder.build().toString());
            return Response.status(500).entity(respJsonBuilder.build().toString()).build();
        }

    }
}