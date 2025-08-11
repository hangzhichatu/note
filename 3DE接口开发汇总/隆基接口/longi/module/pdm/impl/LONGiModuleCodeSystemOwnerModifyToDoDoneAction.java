package longi.module.pdm.impl;


import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.framework.ui.UIUtil;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.service.LONGiModuleOAInteService;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: InboxTaskNodeDoneAction
 * @Description: TODO 流程节点待办完成的处理节点实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:46 PM
 */
public class LONGiModuleCodeSystemOwnerModifyToDoDoneAction implements LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleCodeSystemOwnerModifyToDoDoneAction.class);

    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        String objId = attrMap.get("objId").toString();
        logger.info("流程节点待办完成的处理节点实现类，输入参数：" + attrMap);
//		fromObj.setAttributeValue(context, "LONGiModuleLastAssign",tagIdString);
//		String type = attrMap.get("type").toString();
        try {
            DomainObject obj = DomainObject.newInstance(context, objId);
            String rejectReason = obj.getAttributeValue(context, "LONGiCodeRejectReason");
            if(UIUtil.isNotNullAndNotEmpty(rejectReason)){
                NotifyTodoRemoveContext toDoDoneBean = new NotifyTodoRemoveContext();
//			LONGiModuleTargetBean targetsBean = new LONGiModuleTargetBean();
                toDoDoneBean.setModelId(objId);
                toDoDoneBean.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_APPLICATION_TAG);
                toDoDoneBean.setOptType(1);
//			toDoDoneBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetsBean));
                String result = LONGiModuleOAInteService.sendTodoDone(context, toDoDoneBean, "id", objId);
                logger.info("流程节点待办完成的处理节点实现类执行完成，返回参数：" + result);
            }else{
                logger.info("当前申请单为初始状态，未被拒绝过，无需申请设置待办为已办");
            }


        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            logger.error("流程节点待办完成的处理节点实现类异常：" + e.toString());
        }


        return 0;
    }



}
