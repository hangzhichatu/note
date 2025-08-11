package longi.module.pdm.impl;


import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
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
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import matrix.db.Role;
import matrix.db.User;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/** 
* @ClassName: InboxTaskNodeDoneAction
* @Description: TODO 流程节点待办完成的处理节点实现类
* @author: Longi.suwei
* @date: Jun 12, 2020 5:15:46 PM
*/
public class LONGiModuleCodeSystemRequestRejectToOwnerAction implements LONGiModuleOASendToDoInterface {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleCodeSystemRequestRejectToOwnerAction.class);

	@Override
	public int sendToDo(Context context, Map attrMap) {
		// TODO Auto-generated method stub
		logger.info("编码申请驳回待办待阅处理，输入参数:" + attrMap);
		String id = attrMap.get("objId").toString();
		String state = attrMap.get("state").toString();
		String nextState = attrMap.get("nextState").toString();
		String type = attrMap.get("type").toString();
		DomainObject order = null;

		try {
//			ContextUtil.pushContext(context);
			order = DomainObject.newInstance(context, id);
			Map attributeMap = order.getAttributeMap(context, true);



			//////////////////////////////////////////////////////////////////////////////////
			// Get state based route objects for this object
			//////////////////////////////////////////////////////////////////////////////////


			//待办对应接收人
			LONGiModuleTargetBean targetBean = new LONGiModuleTargetBean();
			/*logger.info("待办申请人:" + order.getOwner(context).getName());
            targetBean.setLoginName(order.getOwner(context).getName());*/

			// 编码系统共享平台 编码申请人
			String codeOwnerID = attributeMap.get("LONGiCodeOwnerID").toString();
			logger.info("待办申请人:" + codeOwnerID);
			targetBean.setLoginName(codeOwnerID);
			// end

			//待办的创建者
			LONGiModuleTargetBean docCreatorBean = new LONGiModuleTargetBean();
			docCreatorBean.setLoginName(LONGiModuleBPMConfig.SYS_TODO_CREATOR);


			NotifyTodoSendContext jsonObject = new NotifyTodoSendContext();
			jsonObject.setModelId(id);
			StringBuilder sbBuilder = new StringBuilder();
//            sbBuilder.append(LONGiModuleBPMConfig.PROJECT_NAME);

//			String relSymbolicName = PropertyUtil.getAliasForAdmin(context, "Type", "Project Space", false);
			try {
				String language = LONGiModuleCommonTools.getLanguage(context.getLocale().getLanguage());
				String typeName = EnoviaResourceBundle.getTypeI18NString(context, type, language);

				sbBuilder.append("【" + typeName + "：" + attributeMap.get("LONGiCodeCNDescription") + "】" + LONGiModuleBPMConfig.CODE_SYSTEM_REQUEST_OWNER_REJECT_MESSAGE);

			} catch (MatrixException e) {
				e.printStackTrace();
			}
			jsonObject.setSubject(sbBuilder.toString());
			jsonObject.setType(1);
			jsonObject.setKey(LONGiModuleBPMConfig.CODE_SYSTEM_APPLICATION_TAG);
			jsonObject.setTargets(LONGiModuleFastJsonUtil.beanToJsonLongiOA(targetBean));
			jsonObject.setDocCreator(LONGiModuleFastJsonUtil.beanToJsonLongiOA(docCreatorBean));
			jsonObject.setLevel(3);
			String ownerID = order.getInfo(context,"owner");
			if ("lihao30".equals(ownerID) || "plmcodeuser".equals(ownerID) || "plmcodepurchase".equals(ownerID)) {
				jsonObject.setLink(LONGiModuleConfig.CODE_SYS_NAVI_APP_URL + "DAD-LGIMTL");
			} else {
				jsonObject.setLink(LONGiModuleConfig.CAS_NAVI_URL + id);
			}
//			id----tagIdString
//			ContextUtil.popContext(context);

			System.out.println(jsonObject.toString());
			System.out.println(LONGiModuleOAInteService.sendTodo(context, jsonObject, "id", id));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		into InboxTaskToDoAction attrMap = {relId=21847.44790.16530.51378, relSymbolicName=Project Task}
		return 0;
	}

}
