package longi.module.pdm.impl;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;

import longi.module.pdm.client.todo.NotifyTodoSendContext;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleOAInteService;
import longi.module.pdm.util.LONGiModuleCommonTools;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import matrix.util.MatrixException;

import java.util.Map;

/** 
* @ClassName: InboxTaskChangeOwnerAction
* @Description: TODO 流程委托的实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:16:03 PM
*/
public class LONGiModuleProjectSpaceChangeOwnerAction implements LONGiModuleOASendToDoInterface {

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
		try {
			DomainObject obj = DomainObject.newInstance(context, objId);
			//tagIdString
			String oldOwner = attrMap.get("oldOwner").toString();

			String tagIdStringNew = LONGiModuleCommonTools.genIdTimesLong(objId);
			String newOwner = attrMap.get("newOwner").toString();
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
			String typeName = "";
			try {
				String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
				typeName = EnoviaResourceBundle.getTypeI18NString(context, "Project Space", language);
			} catch (MatrixException e) {
				e.printStackTrace();
			}
			StringBuilder sbBuilder = new StringBuilder();
//			sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);
			sbBuilder.append("【" + typeName + "：" + obj.getInfo(context,"name") + "】");
			sbBuilder.append(LONGiPersonUtil.getChineseName(context, oldOwner));
			sbBuilder.append("已派发给您该项目,请关注您的项目工作");
			toDoBean.setSubject(sbBuilder.toString());
			toDoBean.setType(2);
			toDoBean.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			toDoBean.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			toDoBean.setLevel(3);
			toDoBean.setExtendContent("");
			toDoBean.setOthers("");
			toDoBean.setLink(LONGiModuleConfig.CAS_NAVI_URL + objId);
			LONGiModuleOAInteService.sendTodo(context,toDoBean,"id",objId);

		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//InboxtaskID+personId
		
		return 0;
	}

}
