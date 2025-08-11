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
import matrix.util.StringList;

import java.util.ArrayList;
import java.util.Map;

/**
 * @ClassName: InboxTaskToDoAction
 * @Description: TODO 流程节点创建待办的实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleProjectReadyMemberNodifyAction implements LONGiModuleOASendToDoInterface {
    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        System.out.println("attrMap=" + attrMap);
        String id = attrMap.get("objId").toString();
        String state = attrMap.get("state").toString();
        String nextState = attrMap.get("nextState").toString();

        try {
//			ContextUtil.pushContext(context);
            DomainObject project = DomainObject.newInstance(context, id);

            //////////////////////////////////////////////////////////////////////////////////
            // Get state based route objects for this object
            //////////////////////////////////////////////////////////////////////////////////

            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
            ArrayList arrayList = new ArrayList();

            StringList memberList = project.getInfoList(context, "from[Member].to.name");
            for (int i = 0; i < memberList.size(); i++) {
                String personName = memberList.get(i);
                targetBean.setLoginName(personName);
                arrayList.add(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            }
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);

            //待办的创建者
            NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
            jsonObject.setModelId(id);
            StringBuilder sbBuilder = new StringBuilder();
            String notifyMessage = "";
            if(nextState.equals("Complete")){
                //PROJECT_MEMBER_COMPLETE_NOTIFY
                notifyMessage = LONGiModuleBPMConfig.PROJECT_MEMBER_COMPLETE_NOTIFY;
            }else{
                notifyMessage = LONGiModuleBPMConfig.PROJECT_MEMBER_NOTIFY;
            }
//            sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);

//			String relSymbolicName = PropertyUtil.getAliasForAdmin(context, "Type", "Project Space", false);
            try {
                String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
                String typeName = EnoviaResourceBundle.getTypeI18NString(context, "Project Space", language);
                sbBuilder.append("【" + typeName + "：" + project.getName() + "】" + notifyMessage);
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            jsonObject.setSubject(sbBuilder.toString());
            jsonObject.setType(2);
            jsonObject.setTargets(arrayList.toString());
            jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            jsonObject.setLevel(3);
            jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
//			id----tagIdString
//			ContextUtil.popContext(context);

            System.out.println(jsonObject.toString());
            System.out.println(LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id));
        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
        return 0;
    }

}
