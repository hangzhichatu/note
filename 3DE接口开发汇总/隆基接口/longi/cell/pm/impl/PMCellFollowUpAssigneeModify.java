package longi.cell.pm.impl;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.framework.ui.UIUtil;
import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleCommonTools;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import matrix.util.MatrixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** 
* @ClassName: PMCellFollowUpAssigneeModify
* @Description: PLM电池待办事项改owner的通知类
* @author: Longi.suwei
* @date: 2021 Aug 4 14:23:50
*/
public class PMCellFollowUpAssigneeModify implements longi.module.pdm.dao.LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(PMCellFollowUpAssigneeModify.class);
    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        logger.info("电池PLM跟进事项问题：" + attrMap);
        String objId = attrMap.get("objectId").toString();
        String newPersonName = attrMap.get("newPerson").toString();
        String oldPersonName = attrMap.get("oldPerson").toString();
        try {
            DomainObject followUpObj = DomainObject.newInstance(context, objId);
            String title = followUpObj.getAttributeValue(context,"Title");
            if(UIUtil.isNotNullAndNotEmpty(oldPersonName)){
                //假如已有待办,关闭以前待办
                try {
                    //待办设置为已办区域
                    NotifyTodoRemoveContext toDoDoneBean = new NotifyTodoRemoveContext();
                    LONGiModuleTargetBean oldOwnerBean = new LONGiModuleTargetBean();
                    oldOwnerBean.setLoginName(oldPersonName);
                    toDoDoneBean.setModelId(objId);
                    toDoDoneBean.setOptType(1);
                    toDoDoneBean.setType(1);
                    //toDoDoneBean.setKey(objId);
                    toDoDoneBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(oldOwnerBean));
                    logger.info("电池PLM跟进事项指派实现类待办任务，发送待办任务报文：" + LONGiModuleJacksonUtil.beanToJson(toDoDoneBean));
                    logger.info("电池PLM跟进事项指派实现类待办任务返回结果："+LONGiModuleOAInteService.sendTodoDone(context,toDoDoneBean,"id",objId));

                } catch (Exception e) {
                    // TODO: handle exception
                    logger.error("电池PLM跟进事项指派实现类待办任务，设原任务为已办异常："+e.toString());
                    e.printStackTrace();
                }
            }

            //新建待办构造区域
            NotifyTodoSendContext toDoBean = new NotifyTodoSendContext();
            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
            targetBean.setLoginName(newPersonName);
            //待办的创建者
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);
            toDoBean.setModelId(objId);
            StringBuilder sbBuilder = new StringBuilder();
            try {
                String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
                String typeName = EnoviaResourceBundle.getTypeI18NString(context, followUpObj.getInfo(context, "type"), language);
                sbBuilder.append("【" + typeName + "：" + followUpObj.getName() + "】");
            } catch (MatrixException e) {
                logger.error("PLM电池翻译异常问题"+e.getMessage());
                e.printStackTrace();
                return 0;
            }
            sbBuilder.append("请处理");
            sbBuilder.append(LONGiPersonUtil.getChineseName(context, Person.getPerson(context).getName()));
            sbBuilder.append("指派给您的跟进事项：" + title);
            toDoBean.setSubject(sbBuilder.toString());
            toDoBean.setType(1);
            toDoBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            toDoBean.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            toDoBean.setLevel(3);
            toDoBean.setExtendContent("");
            toDoBean.setOthers("");
            //toDoBean.setKey(objId);
            toDoBean.setLink(LONGiModuleConfig.CAS_NAVI_URL + objId);
            logger.info("电池PLM跟进事项指派实现类待办任务，发送待办任务报文：" + LONGiModuleJacksonUtil.beanToJson(toDoBean));
            logger.info("电池PLM跟进事项指派实现类待办任务返回结果："+LONGiModuleOAInteService.sendTodo(context,toDoBean,"id",objId));


        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            logger.info("电池PLM跟进事项指派实现类待办任务主程序异常：" + e.toString());
            e.printStackTrace();
        }
        return 0;
    }
}
