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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName: InboxTaskToDoAction
 * @Description: TODO 流程节点创建待办的实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleObjectReleaseNotifyAction implements LONGiModuleOASendToDoInterface {
    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        System.out.println("attrMap=" + attrMap);
        String id = attrMap.get("objId").toString();
//        String state = attrMap.get("state").toString();
//        String nextState = attrMap.get("nextState").toString();
        String type = attrMap.get("type").toString();

        try {
//			ContextUtil.pushContext(context);
            DomainObject obj = DomainObject.newInstance(context, id);
            obj.openObject(context);

            //////////////////////////////////////////////////////////////////////////////////
            // Get state based route objects for this object
            //////////////////////////////////////////////////////////////////////////////////

            //待办对应接收人
            LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
            try {
                targetBean.setLoginName(obj.getOwner(context).getName());
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
            docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);

            //待办的创建者
            NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
            jsonObject.setModelId(id);
            StringBuilder sbBuilder = new StringBuilder();
//            sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);
            sbBuilder.append(LONGiModuleBPMConfig.OBJECT_RELEASE_NOTIFY);
//			String relSymbolicName = PropertyUtil.getAliasForAdmin(context, "Type", "Project Space", false);
            try {

                String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
                String typeName = EnoviaResourceBundle.getTypeI18NString(context, type, language);

                sbBuilder.append("【" + typeName + "：" + obj.getName(context)+"】"+LONGiModuleBPMConfig.PROJECT_OBJECT_RELEASED);
            } catch (MatrixException e) {
                e.printStackTrace();
            }
            jsonObject.setSubject(sbBuilder.toString());
            jsonObject.setType(2);
            jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
            jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
            jsonObject.setLevel(3);
            jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
//			id----tagIdString
//			ContextUtil.popContext(context);

            System.out.println(jsonObject.toString());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            System.out.println("BPM接口调用开始时间----"+df.format(new Date()));// new Date()为获取当前系统时间
            System.out.println(LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id));
            System.out.println("BPM接口调用结束时间----"+df.format(new Date()));// new Date()为获取当前系统时间
        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
        return 0;
    }

}
