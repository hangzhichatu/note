package longi.module.pdm.impl;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LONGiModuleTaskAssgineeSendToDoDeleteAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleTaskAssgineeSendToDoDeleteAction.class);

	@Override
	public int sendToDo(Context context, Map attrMap) {
		logger.info("WBS任务删除分配人关闭待办：" + attrMap);
		// TODO Auto-generated method stub

		//Person
//		String fromId = attrMap.get("fromId").toString();
		String fromName = attrMap.get("fromName").toString();
		//Task
		String toId = attrMap.get("toId").toString();
//		String toName = attrMap.get("toName").toString();
//		String relId = attrMap.get("relId").toString();
		try {
			NotifyTodoRemoveContext removeBean = new NotifyTodoRemoveContext();
			removeBean.setModelId(toId);
			removeBean.setKey("Assigned Tasks");
			removeBean.setOptType(2);
			LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean(fromName);
			removeBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			logger.info("WBS任务删除分配人关闭待办报文：" + LONGiModuleFastJsonUtil.beanToJson(removeBean));
			logger.info("WBS任务删除分配人关闭待办结果：" + LONGiModuleOAInteService.deleteTodo(context, removeBean, "id", toId));
		}catch (Exception e){
			logger.error("WBS任务删除分配人关闭待办异常：" + e.toString());
		}
		return 0;
	}

}
