package longi.cell.pm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import longi.module.pdm.client.todo.NotifyTodoRemoveContext;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import matrix.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/** 
* @ClassName: PMCellFollowUpComplete
* @Description: 设置跟进事项待办完成
* @author: Longi.suwei
* @date: 2021 Aug 4 14:30:16
*/
public class PMCellFollowUpComplete implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(PMCellFollowUpComplete.class);

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
		String objId = attrMap.get("objectId").toString();
		try {
			DomainObject obj = DomainObject.newInstance(context, objId);
			String targetPerson = obj.getAttributeValue(context,"LGiC_AssignOwner");
			NotifyTodoRemoveContext toDoDoneBean = new NotifyTodoRemoveContext();
			LONGiModuleTargetBean targetsBean = new LONGiModuleTargetBean();
			targetsBean.setLoginName(targetPerson);
			toDoDoneBean.setModelId(objId);
			toDoDoneBean.setOptType(2);
			toDoDoneBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetsBean));
			System.out.println(LONGiModuleOAInteService.sendTodoDone(context,toDoDoneBean,"id",objId));
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}
