<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.*,com.jspsmart.upload.*,java.io.*,java.text.*,java.io.File" %> 

<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="matrix.db.Context" %>
<%@include file = "../common/emxNavigatorInclude.inc"%>

<%
	System.out.println("00000000000000000000000000000");
    String[] selectedItems = emxGetParameterValues(request,"emxTableRowId");
    System.out.println("selectedItems++++++++++++>>>>>>>>>>>>>>>"+selectedItems);
	String selectId = "";
	String  name= "";
	String  fname="";
	String  lname="";
	String email = "";
	String cou = "";
	String filename = "creator";
	int count = 0;
	if(selectedItems!=null&&selectedItems.length>0){
		for (int i = 0; i < selectedItems.length; i++) {
			String selectedItem = selectedItems[i];
			System.out.println("selectedItem++++++++++++>>>>>>>>>>>>>>>"+selectedItem);
			String[] arr = selectedItem.split("\\|");
			if(arr.length >= 3) {
				selectId = arr[1];
			}
		
	System.out.println("selectId++++++++++++>>>>>>>>>>>>>>>"+selectId);
	if(!selectId.equals("")){
		System.out.println("1111111111111111111111111111");
		name = MqlUtil.mqlCommand(context, "print bus "+selectId+" select name dump");		
		System.out.println("name++++++++++++>>>>>>>>>>>>>>>"+name);
		
		fname = MqlUtil.mqlCommand(context, "print bus "+selectId+" select attribute[First Name] dump");		
		System.out.println("fname++++++++++++>>>>>>>>>>>>>>>"+fname);
		
		lname = MqlUtil.mqlCommand(context, "print bus "+selectId+" select attribute[Last Name] dump");		
		System.out.println("lname++++++++++++>>>>>>>>>>>>>>>"+lname);
		
		email = MqlUtil.mqlCommand(context, "print bus "+selectId+" select attribute[Email Address] dump");	
		System.out.println("email++++++++++++>>>>>>>>>>>>>>>"+email);

		cou = MqlUtil.mqlCommand(context, "print bus "+selectId+" select attribute[Country] dump");	
		System.out.println("cou++++++++++++>>>>>>>>>>>>>>>"+cou);
		
			cou="CN";
			System.out.println("cou++++++++++++>>>>>>>>>>>>>>>"+cou);
		
	}
	String create = "*PERSON "+name+";CMI"+"\n"+"+ATTRIBUTE First Name;"+fname+"\n"+"+ATTRIBUTE Last Name;"+lname+"\n"+"+ATTRIBUTE Email Address;"+email+"\n"+"+ATTRIBUTE Country;"+cou;
	System.out.println(create);
	// 生成的文件路径
	
			String path = "c:/temp/" + filename + ".txt";
			File file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			// write 解决中文乱码问题
			// FileWriter fw = new FileWriter(file, true);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(create);
			bw.flush();
			bw.close();
			fw.close();
		
		Properties props = new Properties();
		System.out.println("++++++++++++333>>>>>>>>>>>>>>>");
		
		
		String configFileName = "..\\webapps\\3dspace\\WEB-INF\\classes\\ChangePassWord.properties";
		
		FileInputStream in = new FileInputStream(configFileName);
         System.out.println("configFileName>>>>>>>>>>>>"+configFileName);
        props.load(in);
		String address = props.getProperty("address");
		String url = props.getProperty("url");
		String password = props.getProperty("password");
		String sys= address+" -url "+url+" -file "+path+" -action update -default_password "+password;
		System.out.println(sys);
		
		 Runtime rt = Runtime.getRuntime();  
        try {  
            Process exec = rt.exec(sys);  
           System.out.println("exec++++++++++++>>>>>>>>>>>>>>>"+exec); 
		   BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			String result = sb.toString();
			System.out.println("result++++++++++++>>>>>>>>>>>>>>>"+result); 
			
			 
        } catch (IOException e) {  
            e.printStackTrace();
			count =1;	
        }
		
		System.out.println("Success1111++++++++++++>>>>>>>>>>>>>>>"); 
		File files = new File(path);
		files.delete(); 
		}
	}
		System.out.println("Success++++++++++++>>>>>>>>>>>>>>>"); 
		System.out.println("count++++++++++++>>>>>>>>>>>>>>>"+count); 
	%>
<script>
	var exception = <%=count%>;
	if(exception==1){
		alert("\u91cd\u7f6e\u5bc6\u7801\u5931\u8d25\uff0c\u8bf7\u91cd\u65b0\u64cd\u4f5c\uff01");
	}else{
		alert("\u91cd\u7f6e\u5bc6\u7801\u6210\u529f");
	}
    
</script>