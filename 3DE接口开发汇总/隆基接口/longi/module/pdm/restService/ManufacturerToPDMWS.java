package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;

import longi.module.pdm.model.ManufacturerRequestInfo;
import longi.module.pdm.model.ManufacturerResponseInfo;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import matrix.db.Context;
import matrix.db.JPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/Manufacturer")
public class ManufacturerToPDMWS extends RestService {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss:SSS";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    private final static Logger logger = LoggerFactory.getLogger(ManufacturerToPDMWS.class);
    private final static Logger interfaceLogger = LoggerFactory.getLogger("module_interface");
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response excution(@RequestBody ManufacturerRequestInfo requestInfo) {
//    public Response excution(@RequestBody String requestInfoStr) {
        System.out.println("Manufacturer------arg=" + requestInfo);
        interfaceLogger.info("制造商接口接收报文:"+com.alibaba.fastjson.JSON.toJSONString(requestInfo));
//        System.out.println("Manufacturer------arg=" + requestInfoStr);
//        ObjectMapper om = new ObjectMapper();
        Context context = null;
//        ManufacturerRequestInfo requestInfo = null;
        try {
//            requestInfo = om.readValue(requestInfoStr, ManufacturerRequestInfo.class);
            ManufacturerResponseInfo.ResultInfoBean resultInfoBean = new ManufacturerResponseInfo.ResultInfoBean();
            ManufacturerResponseInfo.EsbInfoBean esbInfoBean1 = new ManufacturerResponseInfo.EsbInfoBean();
            ManufacturerResponseInfo responseInfo = new ManufacturerResponseInfo();
            context = ContextUtil.getAnonymousContext();
            ContextUtil.pushContext(context);
            ManufacturerRequestInfo.EsbInfoBean esbInfoBean = requestInfo.getEsbInfo();
            String instId = esbInfoBean.getInstId();
            String resTime = esbInfoBean.getRequestTime();
            ManufacturerRequestInfo.RequestInfoBean requestInfoBean = requestInfo.getRequestInfo();
            ManufacturerRequestInfo.RequestInfoBean.ProClustersBean proClustersBean = requestInfoBean.getProClusters();
            List<ManufacturerRequestInfo.RequestInfoBean.ProClustersBean.ProClusterBean> proClusterBeans =
                    proClustersBean.getProCluster();
            MapList mapList = new MapList();
            if (Objects.nonNull(proClusterBeans) && !proClusterBeans.isEmpty()) {
                for (ManufacturerRequestInfo.RequestInfoBean.ProClustersBean.ProClusterBean proClusterBean : proClusterBeans) {
                    Map proClusterBeanMap = new HashMap();
                    String Manufacturer_ID = proClusterBean.getManufacturerID();
                    String Manufacturer_NAME = proClusterBean.getManufacturerNAME();
                    String Description = proClusterBean.getDescription();
                    proClusterBeanMap.put("Manufacturer_ID", Manufacturer_ID);
                    proClusterBeanMap.put("Manufacturer_NAME", Manufacturer_NAME);
                    proClusterBeanMap.put("Description", Description);
                    mapList.add(proClusterBeanMap);
                }
                Map argMap = new HashMap();
                argMap.put("arg", mapList);
                String[] args = JPO.packArgs(argMap);
                String jpo = "LONGiModulePDM2ERPInterface";
                String method = "createManufacturer";
                String result = JPO.invoke(context, jpo, null, method, args, String.class);
                String returnMsg = "";
                String returnStatus = "";
                String returnCode = "";
                if (UIUtil.isNotNullAndNotEmpty(result)) {
                    returnMsg = result;
                    returnStatus = "E";
                    returnCode = "E0002";
                }  else {
                    returnStatus = "S";
                    returnCode = "A0001";
                    returnMsg = "\u521B\u5EFA\u6210\u529F\uFF01";
                }
                String returnTime = simpleDateFormat.format(new Date());
                //esbInfoBean1
                esbInfoBean1.setInstId(instId);
                esbInfoBean1.setReturnStatus(returnStatus);
                esbInfoBean1.setReturnCode(returnCode);
                esbInfoBean1.setReturnMsg(returnMsg);
                esbInfoBean1.setRequestTime(resTime);
                esbInfoBean1.setResponseTime(returnTime);
                //resultInfoBean
                resultInfoBean.setReturnStatus(returnStatus);
                resultInfoBean.setReturnCode(returnCode);
                resultInfoBean.setReturnMsg(returnMsg);
            } else {
                String returnTime = simpleDateFormat.format(new Date());
                esbInfoBean1.setInstId(instId);
                esbInfoBean1.setReturnStatus("E");
                esbInfoBean1.setReturnCode("E0001");
                esbInfoBean1.setReturnMsg("\u53C2\u6570\u4E3A\u7A7A\u503C\uFF01");
                esbInfoBean1.setRequestTime(resTime);
                esbInfoBean1.setResponseTime(returnTime);
                resultInfoBean.setReturnStatus("E");
                resultInfoBean.setReturnCode("E0001");
                resultInfoBean.setReturnMsg("\u53C2\u6570\u4E3A\u7A7A\u503C\uFF01");
            }
            responseInfo.setEsbInfo(esbInfoBean1);
            responseInfo.setResultInfo(resultInfoBean);
            interfaceLogger.info("制造商接口返回报文:"+com.alibaba.fastjson.JSON.toJSONString(responseInfo));
            return Response.ok(responseInfo).cacheControl(CacheControl.valueOf("no-cache")).build();
        } catch (Exception e) {
            e.printStackTrace();
            interfaceLogger.info("制造商接口程序异常:"+e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
            	interfaceLogger.info("finally 制造商接口程序异常:"+e.getMessage());
            }
        }
    }
}
