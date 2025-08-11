package longi.bpm.BPMReturnProcessService;

import com.alibaba.fastjson.JSONObject;

import com.matrixone.apps.domain.util.ContextUtil;
import longi.bpm.service.impl.LGiM_IntergrationServiceimpl;
import longi.bpm.util.LGiM_ContextUtil;
import longi.bpm.util.LGiM_IntegrationUtil;
import matrix.db.JPO;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import java.util.Date;
@Path("/plm")
public class LGiM_BPMReturnProcessServiceBase extends LGiM_IntegrationUtil {
    private LGiM_IntergrationServiceimpl service = new LGiM_IntergrationServiceimpl();
    public LGiM_BPMReturnProcessServiceBase() {
    }

    @POST
    @Path(value = "/testConnect")
    //测试连通
    public Response testConnect(@javax.ws.rs.core.Context HttpServletRequest request,String dataJson) throws Exception{
        try{
            String retrun = "测试连通成功，输入数据："+"\\n"+dataJson;

            JSONObject resultJSON = new JSONObject();
            resultJSON.put("Code","200");
            resultJSON.put("Messange",retrun);
            return Response.status(200).entity(resultJSON).build();
        }catch (Exception e){
            e.printStackTrace();
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("Code","100");
            resultJSON.put("Messange",e.getMessage());

            return Response.status(500).entity(resultJSON).build();
        }
    }

    @POST
    @Path(value = "/testConnect2")
    public Response testConnect2(@javax.ws.rs.core.Context HttpServletRequest request,String dataJson) throws Exception{
        JsonObjectBuilder resultJsonObj = Json.createObjectBuilder();
        try{
            logger = Logger.getLogger(LGiM_BPMReturnProcessServiceBase.class.getName());
            log(logger);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date startTime = new Date();
            String starTime = sdf.format(startTime);
            logger.info("测试调用接口2记录-start-{"+starTime+"}");

            matrix.db.Context context = ContextUtil.getAnonymousContext();
            //matrix.db.Context context = LGiM_ContextUtil.getContext();
            logger.info("测试传递参信息-json-{"+dataJson+"}");
            String jsonstr = service.getJPOServiceTest(context,dataJson);//
            logger.info("测试返参信息-json-{"+jsonstr+"}");
            Date endDate = new Date();
            String endTime = sdf.format(endDate);

            long interval = (endDate.getTime()-startTime.getTime())/1000;
            logger.info("测试审批结果接口-end-{"+endTime+"}");
            logger.info("测试审批结果接口-总共用时-{"+interval+"}");


            Map argsMap = new HashMap();
            argsMap.put("JsonStr",dataJson);
            resultJsonObj = (JsonObjectBuilder)JPO.invoke(context, "LGiM_BPMReturnProcessServiceJPO", null, "testUseJPO", JPO.packArgs(argsMap), JsonObjectBuilder.class);


            return Response.status(200).entity(resultJsonObj.build().toString()).build();

        }catch (Exception e){
            e.printStackTrace();
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("Code","100");
            resultJSON.put("Messange",e.getMessage());
            return Response.status(500).entity(resultJSON).build();
        }
    }


    @POST
    @Path(value = "/RetrunToPLM")
    public Response RetrunToPLM(@javax.ws.rs.core.Context HttpServletRequest request,String dataJson) throws Exception{
        JsonObjectBuilder resultJsonObj = Json.createObjectBuilder();
        try{
            logger = Logger.getLogger(LGiM_BPMReturnProcessServiceBase.class.getName());
            log(logger);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date startTime = new Date();
            String starTime = sdf.format(startTime);
            logger.info("BPM调用接口记录-start-{"+starTime+"}");

            matrix.db.Context context = LGiM_ContextUtil.getContext();
            logger.info("BPM传递参信息-json-{"+dataJson+"}");
            String jsonstr = service.getJPOServiceTest(context,dataJson);//
            logger.info("BPM返参信息-json-{"+jsonstr+"}");
            Date endDate = new Date();
            String endTime = sdf.format(endDate);

            Map argsMap = new HashMap();
            argsMap.put("JsonStr",dataJson);
            resultJsonObj = (JsonObjectBuilder)JPO.invoke(context, "LGiM_BPMReturnProcessServiceJPO", null, "BPM_ReturnRouteResultToPLM", JPO.packArgs(argsMap), JsonObjectBuilder.class);

            long interval = (endDate.getTime()-startTime.getTime())/1000;
            logger.info("BPM审批结果接口-end-{"+endTime+"}");
            logger.info("BPM审批结果接口-总共用时-{"+interval+"}");

            return Response.status(200).entity(resultJsonObj.build().toString()).build();
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);


            e.printStackTrace(pw);
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("Code","500");
            resultJSON.put("Messange",sw.toString());
            return Response.status(500).entity(resultJSON.toString()).build();
        }
    }

    @POST
    @Path(value = "/CreateCodeReleaseForm")
    public Response CreateCodeReleaseForm(@javax.ws.rs.core.Context HttpServletRequest request,String dataJson) throws Exception{
        JsonObjectBuilder resultJsonObj = Json.createObjectBuilder();
        try{
            logger = Logger.getLogger(LGiM_BPMReturnProcessServiceBase.class.getName());
            log(logger);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date startTime = new Date();
            String starTime = sdf.format(startTime);
            logger.info("BPM发布表单创建接口记录-start-{"+starTime+"}");

            matrix.db.Context context = LGiM_ContextUtil.getContext();
            logger.info("BPM发布表单创建接传递参信息-json-{"+dataJson+"}");
            //String jsonstr = service.getJPOServiceTest(context,dataJson);//

            Date endDate = new Date();
            String endTime = sdf.format(endDate);

            Map argsMap = new HashMap();
            argsMap.put("JsonStr",dataJson);
            resultJsonObj = (JsonObjectBuilder)JPO.invoke(context, "LGiM_BPMReturnProcessServiceJPO", null, "BPM_CreateCodeReleaseForm", JPO.packArgs(argsMap), JsonObjectBuilder.class);
            logger.info("BPM发布表单创建接返参信息-json-{"+resultJsonObj.toString()+"}");
            long interval = (endDate.getTime()-startTime.getTime())/1000;
            logger.info("BPM审批结果接口-end-{"+endTime+"}");
            logger.info("BPM审批结果接口-总共用时-{"+interval+"}");

            return Response.status(200).entity(resultJsonObj.build().toString()).build();
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("Code","500");
            resultJSON.put("Messange",sw.toString());
            return Response.status(500).entity(resultJSON.toString()).build();
        }
    }
}
