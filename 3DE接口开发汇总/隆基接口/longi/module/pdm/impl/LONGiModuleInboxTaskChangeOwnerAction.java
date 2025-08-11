package longi.module.pdm.impl;

import java.util.Map;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
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
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
* @ClassName: InboxTaskChangeOwnerAction
* @Description: TODO 流程委托的实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:16:03 PM
*/
public class LONGiModuleInboxTaskChangeOwnerAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleInboxTaskChangeOwnerAction.class);

	/**
	* @Title: sendToDo
	* @Description: 
	* @param context
	* @param attrMap
	* @return
	* @see longi.module.pdm.dao.LONGiModuleOASendToDoInterface#sendToDo(matrix.db.Context, java.util.Map)
	*/
	@Override
	public int sendToDo(Context context, Map attrMap) {
		logger.info("流程委托的实现类待办任务：" + attrMap);
		// TODO Auto-generated method stub
		String objId = attrMap.get("objId").toString();
		try {
			DomainObject obj = DomainObject.newInstance(context, objId);
			String tagIdString = obj.getAttributeValue(context, "LONGiModuleLastAssign");
			//tagIdString
			String oldOwner = attrMap.get("oldOwner").toString();

			String tagIdStringNew = LONGiModuleCommonTools.genIdTimesLong(objId);
			String newOwner = attrMap.get("newOwner").toString();
			String oldTagId = obj.getAttributeValue(context,"LONGiModuleLastAssign");
			ContextUtil.pushContext(context);
			obj.setAttributeValue(context, "LONGiModuleLastAssign",tagIdStringNew);
			ContextUtil.popContext(context);

			//新建待办构造区域
			NotifyTodoSendContext toDoBean = new NotifyTodoSendContext();
			//待办对应接收人
			LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
			targetBean.setLoginName(newOwner);
			//待办的创建者
			LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
			docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);

			toDoBean.setModelId(objId);
			Person oldOwnerPerson = Person.getPerson(context, oldOwner);
			StringBuilder sbBuilder = new StringBuilder();
			sbBuilder.append("请处理由");
//			sbBuilder.append("【"+oldOwnerPerson.getAttributeValue(context, "部门")+"】");
			sbBuilder.append(LONGiPersonUtil.getChineseName(context, oldOwner));
			sbBuilder.append("委托给您的："+obj.getAttributeValue(context, "Route Instructions"));
			toDoBean.setSubject(sbBuilder.toString());
			toDoBean.setType(1);
			toDoBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			toDoBean.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			toDoBean.setLevel(3);
			toDoBean.setExtendContent("");
			toDoBean.setOthers("");
			toDoBean.setKey(tagIdStringNew);
			toDoBean.setLink(LONGiModuleConfig.CAS_NAVI_URL + objId);
			logger.info("流程委托的实现类待办任务，发送待办任务报文：" + LONGiModuleJacksonUtil.beanToJson(toDoBean));
			logger.info("流程委托的实现类待办任务返回结果："+LONGiModuleOAInteService.sendTodo(context,toDoBean,"id",objId));
//			LONGiModuleOAInteService.sendTodo(context,toDoBean,"id",objId);
			try {
				//待办设置为已办区域
				NotifyTodoRemoveContext toDoDoneBean = new NotifyTodoRemoveContext();
				LONGiModuleTargetBean newOwnerBean = new LONGiModuleTargetBean();
				newOwnerBean.setLoginName(oldOwner);
				toDoDoneBean.setModelId(objId);
				toDoDoneBean.setOptType(1);
				toDoDoneBean.setType(1);
				toDoDoneBean.setKey(oldTagId);
				toDoDoneBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(newOwnerBean));

				logger.info("流程委托的实现类待办任务，发送待办任务报文：" + LONGiModuleJacksonUtil.beanToJson(toDoDoneBean));
				logger.info("流程委托的实现类待办任务返回结果："+LONGiModuleOAInteService.sendTodoDone(context,toDoDoneBean,"id",objId));

			} catch (Exception e) {
				// TODO: handle exception
				logger.error("流程委托的实现类待办任务，设原任务为已办异常："+e.toString());
				e.printStackTrace();
			}

		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			logger.error("流程委托的实现类待办任务，主程序异常："+e.toString());
			e.printStackTrace();
		}
		//InboxtaskID+personId
		
		return 0;
	}

}
