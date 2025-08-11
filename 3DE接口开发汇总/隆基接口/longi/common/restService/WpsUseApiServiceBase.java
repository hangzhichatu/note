package longi.common.restService;

import com.dassault_systemes.platform.restServices.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/getInfo")
public class WpsUseApiServiceBase extends RestService {

	@SuppressWarnings("unused")
	private final static Logger interfaceLogger = LoggerFactory.getLogger("module_interface");

	/**
	 * @Author: Longi.suwei
	 * @Title: getInfo
	 * @Description: 文件基础信息的接口
	 * @param: @param  request
	 * @param: @param  requestJson
	 * @param: @return
	 * @param: @throws Exception
	 * @date: 2024年4月23日 下午3:47:16
	 */
	@GET // 调用方式
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM }) // 输入格式
	@Produces({ MediaType.APPLICATION_JSON }) // 输出格式
	public Response getInfo(@Context HttpServletRequest request, @RequestBody String requestJson) throws Exception {
		System.out.println("sendData.." + requestJson);
		// 文件的Id
		String fileId = request.getHeader("X-Weboffice-File-Id");
		System.out.println("fileId=" + fileId);

		JsonObjectBuilder respJsonObj = Json.createObjectBuilder();
		respJsonObj.add("code", 500);
		respJsonObj.add("message", "success");
		respJsonObj.add("details", fileId);
		respJsonObj.add("hint", "");
		return Response.status(200).entity(respJsonObj.build().toString()).build();
	}

	/*
	 * @SuppressWarnings("unchecked") // 忽略掉unchecked警告 public Response
	 * sendData(HttpServletRequest request, String requestJson) throws Exception {
	 * System.out.println("sendData.." + requestJson); matrix.db.Context context =
	 * null; JsonObjectBuilder respJsonObj = Json.createObjectBuilder(); try {
	 * JSONObject reqJsonObj = JSONObject.parseObject(requestJson); String menu =
	 * reqJsonObj.getString("Menu");
	 * 
	 * Properties property = new Properties(); String path =
	 * this.getClass().getResource("/").getPath(); File propertyFile = new File(path
	 * + "WebserviceParam.properties"); FileInputStream fis = new
	 * FileInputStream(propertyFile); property.load(fis);
	 * 
	 * String internalURL = property.getProperty("internalURL"); String internalUser
	 * = property.getProperty("internalUser"); String internalPwd =
	 * property.getProperty("internalPwd");
	 * 
	 * context = new matrix.db.Context(internalURL); context.setUser(internalUser);
	 * context.setPassword(internalPwd); context.connect();
	 * System.out.println("connect success..");
	 * 
	 * String data = ""; ContextUtil.startTransaction(context, true);
	 * 
	 * if ("SalesRegionalSalesCatalog".equals(menu)) { data =
	 * getAreaSalesCatalogData(context); } else if ("ProductCode".equals(menu)) {
	 * data = getProductCodeData(context); }
	 * 
	 * System.out.println("data=" + data); ContextUtil.commitTransaction(context);
	 * respJsonObj.add("returnState", "0"); respJsonObj.add("message", "success");
	 * respJsonObj.add("data", data); return
	 * Response.status(200).entity(respJsonObj.build().toString()).build();
	 * 
	 * } catch (Exception ex) { ex.printStackTrace();
	 * ContextUtil.abortTransaction(context); respJsonObj.add("returnState", "1");
	 * respJsonObj.add("message", "" + ex); return
	 * Response.status(500).entity(respJsonObj.build().toString()).build(); } }
	 * 
	 * public String getProductCodeData(matrix.db.Context context) throws Exception
	 * { String strType =
	 * "LGiM_ModuleStandardBenchmarkCode,LGiM_ModuleCustomizedBenchmarkCode";
	 * JSONObject ProductCodeObject = new JSONObject(); JSONArray ProductCodeArray =
	 * new JSONArray(); String busWhere = "revision == last"; StringList busSelects
	 * = new StringList(DomainObject.SELECT_ID);
	 * busSelects.add(DomainObject.SELECT_NAME);
	 * busSelects.add(DomainObject.SELECT_CURRENT);
	 * busSelects.add("attribute[LONGiCodeCNDescription]");
	 * busSelects.add("attribute[LGiM_ModuleTargetSalesArea]");
	 * busSelects.add("attribute[LGiM_AdminPartInterfaceHistoryDate]"); MapList
	 * returnMapList = DomainObject.findObjects(context, strType, "*", busWhere,
	 * busSelects); Map dataMap = new HashMap<>(); // String current = ""; String
	 * name = ""; // String objectId = ""; String CNDesc = ""; String salesArea =
	 * ""; String releaseDate = ""; for (int i = 0; i < returnMapList.size(); i++) {
	 * JSONObject ProductCode_ITEM = new JSONObject(); dataMap = (Map)
	 * returnMapList.get(i); // current = (String)
	 * dataMap.get(DomainObject.SELECT_CURRENT); // objectId = (String)
	 * dataMap.get(DomainObject.SELECT_ID); if (!"Release".equals(current)) {
	 * objectId = getPreviousAndReleaseId(context, objectId); if
	 * (UIUtil.isNullOrEmpty(objectId)) { continue; } MapList tempList =
	 * DomainObject.findObjects(context, strType, "*", "id == '" + objectId + "'",
	 * busSelects); if (tempList.size() > 0) { dataMap = (Map) tempList.get(0); } }
	 * name = (String) dataMap.get(DomainObject.SELECT_NAME); CNDesc = (String)
	 * dataMap.get("attribute[LONGiCodeCNDescription]"); salesArea = (String)
	 * dataMap.get("attribute[LGiM_ModuleTargetSalesArea]"); salesArea =
	 * i18nNow.getRangeI18NString("LGiM_ModuleTargetSalesArea", salesArea, "zh-CN");
	 * // DomainObject dmo = new DomainObject(id); // salesArea =
	 * dmo.getAttributeValue(context, "LGiM_ModuleTargetSalesArea"); releaseDate =
	 * (String) dataMap.get("attribute[LGiM_AdminPartInterfaceHistoryDate]"); if
	 * (UIUtil.isNotNullAndNotEmpty(releaseDate)) { releaseDate =
	 * formatDate(releaseDate); }
	 * 
	 * ProductCode_ITEM.put("NAME", name); ProductCode_ITEM.put("ITEMCODEDESC_CN",
	 * CNDesc); ProductCode_ITEM.put("SALESAREA", salesArea);
	 * ProductCode_ITEM.put("RELEASE_DATE", releaseDate); //
	 * ProductCode_ITEM.put("ID", objectId); ProductCodeArray.add(ProductCode_ITEM);
	 * } System.out.println("ProductCodeArray : " + ProductCodeArray.size());
	 * ProductCodeObject.put("ProductCodeData", ProductCodeArray); return
	 * ProductCodeObject.toString();
	 * 
	 * }
	 * 
	 * public String getPreviousAndReleaseId(matrix.db.Context context, String
	 * objectId) throws Exception { String previousId = ""; DomainObject domain =
	 * new DomainObject(objectId); BusinessObject preRevisionBo =
	 * domain.getPreviousRevision(context); if (preRevisionBo.exists(context)) {
	 * DomainObject preRevObj = new DomainObject(preRevisionBo); String current =
	 * preRevObj.getInfo(context, "current"); if ("Release".equals(current)) {
	 * previousId = preRevObj.getObjectId(); } } return previousId; }
	 * 
	 * public String getAreaSalesCatalogData(matrix.db.Context context) throws
	 * Exception { StringList busSelects = new StringList(DomainObject.SELECT_TYPE);
	 * busSelects.add(DomainObject.SELECT_ID); MapList catalogList = new MapList();
	 * catalogList = DomainObject.findObjects(context,
	 * "LGiM_ModuleAreaSalesCatalog", "eService Production",
	 * "current == 'Complete'", busSelects); StringList ids = new StringList(); for
	 * (int i = 0; i < catalogList.size(); i++) { Map catalogMap = (Map)
	 * catalogList.get(i); String catalogId = (String)
	 * catalogMap.get(DomainObject.SELECT_ID); ids.add(catalogId); } String
	 * requestData = prepareData(context, ids); return requestData; }
	 * 
	 * public String prepareData(matrix.db.Context context, StringList ids) throws
	 * Exception { JSONObject SalesRegionalSalesCatalog = new JSONObject();
	 * JSONArray SalesRegionalSalesCatalog_ITEMS = new JSONArray(); for (int i = 0;
	 * i < ids.size(); i++) { String catalogId = ids.get(i); DomainObject catalogDmo
	 * = new DomainObject(catalogId); String completeDate =
	 * catalogDmo.getInfo(context, "state[Complete].actual"); if
	 * (UIUtil.isNotNullAndNotEmpty(completeDate)) { completeDate =
	 * formatDate(completeDate); } StringList busSelects = new
	 * StringList(DomainObject.SELECT_ID); busSelects.add(DomainObject.SELECT_NAME);
	 * busSelects.add("attribute[LGiM_ModuleTargetSalesArea]");
	 * busSelects.add("attribute[LONGiCodeCNDescription]");
	 * busSelects.add("attribute[LGiM_AdminPartInterfaceHistoryDate]");
	 * 
	 * StringList relSelects = new StringList(DomainObject.SELECT_RELATIONSHIP_ID);
	 * relSelects.add("attribute[LGiM_ModuleSalesArea]");
	 * relSelects.add("attribute[LGiM_ModuleAreaSalesUseContext]"); //
	 * relSelects.add("attribute[LGiM_ModuleEstimatedHaltSalesDate]"); //
	 * relSelects.add("attribute[LGiM_ModuleSchemeDesc]"); //
	 * relSelects.add("attribute[LGiM_ModuleBOMCostVariance]"); //
	 * relSelects.add("attribute[LGiM_ModuleCompareCode]");
	 * 
	 * MapList codeList = catalogDmo.getRelatedObjects( context,
	 * "LGiM_ModuleCatalog2BenchmarkCode", "*", busSelects, relSelects, false, true,
	 * (short) 0, "", "", 0); if (codeList.size() == 0) { return ""; } Map dataMap =
	 * new HashMap(); String salesRegional = ""; String itemCode = ""; String
	 * itemCodeCNDesc = ""; String itemRegional = ""; String itemUseContext = "";
	 * String startDate = ""; String endDate = ""; String itemBasicCode = ""; String
	 * releaseDate = ""; List<LGiM_ModuleAreaSalesCatalogCode> catalogCodeList = new
	 * ArrayList<LGiM_ModuleAreaSalesCatalogCode>(); for (int index = 0; index <
	 * codeList.size(); index++) { dataMap = (Map) codeList.get(index);
	 * salesRegional = (String) dataMap.get("attribute[LGiM_ModuleSalesArea]");
	 * itemCode = (String) dataMap.get(DomainObject.SELECT_NAME); itemCodeCNDesc =
	 * (String) dataMap.get("attribute[LONGiCodeCNDescription]"); itemRegional =
	 * (String) dataMap.get("attribute[LGiM_ModuleTargetSalesArea]"); (startDate =
	 * (String) dataMap.get("attribute[LGiM_ModuleEstimatedSalesDate]"); if
	 * (UIUtil.isNotNullAndNotEmpty(startDate)) { startDate = formatDate(startDate);
	 * } endDate = (String)
	 * dataMap.get("attribute[LGiM_ModuleEstimatedHaltSalesDate]"); if
	 * (UIUtil.isNotNullAndNotEmpty(endDate)) { endDate = formatDate(endDate); }
	 * itemBasicCode = (String) dataMap.get("attribute[LGiM_ModuleCompareCode]");
	 * itemUseContext = (String)
	 * dataMap.get("attribute[LGiM_ModuleAreaSalesUseContext]"); if
	 * (UIUtil.isNotNullAndNotEmpty(itemUseContext)) { itemUseContext =
	 * i18nNow.getRangeI18NString("LGiM_ModuleAreaSalesUseContext", itemUseContext,
	 * "zh-cn"); } releaseDate = (String)
	 * dataMap.get("attribute[LGiM_AdminPartInterfaceHistoryDate]"); if
	 * (UIUtil.isNotNullAndNotEmpty(releaseDate)) { releaseDate =
	 * formatDate(releaseDate); }
	 * 
	 * LGiM_ModuleAreaSalesCatalogCode catalogCode = new
	 * LGiM_ModuleAreaSalesCatalogCode();
	 * catalogCode.setSalesRegional(salesRegional);
	 * catalogCode.setItemCode(itemCode); catalogCode.setItemRegional(itemRegional);
	 * catalogCode.setItemUseContext(itemUseContext);
	 * catalogCode.setItemCodeCNDesc(itemCodeCNDesc);
	 * catalogCode.setReleaseDate(releaseDate); //
	 * catalogCode.setStartDate(startDate); // catalogCode.setEndDate(endDate); //
	 * catalogCode.setItemBasicCode(itemBasicCode);
	 * catalogCodeList.add(catalogCode); } Map<String,
	 * List<LGiM_ModuleAreaSalesCatalogCode>> catalogCodeMap =
	 * catalogCodeList.stream().collect(Collectors.groupingBy(o ->
	 * o.getSalesRegional())); String salesArea =
	 * PropertyUtil.getSchemaProperty(context, "attribute_LGiM_ModuleSalesArea");
	 * StringList salesAreaRanges = FrameworkUtil.getRanges(context, salesArea);
	 * JSONObject SalesRegionalSalesCatalog_ITEM = new JSONObject(); JSONArray
	 * SalesRegionalITEMS_ITEM = new JSONArray(); for (String salesAreaRange :
	 * salesAreaRanges) { List<LGiM_ModuleAreaSalesCatalogCode> itemList =
	 * catalogCodeMap.get(salesAreaRange); if (itemList != null) { JSONObject
	 * COMP_ITEMS = new JSONObject(); COMP_ITEMS.put("SalesRegional",
	 * salesAreaRange); COMP_ITEMS.put("SalesRegionalDESC",
	 * i18nNow.getRangeI18NString("LGiM_ModuleSalesArea", salesAreaRange, "zh-CN"));
	 * JSONArray COMP_ITEM = new JSONArray(); for (LGiM_ModuleAreaSalesCatalogCode
	 * catalogCode : itemList) { // System.out.println("catalogCode : " +
	 * catalogCode); JSONObject ITEM = new JSONObject(); ITEM.put("ITEMCODE",
	 * catalogCode.getItemCode()); ITEM.put("ITEMCODEDESC_CN",
	 * catalogCode.getItemCodeCNDesc()); ITEM.put("ITEMUSECONTEXT",
	 * catalogCode.getItemUseContext()); ITEM.put("RELEASE_DATE",
	 * catalogCode.getReleaseDate()); COMP_ITEM.add(ITEM); }
	 * COMP_ITEMS.put("COMP_ITEMS", COMP_ITEM);
	 * SalesRegionalITEMS_ITEM.add(COMP_ITEMS); } }
	 * SalesRegionalSalesCatalog_ITEM.put("SalesRegionalSalesCatalog_DATE",
	 * completeDate);
	 * SalesRegionalSalesCatalog_ITEM.put("SalesRegionalSalesCatalog_ITEMS",
	 * SalesRegionalITEMS_ITEM);
	 * SalesRegionalSalesCatalog_ITEMS.add(SalesRegionalSalesCatalog_ITEM); }
	 * SalesRegionalSalesCatalog.put("SalesRegionalSalesCatalog",
	 * SalesRegionalSalesCatalog_ITEMS); return
	 * SalesRegionalSalesCatalog.toString(); }
	 * 
	 * public String formatDate(String date) throws Exception { SimpleDateFormat
	 * sdf1 = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),
	 * Locale.US); SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	 * String rDate = ""; Date dDate = null; if (UIUtil.isNotNullAndNotEmpty(date))
	 * { dDate = sdf1.parse(date); rDate = sdf2.format(dDate); rDate = rDate; }
	 * return rDate; }
	 */

}
