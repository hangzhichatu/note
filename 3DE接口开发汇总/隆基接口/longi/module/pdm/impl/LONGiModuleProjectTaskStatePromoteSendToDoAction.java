package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
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

public class LONGiModuleProjectTaskStatePromoteSendToDoAction implements LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleProjectTaskStatePromoteSendToDoAction.class);
    // TODO Auto-generated method stub
    @Override
    public int sendToDo(Context context, Map attrMap){
        logger.info("WBS任务复核人待办启动：" + attrMap);
        String id = attrMap.get("objId").toString();
        try {
//			ContextUtil.pushContext(context);
            DomainObject task = DomainObject.newInstance(context, id);
            if(task.getInfo(context,"to[Subtask].from.type").equals("LONGiModuleProject")){
                System.out.println("非叶子节点任务，不发送待办");
                return 0;
            }
            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
            String message = "";
            String relId = task.getInfo(context,"to[Assigned Tasks].id");
            NotifyTodoRemoveContext removeBean = new NotifyTodoRemoveContext();

            //关闭待办
            removeBean.setModelId(id);
            removeBean.setKey("Assigned Tasks");
            /*
            1:表示设待办为已办操作
            2:表示设置目标待办所属人为已办操作
            */
            removeBean.setOptType(1);
            removeBean.setType(1);
            //到达审核状态,设计人的任务完成,结束设计人待办
            logger.info("WBS任务复核人，关闭任务被分配人待办报文内容：" + LONGiModuleFastJsonUtil.beanToJsonLongiOA(removeBean));
            logger.info("WBS任务复核人，关闭任务被分配人待办结果：" + LONGiModuleOAInteService.sendTodoDone(context, removeBean, "id", id));

            message = LONGiModuleBPMConfig.TASK_REVIEW_MESSAGE;
            try {
                targetBean.setLoginName(task.getOwner(context).getName());
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);
            //待办的创建者
            NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
            jsonObject.setModelId(id);
            StringBuilder sbBuilder = new StringBuilder();
            try {
                String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
                String typeName = EnoviaResourceBundle.getTypeI18NString(context, task.getInfo(context,"type"), language);
                sbBuilder.append("【" + typeName + "：" + task.getName() + "】"+message);
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            jsonObject.setSubject(sbBuilder.toString());
            jsonObject.setType(1);
            jsonObject.setKey("Manager");
            jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            jsonObject.setLevel(3);
            jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
            logger.info("WBS任务复核人，待办报文内容：" + LONGiModuleFastJsonUtil.beanToJsonLongiOA(jsonObject));
            logger.info("WBS任务复核人，发送复核待办结果：" + LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id));

        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            logger.error("WBS任务复核人待办，异常信息："+e.toString());
            e.printStackTrace();
        }
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
        return 0;
    }
}
