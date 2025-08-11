package longi.module.pdm.impl;

import java.util.Iterator;
import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;

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
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
* @ClassName: LONGiModuleRouteRejectToStopAction
* @Description: TODO 流程停止时删除未审批的任务节点
* @author: Longi.suwei
* @date: Nov 11, 2020 10:19:06 AM
*/
public class LONGiModuleRouteRejectToStopAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleRouteRejectToStopAction.class);

	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		logger.info("流程停止：" + attrMap + "，删除未执行的任务节点");
		String objId = attrMap.get("objId").toString();
		// DomainObject object = DomainObject.newInstance(context, objectId);
		try {
			DomainObject object = DomainObject.newInstance(context, objId);
			String relationshipPattern = "Route Task";
			String typePattern = "Inbox Task";
			StringList objectSelects = new StringList();
			objectSelects.addElement("id");
			objectSelects.addElement("name");
			objectSelects.addElement("attribute[LONGiModuleLastAssign]");
			objectSelects.addElement("current");
			objectSelects.addElement("owner");
			StringList relationshipSelects = new StringList();
			boolean getTo = true;
			boolean getFrom = false;
			String routeOwner = object.getOwner(context).getName();
			MapList doDeleteList = object.getRelatedObjects(context, relationshipPattern, typePattern, objectSelects, relationshipSelects, getTo, getFrom, (short)1, "current==Assigned", "", 0);
			logger.info("流程停止，查询到未执行的任务节点："+doDeleteList);
			for (Iterator iterator = doDeleteList.iterator(); iterator.hasNext();) {
				Map doDeleteMap = (Map) iterator.next();
				String tagIdString = doDeleteMap.get("attribute[LONGiModuleLastAssign]").toString();
				//tagIdString
				String id = doDeleteMap.get("id").toString();
				String owner = doDeleteMap.get("owner").toString();
				NotifyTodoRemoveContext removeBean = new NotifyTodoRemoveContext();
				removeBean.setModelId(id);
				removeBean.setKey(tagIdString);
				removeBean.setOptType(2);
				removeBean.setKey(tagIdString);
				LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean(owner);
				removeBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
//				System.out.println(LONGiModuleOAInteService.deleteTodo(context ,removeBean,"id",id));
				logger.info("流程停止，删除未执行的任务节点，报文内容：" + LONGiModuleFastJsonUtil.beanToJsonLongiOA(removeBean));
				String res = LONGiModuleOAInteService.deleteTodo(context ,removeBean,"id",id);
				logger.info("流程停止，删除未执行的任务节点，执行结果："+res);
			}


			NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
			jsonObject.setModelId(objId);
			StringBuilder sbBuilder = new StringBuilder();
			String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
			String typeName = EnoviaResourceBundle.getTypeI18NString(context, object.getInfo(context,"type"), language);
			sbBuilder.append("【" + typeName + "：" + object.getInfo(context,"name") + "】"+LONGiModuleBPMConfig.ROUTE_STOP_MESSAGE);
			jsonObject.setSubject(sbBuilder.toString());
			jsonObject.setType(2);
			LONGiModuleTargetBean routeTargetBean = new LONGiModuleTargetBean(routeOwner);
			jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(routeTargetBean));
			//待办的创建者
			LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
			docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);
			jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			jsonObject.setLevel(3);
			jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + objId);
			logger.info("流程停止，通知流程创建人报文内容：" + LONGiModuleFastJsonUtil.beanToJsonLongiOA(jsonObject));
			logger.info("流程停止，通知流程创建人代码执行结果：" + LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", objId));



			//			id----tagIdString
//			ContextUtil.popContext(context);
		} catch (Exception e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("流程停止，主程序异常，异常内容：" + e.toString());
		}
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
		return 0;
	}

}
