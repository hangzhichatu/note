package longi.module.pdm.impl;

import com.matrixone.apps.common.ProjectManagement;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;

import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleCommonTools;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import matrix.db.Context;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
* @ClassName: LONGiModuleTaskNotifyAction
* @Description: TODO 任务节点创建待办的实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:15:19 PM
*/
public class LONGiModuleTaskNotifyAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleTaskNotifyAction.class);

	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		logger.info("WBS任务通知被分配者待办启动：" + attrMap);
		String id = attrMap.get("objId").toString();
//		String state = attrMap.get("state").toString();
//		String nextState = attrMap.get("nextState").toString();

		try {
//			ContextUtil.pushContext(context);
			DomainObject task = DomainObject.newInstance(context, id);

			//////////////////////////////////////////////////////////////////////////////////
			// Get state based route objects for this object
			//////////////////////////////////////////////////////////////////////////////////

			//待办对应接收人
			LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
			ArrayList arrayList = new ArrayList();

			StringList assignList = task.getInfoList(context, "to[Assigned Tasks].from.name");
			for (int i = 0; i < assignList.size(); i++) {
				String personName = assignList.get(i);
				targetBean.setLoginName(personName);
				arrayList.add(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			}
			LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
			docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);

			//待办的创建者
			NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
			jsonObject.setModelId(id);
			StringBuilder sbBuilder = new StringBuilder();
//            sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);

//			String relSymbolicName = PropertyUtil.getAliasForAdmin(context, "Type", "Project Space", false);
			try {
				String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
				String typeName = EnoviaResourceBundle.getTypeI18NString(context, task.getInfo(context,"type"), language);
				sbBuilder.append("【" + typeName + "：" + task.getName() + "】"+LONGiModuleBPMConfig.TASK_ASSIGNEE_MESSAGE);
			} catch (MatrixException e) {
				e.printStackTrace();
			}
			jsonObject.setKey("Assigned Tasks");
			jsonObject.setSubject(sbBuilder.toString());
			jsonObject.setType(1);
			jsonObject.setTargets(arrayList.toString());
			jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			jsonObject.setLevel(3);
			jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
//			id----tagIdString
//			ContextUtil.popContext(context);

			logger.info("通知任务被分配者待办，报文内容：" + LONGiModuleJacksonUtil.beanToJson(jsonObject));
//			System.out.println(jsonObject.toString());
			logger.info("通知任务被分配者待办，推送结果：" + LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id));
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("通知任务被分配者待办，程序出现异常，异常内容");
		}
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
		return 0;
	}

}
