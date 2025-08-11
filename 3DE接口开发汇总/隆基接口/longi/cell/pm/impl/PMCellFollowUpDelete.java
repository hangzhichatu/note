package longi.cell.pm.impl;


import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.service.LONGiModuleOAInteService;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/** 
* @ClassName: PMCellFollowUpDelete
* @Description: TODO 跟进事项删除实现类
* @author: Longi.suwei
* @date: 2021 Aug 4 14:30:44
*/
public class PMCellFollowUpDelete implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(PMCellFollowUpDelete.class);
	@Override
	public int sendToDo(Context context, Map attrMap) {

		// TODO Auto-generated method stub
		String objId = attrMap.get("objectId").toString();
		//LONGiCodeSystemPM
		try {
			NotifyTodoRemoveContext removeContext = new NotifyTodoRemoveContext();
			removeContext.setModelId(objId);
			removeContext.setOptType(1);
			logger.info("跟进事项删除原指派人员的待办，输入参数"+attrMap);
			//删除流程申请人的待办
			String ownerMessage = LONGiModuleOAInteService.deleteTodo(context,removeContext,"id",objId);
			logger.info("跟进事项删除原指派人员的待办结果：" + ownerMessage);
			removeContext.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_PM_TAG);
			String pmMessage = LONGiModuleOAInteService.deleteTodo(context, removeContext, "id", objId);
			logger.info("跟进事项删除原指派人员的待办结果" + pmMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("跟进事项删除原指派人员的待办程序异常："+e.toString());
		}
		return 0;
	}
}
