package longi.module.pdm.impl;


import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import matrix.db.Context;

/** 
* @ClassName: InboxTaskNodeDoneAction
* @Description: TODO 流程节点待办完成的处理节点实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:15:46 PM
*/
public class LONGiModuleInboxTaskNodeDoneAction implements LONGiModuleOASendToDoInterface {

	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String objId = attrMap.get("objId").toString();
		
//		fromObj.setAttributeValue(context, "LONGiModuleLastAssign",tagIdString);
//		String type = attrMap.get("type").toString();
		try {
			DomainObject obj = DomainObject.newInstance(context, objId);
			
			String tagIdString = obj.getAttributeValue(context, "LONGiModuleLastAssign");
			NotifyTodoRemoveContext toDoDoneBean = new NotifyTodoRemoveContext();
			LONGiModuleTargetBean targetsBean = new LONGiModuleTargetBean();
			targetsBean.setLoginName(obj.getInfo(context,"from[Project Task].to.name"));

			toDoDoneBean.setModelId(objId);
			toDoDoneBean.setOptType(2);
			toDoDoneBean.setKey(tagIdString);
			toDoDoneBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetsBean));
			System.out.println(LONGiModuleOAInteService.sendTodoDone(context,toDoDoneBean,"id",objId));
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		
		
		return 0;
	}

//	public int sendToDo(Context context, String objectId, String type, String targetState) {
//		// TODO Auto-generated method stub
//		try {
//			DomainObject object = DomainObject.newInstance(context, objectId);
//			Map attribtueMap = object.getAttributeMap(context, true);
//			System.out.println("attribtueMap="+attribtueMap);
//		} catch (FrameworkException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return 0;
//	}
//
//	public int sendToDoDelete() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
	


}
