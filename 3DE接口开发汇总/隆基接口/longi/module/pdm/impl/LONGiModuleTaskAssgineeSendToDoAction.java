package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleCommonTools;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import matrix.db.Context;
import matrix.util.MatrixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: InboxTaskToDoAction
 * @Description: TODO 任务节点创建待办的实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleTaskAssgineeSendToDoAction implements LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleTaskAssgineeSendToDoAction.class);

    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        logger.info("WBS添加被分配人待办启动：" + attrMap);
        //Person
//		String fromId = attrMap.get("fromId").toString();
        String fromName = attrMap.get("fromName").toString();
        //Task
        String toId = attrMap.get("toId").toString();
        String typeName = "";
        try {
            DomainObject taskObj = DomainObject.newInstance(context, toId);

            String taksCurrent = taskObj.getInfo(context, "current");
            String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
            typeName = EnoviaResourceBundle.getTypeI18NString(context, "Task Management", language);
            if (!taksCurrent.equals("Assign") && !taksCurrent.equals("Active")) {
                logger.info("WBS添加被分配人待办退出，由于任务处于创建状态");
                return 0;
            }
            String toName = attrMap.get("toName").toString();
            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
            targetBean.setLoginName(fromName);
            //待办的创建者
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);
            NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
            jsonObject.setModelId(toId);
            StringBuilder sbBuilder = new StringBuilder();
            sbBuilder.append("【" + typeName + "：" + toName + "】" + LONGiModuleBPMConfig.TASK_ASSIGNEE_MESSAGE);
            jsonObject.setSubject(sbBuilder.toString());
            jsonObject.setType(1);
            jsonObject.setKey("Assigned Tasks");
            jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            jsonObject.setLevel(3);
            jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + toId);
            logger.info("WBS添加被分配人待办报文：" + LONGiModuleFastJsonUtil.beanToJson(jsonObject));
            logger.info("WBS添加被分配人待办结果：" + LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", toId));
        } catch (MatrixException e) {
            logger.error("WBS添加被分配人待办异常：" + e.toString());
            e.printStackTrace();
        }
        return 0;
    }

}
