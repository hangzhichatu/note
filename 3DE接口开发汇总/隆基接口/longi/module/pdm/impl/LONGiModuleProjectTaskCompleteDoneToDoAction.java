package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LONGiModuleProjectTaskCompleteDoneToDoAction implements LONGiModuleOASendToDoInterface {
    // TODO Auto-generated method stub
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleProjectTaskCompleteDoneToDoAction.class);
    @Override
    public int sendToDo(Context context, Map attrMap){
        logger.info("WBS审核任务完成，关闭审核待办：" + attrMap);
        String id = attrMap.get("objId").toString();
        try {
            DomainObject task = DomainObject.newInstance(context, id);
            if(task.getInfo(context,"to[Subtask].from.type").equals("LONGiModuleProject")){
                logger.info("非叶子节点任务，不发送待办");
                return 0;
            }
            //待办对应接收人
//            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
//            String relId = task.getInfo(context,"to[Assigned Tasks].id");
//            String assignedPerson = task.getInfo(context,"to[Assigned Tasks].from.name");
//            LONGiModuleTargetBean targetDeleteBean = new LONGiModuleTargetBean();
            NotifyTodoRemoveContext removeBean = new NotifyTodoRemoveContext();
            removeBean.setModelId(id);
            removeBean.setKey("Manager");
            removeBean.setOptType(1);
//            removeBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetDeleteBean));
            removeBean.setType(1);
            //到达审核状态,设计人的任务完成,结束设计人待办
            logger.info("WBS审核任务完成，关闭审核待办报文内容：" + LONGiModuleFastJsonUtil.beanToJsonLongiOA(removeBean));
            logger.info("WBS审核任务完成，关闭审核待办待办结果：" + LONGiModuleOAInteService.sendTodoDone(context ,removeBean,"id",id));

        } catch (
                FrameworkException e) {
            // TODO Auto-generated catch block
            logger.info("WBS审核任务完成，关闭审核待办异常：" + e.toString());
            e.printStackTrace();
        }
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
        return 0;
    }
}
