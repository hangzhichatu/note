package longi.module.pdm.service;


import java.util.Map;

import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.factory.LONGiModuleOASendToDoFactory;
import longi.module.pdm.util.LONGiModuleUserContext;
import matrix.db.Context;

/** 
* @ClassName: LONGiModuleOATriggerService
* @Description: TODO BPM集成入口类,由该类进行转发
* @author: Longi.suwei
* @date: Jun 29, 2020 5:50:22 PM
*/
public class LONGiModuleOATriggerService {


	/**
	 * The Action trigger  method on (Pending --> In Work) to Promote Connected CO to In Work State
	 * @param context
	 * @param attrMap (Change Action Id)
	 * @return 
	 * @throws Exception
	 */

//	public int oaSendTodo(Context context, String objectId, String type, String targetState) {
//		// TODO Auto-generated method stub
//		OASendToDoInterface targetOperation = OASendToDoFactory.getOperation(type)
//			      .orElseThrow(() -> new IllegalArgumentException("Invalid Operator"));
//			    return targetOperation.sendToDo(context, objectId, type, targetState);
//	}
//	public int oaSendTodoDelete(Context context, String objectId, String type, String targetState) {
//		// TODO Auto-generated method stub
//		OASendToDoInterface targetOperation = OASendToDoFactory.getOperation(type)
//			      .orElseThrow(() -> new IllegalArgumentException("Invalid Operator"));
//			    return targetOperation.sendToDoDelete();
//	}
	public int oaSendTodo(Context context, String action, Map attrMap) throws Exception {
		// TODO Auto-generated method stub
		LONGiModuleOASendToDoInterface targetOperation = LONGiModuleOASendToDoFactory.getOperation(action)
			      .orElseThrow(() -> new IllegalArgumentException("Invalid Operator,未定义该类型工厂方法:"+action));
			    return targetOperation.sendToDo(context, attrMap);
	}
}
