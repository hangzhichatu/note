package longi.module.pdm.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.util.MatrixException;
import matrix.util.StringList;

import java.util.Map;

public class LONGiPersonUtil extends Person{
	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = -4331689258572390258L;

	public static String getChineseName(Context context,String personLoginCode) {
		String fullNameString ="";
		try {
			Person person = Person.getPerson(context, personLoginCode);
			String lastNameString = person.getAttributeValue(context, "Last Name");
			String firstNameString = person.getAttributeValue(context, "First Name");
			fullNameString = lastNameString+","+firstNameString;
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fullNameString;
	}
	/**
	 * @Author: Longi.suwei
	 * @Title: transferNametoCompanyId
	 * @Description: TODO 转换登录号字符串为工号JSON eg. jiaofk,lu_hao->{PersonNo:'117194'}，{PersonNo:'117194'}
	 * @param: @param context
	 * @param: @param fd_n10
	 * @param: @return
	 * @date: Jul 16, 2020 11:05:36 AM
	 */
	public static String transferNametoCompanyId(Context context, String fd) throws FrameworkException {
		// TODO Auto-generated method stub
		JSONArray jsonArray = new JSONArray();
		StringBuilder sBuilder = new StringBuilder();
		if(ProgramCentralUtil.isNotNullString(fd)) {
			String[] fdStrings = fd.split("\\|");
            for (int i = 0; i < fdStrings.length; i++) {
				JSONObject jsonObject = new JSONObject();
                String fdString = fdStrings[i];
				Person person = Person.getPerson(context,fdString);
				jsonObject.put("PersonNo",person.getAttributeValue(context,"LONGiModulePersonId"));
				sBuilder.append(jsonObject.toJSONString()+",");
            }
		}
		String sbReturn = sBuilder.toString();
		if(sbReturn.endsWith(",")){
			sbReturn = sbReturn.substring(0,sbReturn.lastIndexOf(","));
		}
		return sbReturn;
	}

//	public static void main(String[] args) {
//		Context context = LONGiModuleUserContext.getContext();
//		try {
//			String appr = getDocApprover(context,"21847.44790.8868.65394");
//			System.out.println(appr);
//		} catch (MatrixException e) {
//			e.printStackTrace();
//		}
//	}
	public static String getDocApprover(Context context, String documentId) throws MatrixException {
		DomainObject documentObj = DomainObject.newInstance(context,documentId);
		BusinessObject bu = documentObj.getPreviousRevision(context);
		String approver = "";
		if(bu.exists(context)) {
			DomainObject previousObj = DomainObject.newInstance(context, bu.getObjectId());
//			DomainObject previousObj = documentObj;
			String relationshipPattern = "Route Task";
			String typePattern = "Inbox Task";
			StringList objectSelects = new StringList();
			objectSelects.addElement("name");
			objectSelects.addElement("from[Project Task].to.name");
			StringList relationshipSelects = new StringList();
			boolean getTo = true;
			boolean getFrom = false;
			short recurseToLevel = 1;
			String objectWhere = "";
			String relationshipWhere = "";
			int limit = 0;
			DomainObject relNodeObj = null;
			if (previousObj.isKindOf(context, "ACAD Drawing")) {
				String relNodeId = previousObj.getInfo(context, "to[LONGiModuleACADDrawingProcessForm].from.from[Object Route].to.id");
				if (ProgramCentralUtil.isNotNullString(relNodeId)) {
					relNodeObj = DomainObject.newInstance(context, relNodeId);

				}
			} else {

				String routeId = previousObj.getInfo(context, "from[Object Route|attribute[Route Base Purpose]=='Approval'].to.id");
//				String routeId = previousObj.getInfo(context, "from[Object Route].to.id");
				if (ProgramCentralUtil.isNotNullString(routeId)) {
					relNodeObj = DomainObject.newInstance(context, routeId);
				}
			}
			if (relNodeObj != null) {
				MapList mapList = relNodeObj.getRelatedObjects(context, relationshipPattern, typePattern, objectSelects, relationshipSelects, getTo, getFrom, recurseToLevel, objectWhere, relationshipWhere, limit);
				if (mapList != null && mapList.size() > 0) {
					mapList.addSortKey("name", "descending", "String");
					mapList.sort();
					Map approverMap = (Map) mapList.get(0);
					approver = approverMap.get("from[Project Task].to.name").toString();
					approver = LONGiPersonUtil.getChineseName(context, approver);
				}
			}
		}
		return approver;
	}
}
