package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.route.KmReviewParamterForm;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleBPMRouteFlowParamBean;
import longi.module.pdm.service.LONGiModuleBPMInteService;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: LONGiModuleOrderPromoteCheckBPMRouteCheck
 * @Description: TODO 删除受控单时检查受控单是否存在已关联的BPM流程，同时删除这个流程
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleOrderDeleteRouteAbandonCheck implements LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleOrderDeleteRouteAbandonCheck.class);

    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        String id = attrMap.get("objId").toString();
        System.out.println(id);
        DomainObject order = null;
        try {
            order = DomainObject.newInstance(context, id);
            String name = order.getInfo(context,"name");
            if (name.startsWith("CON")) {
                KmReviewParamterForm form = new KmReviewParamterForm();
                LONGiModuleBPMRouteFlowParamBean routeFlowParam = new LONGiModuleBPMRouteFlowParamBean();
                routeFlowParam.setAuditNote("受控单+" + name + "删除，流程废弃");
                routeFlowParam.setOperationType("drafter_abandon");
                form.setFlowParam(LONGiModuleJacksonUtil.beanToJson(routeFlowParam));
                //bpm 流程id 不为空，执行更新操作
                String bpmRouteId = order.getAttributeValue(context,"LONGiModuleRelBPMRouteId");
                form.setFdId(bpmRouteId);
                try {
                    String result = LONGiModuleBPMInteService.approveRoute(form);
                    logger.info("受控单关联的BPM废弃处理，返回结果：" + result);
                }catch (Exception e){
                    logger.error("调用BPM接口废弃流程失败，调用接口逻辑，异常信息：" + e.toString());
                }
            } else {
                logger.info("废弃的受控单数据是文件对象或者未关联BPM流程，受控单名称：" + name);
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
            logger.error("删除受控单时检查受控单是否存在已关联的BPM流程失败，异常信息：" + e.toString());
        }
        return 0;
    }

}
