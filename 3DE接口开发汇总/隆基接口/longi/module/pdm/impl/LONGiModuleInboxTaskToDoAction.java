package longi.module.pdm.impl;

import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
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

/** 
* @ClassName: InboxTaskToDoAction
* @Description: TODO 流程节点创建待办的实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:15:19 PM
*/
public class LONGiModuleInboxTaskToDoAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleInboxTaskToDoAction.class);

	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		logger.info("流程节点创建待办的实现类待办任务：" + attrMap);
		String relId = attrMap.get("relId").toString();
		String relType = attrMap.get("relType").toString();
		// DomainObject object = DomainObject.newInstance(context, objectId);
		try {
//			ContextUtil.pushContext(context);
//			Person person = Person.getPerson(context);
//			DomainRelationship relationship = DomainRelationship.newInstance(context, relId);
			DomainRelationship relationship = new DomainRelationship(relId);
			try {
				relationship.open(context);
			} catch (MatrixException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DomainObject toObj = DomainObject.newInstance(context, relationship.getTo().getObjectId());
			DomainObject fromObj = DomainObject.newInstance(context, relationship.getFrom().getObjectId());
//			Map toAttribtueMap = toObj.getAttributeMap(context, true);
			Map fromAttribtueMap = fromObj.getAttributeMap(context, true);
			System.out.println("fromAttribtueMap="+fromAttribtueMap);
			String routeOwner = fromObj.getInfo(context,"from[Route Task].to.owner");
			String inboxTaskType = fromAttribtueMap.get("Route Action").toString();
			String id = relationship.getFrom().getObjectId();
			String tagIdString = LONGiModuleCommonTools.genIdTimesLong(id);
			ContextUtil.pushContext(context);
			fromObj.setAttributeValue(context, "LONGiModuleLastAssign",tagIdString);
			ContextUtil.popContext(context);
			//待办对应接收人
			LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
			targetBean.setLoginName(toObj.getName(context));
			//待办的创建者
			LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
			docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);

			NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
			jsonObject.setModelId(id);
			jsonObject.setKey(tagIdString);
			StringBuilder sbBuilder = new StringBuilder();
			sbBuilder.append("请审批");
			sbBuilder.append(LONGiPersonUtil.getChineseName(context,routeOwner));
//routeOwner
//			sbBuilder.append("【"+ LONGiModuleBPMConfig.PROJECT_NAME+"】");
//			sbBuilder.append(fromObj.getAttributeValue(context, "Route Instructions"));
			sbBuilder.append("提交的流程："+fromObj.getAttributeValue(context, "Route Instructions"));
			jsonObject.setSubject(sbBuilder.toString());
			if ("Notify Only".equals(inboxTaskType)){
				jsonObject.setType(2);
			}else{
				jsonObject.setType(1);
			}
			jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			jsonObject.setLevel(3);
			jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
//			id----tagIdString
//			ContextUtil.popContext(context);
			logger.info("流程节点创建待办的实现类待办任务，报文：" + LONGiModuleJacksonUtil.beanToJson(jsonObject));
			logger.info("流程节点创建待办的实现类待办任务，结果：" + LONGiModuleOAInteService.sendTodo(context,jsonObject,"relId",relId));
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			logger.info("流程节点创建待办的实现类待办任务主程序异常：" + e.toString());
			e.printStackTrace();
		}
		return 0;
	}

}
