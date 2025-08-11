<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="../emxUICommonAppInclude.inc"%>
<%@include file="../emxUICommonHeaderBeginInclude.inc"%>
<%@include file = "../common/emxUIConstantsInclude.inc"%>
<%@page import="com.matrixone.apps.domain.util.FrameworkUtil"%>
<%@page import="com.matrixone.apps.domain.util.MqlUtil"%>
<%@page import="com.matrixone.apps.domain.DomainObject"%>
<%@page import="com.matrixone.apps.domain.DomainConstants"%>
<%@page import="matrix.db.AttributeType"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>文档中心</title>
	<script type="text/javascript" src="../common/scripts/emxSearchGeneral.js"></script>
	<script type="text/javascript" src="../common/scripts/emxUIToolbar.js"></script>
	<script type="text/javascript" src="../common/scripts/emxUICalendar.js"></script>
	<script type="text/javascript" src="../common/scripts/jquery-1.9.1.js"></script>
	<link href="../common/styles/emxUICalendar.css" rel="stylesheet"></link>
	<style type="text/css">
		
		input{outline-style: none;}
		select div{border: 0;}
		.input{
		  width:184px; 
		  height:20px;
		  border: 1px solid #707070;
		  padding: 1px!important; 
		} /* 96+1*2+1*2=100 */
		.select{
		  width:188px;
		  height:24px;
		  padding:1px;
		} /* 98+1*2+0*2=100 */
	</style>
</head>

<body>
<form action="" name="searchpage" id="searchpage">
	<table  border="0" cellpadding="2" cellspacing="1" height="100%" width="100%">
		<%
			String ACCDocCategoryDictionaryURL = "../common/emxFullSearch.jsp?table=ACCDictionarySearchTable&field=TYPES=type_ACCDocCategoryDictionary&program=ACCDoc:getFirstLevelDocCategory&expandProgram=ACCDoc:expendDocCategory&fieldNameActual=ACCDocCategoryDictionary&fieldNameOID=ACCDocCategoryDictionaryOID&showInitialResults=true&fieldNameDisplay=ACCDocCategoryDictionary&selection=multiple&submitURL=ACCSelectDocTypeProcess.jsp&suiteKey=ProgramCentral";
			
			String personInchargeURL="../common/emxFullSearch.jsp?field=TYPES=type_Person,type_BusinessUnit&sortColumnName=none&multiColumnSort=false&disableSel=TRUE&editLink=false&program=ATOZSearchPeopleBUJPO:getBusinessUnit&selection=multiple&hasChildren=false&table=AEFPersonChooserDetails&submitURL=../components/ACCSearchPersonSubmit.jsp&showInitialResults=true&expandProgram=ATOZSearchPeopleBUJPO:getPersonByBU&fieldNameActual=fieldPersonInchargeOID&fieldNameDisplay=fieldPersonIncharge&fieldNameOID=fieldPersonInchargeOID";
			
		//触发选人-----开始 用户名唯一的话显示用户名，不唯一的话显示用户名 登录名
			StringList busSelects = new StringList();
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			busSelects.add(DomainConstants.SELECT_ORIGINATED);
			busSelects.add("attribute[First Name]");
			busSelects.add("attribute[Last Name]");
			StringBuffer personNameDatasList = new StringBuffer();
			StringBuffer personIDDatasList = new StringBuffer();
			StringBuffer personDLDatasList = new StringBuffer();
			StringList orderBys = new StringList();
			orderBys.add("-originated");
			String busWhere = "current==Active";
			MapList mapLists = DomainObject.findObjects(context, "Person", context.getVault().getName(), busWhere,
						busSelects, (short)0, orderBys);
			MapList personDataList = new MapList();
			for(int datas=0;datas < mapLists.size();datas++){
				Map map = (Map)mapLists.get(datas);
				String personId_data = (String)map.get("id");
				String personName_data = (String)map.get("name");
				String personCNName_data = (String)map.get("attribute[First Name]")+(String)map.get("attribute[Last Name]");
				Map personDataMap = new HashMap();
				
				if(personDataList!=null && personDataList.size()>0){
					String renameFlag = "N";
					for(int datas1=0;datas1 < personDataList.size();datas1++){
						String personCNName_data_temp = (String)((Map)personDataList.get(datas1)).get("personCNName_data");
						String personId_data_temp = (String)((Map)personDataList.get(datas1)).get("personId_data");
						String personName_data_temp = (String)((Map)personDataList.get(datas1)).get("personName_data");
						if(personCNName_data.equals(personCNName_data_temp)){
							renameFlag = "Y";
							personDataList.remove(datas1);
							Map personDataMap_temp = new HashMap();
							personDataMap_temp.put("personId_data",personId_data_temp);
							personDataMap_temp.put("personName_data",personName_data_temp);
							personDataMap_temp.put("personCNName_data",personCNName_data_temp + " " + personName_data_temp);
							personDataList.add(personDataMap_temp);
							break;
						}
						
					}
					if("N".equals(renameFlag)){
						personDataMap.put("personId_data",personId_data);
						personDataMap.put("personName_data",personName_data);
						personDataMap.put("personCNName_data",personCNName_data);
						personDataList.add(personDataMap);
					}else{
						personDataMap.put("personId_data",personId_data);
						personDataMap.put("personName_data",personName_data);
						personDataMap.put("personCNName_data",personCNName_data + " " + personName_data);
						personDataList.add(personDataMap);
					}
				}else{
					personDataMap.put("personId_data",personId_data);
					personDataMap.put("personName_data",personName_data);
					personDataMap.put("personCNName_data",personCNName_data);
					personDataList.add(personDataMap);
				}
			}
							
			for(int datas=0;datas < personDataList.size();datas++){
				Map map = (Map)personDataList.get(datas);
				String personId_data = (String)map.get("personId_data");
				String personName_data = (String)map.get("personName_data");
				String personCNName_data = (String)map.get("personCNName_data");
				if(personNameDatasList.length()>0){
					personNameDatasList.append(",");
					personNameDatasList.append(personCNName_data);
				}else{
					personNameDatasList.append(personCNName_data);
				}
				if(personIDDatasList.length()>0){
					personIDDatasList.append(",");
					personIDDatasList.append(personId_data);
				}else{
					personIDDatasList.append(personId_data);
				}
				if(personDLDatasList.length()>0){
					personDLDatasList.append(",");
					personDLDatasList.append(personName_data);
				}else{
					personDLDatasList.append(personName_data);
				}
			}
			String personNameDatasString = personNameDatasList.toString();
			String personIDDatasString = personIDDatasList.toString();
			String personDLDatasString = personDLDatasList.toString();
			//触发选人-----结束
		%>
		<tr>
			<td class="label">文档编号</td>
			<td class="inputField">
				<input type="text" name= "ACCDocCode" id="ACCDocCode" value="" class="input" style="width:150px;">&nbsp;
			</td>
			<td class="label">文档名称</td>
			<td class="inputField">
				<input type="text" name= "ACCDocName" id="ACCDocName" value="" class="input" style="width:150px;">&nbsp;
			</td>
			<td class="label">关键词</td>
			<td class="inputField">
				<input type="text" name= "ACCKeyWord" id="ACCKeyWord" value="" class="input" style="width:150px;">&nbsp;
			</td>
		</tr>
		<tr>
			<td class="label">文件分类</td>
			<td class="inputField">
				<input type="text" name= "ACCDocCategoryDictionary" id="ACCDocCategoryDictionary" value="" class="input" style="width:150px;">&nbsp;
				<input type="hidden" name= "ACCDocCategoryDictionaryCode" id="ACCDocCategoryDictionaryCode" value="">
				<input type="hidden" name= "ACCDocCategoryDictionaryOID" id="ACCDocCategoryDictionaryOID" value="">
				<input type="hidden" name= "ACCDocCategoryDictionaryRowId" id="ACCDocCategoryDictionaryRowId" value="">
				<input type="button" class="button" value="..." name="btn1" id="btn1" onclick="javascript:showModalDialog('<%=XSSUtil.encodeForHTML(context, ACCDocCategoryDictionaryURL)%>',575,575);"/>
			</td>
			<td class="label">负责人</td>
			<td class="inputField">
				<input type="text" name= "fieldPersonIncharge" id="fieldPersonIncharge" value="" class="input" list="fieldPersonInchargegreetings" onchange="reloadUnit('fieldPersonIncharge')" onBlur="checkExist('fieldPersonIncharge')" style="width:150px;">&nbsp;
				<datalist id="fieldPersonInchargegreetings" style="display:none;">
				<%
					for (int data = 0; data < personDataList.size(); data++) {
						Map map = (Map)personDataList.get(data);
						String personId_data = (String)map.get("personId_data");
						String personName_data = (String)map.get("personName_data");
						String personCNName_data = (String)map.get("personCNName_data");
						String personLabel = "";
						String[] personCNName_dataList = personCNName_data.split(" ");
						if(personCNName_dataList.length==2){
							personLabel = personCNName_data;
						}else{
							personLabel = personCNName_data + " " + personName_data;
						}
				%>
					<option value="<%=personCNName_data%>" data-value="<%=personId_data%>"><%=personLabel%></option>				
				<%  } 	%>							
				</datalist>
				<input type="hidden" name= "fieldPersonInchargeOID" id="fieldPersonInchargeOID" value="">
				<input type="button" class="button" value="..." name="btn3" id="btn3" onclick="javascript:showModalDialog('<%=XSSUtil.encodeForHTML(context, personInchargeURL)%>',575,575);"/>
			</td>
			<td class="label">文档版本</td>
			<td class="inputField">
				<select class="select" name="ACCDocVersion" id="ACCDocVersion" style="width:150px;">
					<option value="最新版" selected="selected">最新版</option>
					<option value="全部版本">全部版本</option>
				</select>
			</td>
			<td class="label">
				<input type="button" class="button" onclick="javascript:searchProgram();" value="搜 索"/>
				<input type="button" class="button" onclick="javascript:resetForm();" value="重 置"/>
			</td>
		</tr>
	</table>
</form>
</body>
<script type="text/javascript">

	function searchProgram()
	{
		var ACCDocCode = document.searchpage.ACCDocCode.value;
		var ACCDocName = document.searchpage.ACCDocName.value;
		var ACCKeyWord = document.searchpage.ACCKeyWord.value;
		var ACCDocCategoryDictionaryCode = document.searchpage.ACCDocCategoryDictionaryCode.value;
		var fieldPersonInchargeOID = document.searchpage.fieldPersonInchargeOID.value;
		var ACCDocVersion = document.searchpage.ACCDocVersion.value;

		var url="../common/emxTable.jsp?program=ACCDoc:getQueryDevelopDocs,ACCDoc:getMyDevelopDocs,ACCDoc:getMyCompanyDevelopDocs,ACCDoc:getAllDevelopDocs&programLabel=emxComponentCentral.ACCDevelopDoc.getQueryDevelopDocs,emxComponentCentral.ACCDevelopDoc.getMyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getMyCompanyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getAllDevelopDocs&toolbar=ACCDevelopDocSummaryToolbar&mode=New&table=ACCDevelopDocSummary&header=emxComponentCentral.ACCDevelopDoc.MyDoc&clearLimitNotice=true&editLink=false&selection=multiple&suiteKey=Components&StringResourceFileId=emxComponentsStringResource"
				+"&ACCDocCode="+ACCDocCode+"&ACCDocName="+ACCDocName
				+"&ACCKeyWord="+ACCKeyWord+"&ACCDocCategoryDictionaryCode="+ACCDocCategoryDictionaryCode
				+"&fieldPersonInchargeOID="+fieldPersonInchargeOID+"&ACCDocVersion="+ACCDocVersion;
		console.log("url-----"+url);
		self.parent.frames["ShowData"].location = url;
	}
	
	function reloadUnit(id){
		var input_select = document.getElementById(id).value;
		var option = $("#"+id+"greetings").children();
		var flag = "";
		for(var i = 0; i < option.length; i++){
			
			var option_value = $("#"+id+"greetings").children()[i].value;
			
			if(input_select == option_value&&option_value!=""&&input_select!=""){
				flag = "Y";
				var datavalue = $("#"+id+"greetings").children()[i].getAttribute("data-value");
				var valueId= $("#"+id+"greetings").children()[i].getAttribute("data-value");
				var setfieldName="document.getElementById(\"" + id + "OID\").value=\""+valueId+"\"";
				eval(setfieldName);
				break;
			}
		}
		if(""==flag){
			var setfieldName="document.getElementById(\"" + id + "OID\").value=\"\"";
			eval(setfieldName);
		}
	}
	
	function resetForm()
	{
		$("input[type='hidden']").val("");
		$('#searchpage')[0].reset();
		var url = "../common/emxIndentedTable.jsp?program=ACCDoc:getMyDevelopDocs,ACCDoc:getMyCompanyDevelopDocs,ACCDoc:getAllDevelopDocs&programLabel=emxComponentCentral.ACCDevelopDoc.getMyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getMyCompanyDevelopDocs,emxComponentCentral.ACCDevelopDoc.getAllDevelopDocs&toolbar=ACCDevelopDocSummaryToolbar&mode=New&table=ACCDevelopDocSummary&header=emxComponentCentral.ACCDevelopDoc.MyDoc&clearLimitNotice=true&sortColumnName=Originated&sortDirection=decending&editLink=false&selection=multiple&suiteKey=Components&StringResourceFileId=emxComponentsStringResource";
		self.parent.frames["ShowData"].location = url;
		return;
	}		
</script>
</html>