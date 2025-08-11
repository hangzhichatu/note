package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.service.LONGiModuleOAInteService;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** 
* @ClassName: InboxTaskChangeOwnerAction
* @Description: TODO 删除系统
* @author: Longi.suwei
* @date: Jun 12, 2020 5:16:03 PM
*/
public class LONGiModuleCodeRequestToDoDeleteAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleCodeRequestToDoDeleteAction.class);

	/**
	* @Title: sendToDo
	* @Description: 
	* @param context
	* @param attrMap
	* @return
	* @see LONGiModuleOASendToDoInterface#sendToDo(Context, Map)
	*/
	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		String objId = attrMap.get("objId").toString();
		String state = attrMap.get("state").toString();
		//LONGiCodeSystemPM
//		String type = attrMap.get("type").toString();
		try {
//			DomainObject obj = DomainObject.newInstance(context,objId);
//			state = obj.getInfo(context,"current");
//			Map attributeMap = obj.getAttributeMap(context);
//			logger.info("删除编码申请单，申请单数据："+attributeMap);
			NotifyTodoRemoveContext removeContext = new NotifyTodoRemoveContext();
			removeContext.setModelId(objId);
			removeContext.setOptType(1);
			logger.info("编码申请单中删除原流程人员的待办，输入参数"+attrMap);
//			if(state.equals("Create")){
				//删除流程申请人的待办
				removeContext.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_APPLICATION_TAG);
				String ownerMessage = LONGiModuleOAInteService.deleteTodo(context,removeContext,"id",objId);
				logger.info("编码申请单中删除申请人员的待办结果：" + ownerMessage);
//			}else if(state.equals("Review")){
				removeContext.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_PM_TAG);
				String pmMessage = LONGiModuleOAInteService.deleteTodo(context, removeContext, "id", objId);
				logger.info("编码申请单中删除审核人的待办结果" + pmMessage);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("编码申请单中删除原流程人员的待办程序异常："+e.toString());
		}
		//InboxtaskID+personId
		return 0;
	}

}
