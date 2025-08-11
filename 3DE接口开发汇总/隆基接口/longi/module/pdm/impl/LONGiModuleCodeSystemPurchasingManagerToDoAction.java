package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;

import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleCommonTools;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import matrix.db.Role;
import matrix.db.User;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: InboxTaskToDoAction
 * @Description: TODO 流程节点创建待办的实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleCodeSystemPurchasingManagerToDoAction implements LONGiModuleOASendToDoInterface {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleCodeSystemPurchasingManagerToDoAction.class);

    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        logger.info("编码系统流程节点创建待办的实现类，输入参数：" + attrMap);
        String id = attrMap.get("objId").toString();
        String state = attrMap.get("state").toString();
        String nextState = attrMap.get("nextState").toString();
        String type = attrMap.get("type").toString();
        DomainObject order = null;

        try {
//			ContextUtil.pushContext(context);
            order = DomainObject.newInstance(context, id);
			// System.out.println("id : " + id);
            Map attributeMap = order.getAttributeMap(context, true);
			// System.out.println("attributeMap : " + attributeMap);
            //////////////////////////////////////////////////////////////////////////////////
            // Get state based route objects for this object
            //////////////////////////////////////////////////////////////////////////////////

            ArrayList arrayList = new ArrayList();
            //采购经理
            StringList memberList = new StringList();


            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();

//            targetBean.setLoginName(order.getOwner(context).getName());
            /*String systemPM = attributeMap.get("LONGiCodeSystemPM").toString();
            logger.info("编码系统审批角色设置，输入参数：" + systemPM);
            Role role = new Role(systemPM);
            UserList userList = role.getAssignments(context);
            for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
                User user = iterator.next();
                targetBean.setLoginName(user.getName());
                arrayList.add(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            }*/
            // 编码系统共享平台 采购经理
            String auditorID = (String) attributeMap.get("LGi_AuditorID");
			System.out.println("auditorID333 : " + auditorID);
            targetBean.setLoginName(auditorID);
			arrayList.add(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            // end

            //待办的创建者
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);


            NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
            jsonObject.setModelId(id);
            StringBuilder sbBuilder = new StringBuilder();
//            sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);

//			String relSymbolicName = PropertyUtil.getAliasForAdmin(context, "Type", "Project Space", false);
            try {
                String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
                String typeName = EnoviaResourceBundle.getTypeI18NString(context, type, language);

                sbBuilder.append("【" + typeName + "：" + attributeMap.get("LONGiCodeCNDescription") + "】" + LONGiModuleBPMConfig.CODE_SYSTEM_REQUEST_APPROVE_MESSAGE + typeName);

            } catch (MatrixException e) {
                e.printStackTrace();
            }
            jsonObject.setSubject(sbBuilder.toString());
            jsonObject.setType(1);
            jsonObject.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_PM_TAG);
            jsonObject.setTargets(arrayList.toString());
            jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            jsonObject.setLevel(3);
            jsonObject.setLink(LONGiModuleConfig.CODE_SYS_NAVI_APP_URL + "DAD-LGIMTL");
//			id----tagIdString
//			ContextUtil.popContext(context);

            String result = LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id);
            logger.info("编码系统流程节点创建待办的实现类，返回结果：" + result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
        return 0;
    }

}
