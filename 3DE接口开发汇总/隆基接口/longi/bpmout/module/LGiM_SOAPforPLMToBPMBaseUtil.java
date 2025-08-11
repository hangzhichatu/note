package longi.bpmout.module;

import com.alibaba.fastjson.JSONObject;
import longi.module.pdm.client.route.KmReviewParamterForm;
import longi.module.pdm.service.LONGiModuleBPMInteService;
import matrix.db.Context;

import javax.xml.soap.SOAPException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class LGiM_SOAPforPLMToBPMBaseUtil {
    public void connect2(Context context,String[] args) throws MalformedURLException, SOAPException, RemoteException {

        KmReviewParamterForm  approveForm= new KmReviewParamterForm();
        approveForm.setFdId("333");
        Map folwParamMap = new HashMap();
        folwParamMap.put("operationType", "handler_pass");
        folwParamMap.put("auditNote", args[0]);
        JSONObject folwParamJson = new JSONObject(folwParamMap);
        approveForm.setFlowParam(folwParamJson.toJSONString());

        LONGiModuleBPMInteService.approveRoute(approveForm);
    }
}
