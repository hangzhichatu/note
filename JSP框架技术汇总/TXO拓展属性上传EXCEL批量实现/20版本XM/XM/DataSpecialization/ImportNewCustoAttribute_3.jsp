<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*,com.jspsmart.upload.*,java.io.*,java.text.*" %>
<%@ page
        import="matrix.util.UUID,matrix.db.RelationshipType,com.matrixone.servlet.Framework,matrix.db.Context,com.matrixone.apps.domain.DomainObject,com.matrixone.apps.domain.util.PersonUtil,matrix.util.MatrixException,com.matrixone.apps.domain.util.ContextUtil,com.matrixone.apps.domain.util.FrameworkException,com.matrixone.apps.domain.util.MqlUtil,com.matrixone.apps.domain.DomainRelationship" %>
<%@ page import="jxl.*" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../emxTagLibInclude.inc" %>
<%@include file="../common/emxUIConstantsInclude.inc" %>

<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.DAuManagerAccess" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.IDAuManager" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.DAuData" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.DAuUtilities" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.DAuAttrInfosData" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.IDAuType" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.IDAuExtension" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.IDAuAttrInfos" %>
<%@page import="com.dassault_systemes.DictionaryAuthor_itfs.IDAuAttribute" %>
<%@ page import="java.io.File" %>
<html>
<head>
    <title>文件上传处理页面</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>


<%
    System.out.println("ImportNewCustoAttribute_3.jsp-----");

    String returnMessage = "";
    int sum = 0;
    context = Framework.getFrameContext(session);
    String currentTypeId = request.getParameter("currentTypeId");
    System.out.println("currentTypeId================33" + currentTypeId);
    String relFilePath = "";
    String selectedFrame = "";

    String strPathFile = request.getSession().getServletContext().getRealPath("/") + "upload";
    String strfile = "";
    try {
        File dirFile = new File(strPathFile);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        com.matrixone.servlet.MultipartRequest fileUpload = new MultipartRequest(request, strPathFile);
        Enumeration oFiles = fileUpload.getFileNames();
        String sFileName = "";
        if (oFiles != null) {
            while (oFiles.hasMoreElements()) {
                // query the multipart request for HTTP files
                String sName = (String) oFiles.nextElement();
                sFileName = fileUpload.getFilesystemName(sName);
                if ("file".equals(sName)) {
                    strfile = sFileName;
                    break;
                }
            }
        }
        String filename = sFileName;
        String prefix = filename.substring(filename.lastIndexOf(".") + 1);
        if (!"xlsx".equals(prefix)) {
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("请导入正确的类型，只支持.xlsx文件类型。");
    window.close();
</script>
<%
        return;
    }
    //改名开始
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
    Date myDate = new Date();
    String tempname = formatter.format(myDate);
    String temp_NewFileName = "NewCustoAttr" + tempname + ".xlsx"; // 这儿在日期字符串加上了一个标识符
    relFilePath = strPathFile + "/" + filename;
    File file = new File(relFilePath);
    file.renameTo(new File(strPathFile + "/" + temp_NewFileName));
    relFilePath = strPathFile + "/" + temp_NewFileName;
    HashMap<String, String> paramMap = new HashMap<String, String>(2);
    paramMap.put("relFilePath", relFilePath);
    MapList returnMapList = JPO.invoke(context, "ImportNewCustoAttribute", new String[]{}, "ImportExcel", JPO.packArgs(paramMap), MapList.class);
    //System.out.println("returnMapList===========" + returnMapList);
    //File fileDir = new File(relFilePath); //指定文件名及路径
    //if (fileDir.exists()) {
    //fileDir.delete();
    //}
    if (returnMapList != null) {
        Iterator<Map> attIter = returnMapList.iterator();
        while (attIter.hasNext()) {

            Map attrInfo = attIter.next();
            // ContextUtil.pushContext(context);

            if ((null != currentTypeId) && !currentTypeId.isEmpty()) {
                ContextUtil.startTransaction(context, true);
                IDAuManager manager = DAuManagerAccess.getDAuManager();
                if (null != manager) {
                    DAuData data = manager.getTypeOrExtensionFromId(context, currentTypeId);
                    if (null != data) {
                        IDAuType type = data.IDAUTYPE;
                        // System.out.println("type=====>" + type);
                        IDAuExtension extension = data.IDAUEXTENSION;
                        IDAuType instance = data.IDAUINSTANCE;
                        if (null != type) {
                            // System.out.println("11111");
                            String attrName = (String) attrInfo.get("name");
                            String attrType = (String) attrInfo.get("type");
                            String attrUnit = (String) attrInfo.get("unit");
                            String attrDefaultValue = "";
                            ArrayList<String> rangeValues = new ArrayList<String>();
                            returnMessage = attrName;
                            if ("String".equals(attrType)) {
                                String strRangeValues = (String) attrInfo.get("rangeValues");
                                if (UIUtil.isNotNullAndNotEmpty(strRangeValues)) {
                                    String[] strRangeValuess = strRangeValues.split(",");
                                    for (String range : strRangeValuess) {
                                        rangeValues.add(range);
                                    }
                                }
                                String strDefaultValue = (String) attrInfo.get("defaultValue");
                                if (UIUtil.isNotNullAndNotEmpty(strDefaultValue)) {
                                    attrDefaultValue = strDefaultValue;
                                }
                            }
                            selectedFrame = "type";
                            DAuAttrInfosData attrInfosData = new DAuAttrInfosData();
                            attrInfosData.S_Type = attrType;
                            attrInfosData.S_PreferredUnit = attrUnit;
                            attrInfosData.S_Length = null;
                            attrInfosData.S_DefaultValue = attrDefaultValue;
                            attrInfosData.B_ResetOnCloning = true;
                            attrInfosData.B_ResetOnNewVersion = true;
                            attrInfosData.S_UserAccess = "ReadWrite";
                            attrInfosData.B_MultiValuated = false;
                            attrInfosData.L_Range = rangeValues;
                            attrInfosData.S_SIXW = "";
                            attrInfosData.B_Indexation = true;
									/*
									if(attribute3DXML)
										attrInfosData.B_3DXMLExposed	  = attribute3DXML;
									if(attributeExportable)
										attrInfosData.B_V6Exportable	  = attributeExportable;
									*/
                            IDAuAttrInfos attrInfos = type.createAttrInfos(context, attrInfosData);
                            //IDAuAttrInfos attrInfos = type.createAttrInfos(context, attrType, attrUnit, attrLength, attrDefaultValue, attrResetCloning, attrResetNew, attrProtection);
                            if (null != attrInfos) {
                                IDAuAttribute attribute = type.addAttribute(context, attrName, attrInfos);
                                if (null != attribute) {
%>
<%@ include file="emxPackagesConfigurationCommitTransaction.inc" %>
<script language="javascript">
    function redirection(iFrame, iURL) {
        iFrame.document.location = iURL;
    }

    var contentFrame = openerFindFrame(top, "content");
    if (null != contentFrame) {
        //setTimeout('redirection(contentFrame, url)', 10);
        //var link = "javascript:link(\"1\",\"18720.4974.1920.40626\",\"\",\"18720.4974.51584.26442\",\"GMXDoc1\")";
        //setTimeout('eval(link)', 10000);
        //alert("Test");
    }
</script>
<%
        }
    }
    sum++;
} else if (null != extension) {
    // System.out.println("22222");
    String attrName = (String) attrInfo.get("name");
    String attrType = (String) attrInfo.get("type");
    String attrUnit = (String) attrInfo.get("unit");
    String attrDefaultValue = "";
    ArrayList<String> rangeValues = new ArrayList<String>();
    returnMessage = attrName;
    if ("String".equals(attrType)) {
        String strRangeValues = (String) attrInfo.get("rangeValues");
        if (UIUtil.isNotNullAndNotEmpty(strRangeValues)) {
            String[] strRangeValuess = strRangeValues.split(",");
            for (String range : strRangeValuess) {
                rangeValues.add(range);
            }
        }
        String strDefaultValue = (String) attrInfo.get("defaultValue");
        if (UIUtil.isNotNullAndNotEmpty(strDefaultValue)) {
            attrDefaultValue = strDefaultValue;
        }
    }
    selectedFrame = "extension";
    DAuAttrInfosData attrInfosData = new DAuAttrInfosData();
    attrInfosData.S_Type = attrType;
    attrInfosData.S_PreferredUnit = attrUnit;
    attrInfosData.S_Length = null;
    attrInfosData.S_DefaultValue = attrDefaultValue;
    attrInfosData.B_ResetOnCloning = true;
    attrInfosData.B_ResetOnNewVersion = true;
    attrInfosData.S_UserAccess = "ReadWrite";
    attrInfosData.B_MultiValuated = false;
    attrInfosData.L_Range = rangeValues;
    attrInfosData.S_SIXW = "";
    attrInfosData.B_Indexation = true;
									/*
									if(attribute3DXML)
										attrInfosData.B_3DXMLExposed	  = attribute3DXML;
									if(attributeExportable)
										attrInfosData.B_V6Exportable	  = attributeExportable;
									*/
    //IDAuAttrInfos attrInfos = extension.createAttrInfos(context, attrType, attrUnit, attrLength, attrDefaultValue, attrResetCloning, attrResetNew, attrProtection);
    IDAuAttrInfos attrInfos = extension.createAttrInfos(context, attrInfosData);
    if (null != attrInfos) {
        IDAuAttribute attribute = extension.addAttribute(context, attrName, attrInfos);
        if (null != attribute) {
%>
<%@ include file="emxPackagesConfigurationCommitTransaction.inc" %>
<%
                            }
                        }
                        sum++;
                    }
                }
            }
            // ContextUtil.popContext(context);
            ContextUtil.commitTransaction(context);
        }

    }

%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("导入完成，成功导入<%=sum%>条数据，请关闭。");
    window.close();
</script>
<%
} else {
    // ContextUtil.abortTransaction(context);
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("导入出现问题，请重新导入。");
    window.close();
</script>
<%
    }
} catch (Exception e) {
    ContextUtil.abortTransaction(context);
%>
<script type="text/javascript">
    window.opener.location.reload();
    alert("导入<%=returnMessage%>时出现问题，请重新导入。");
    window.close();
</script>
<%
    } finally {

    }
%>


</body>
</html>