package longi.dataecharts.handle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import longi.dataecharts.FileWriterUtil;
import longi.dataecharts.entity.CodeData;
import longi.dataecharts.entity.InCodeData;
import longi.dataecharts.restinterface.RestInterfaceUtil;
import matrix.db.Context;
import matrix.util.StringList;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CodeInfoHandle {
    private static String  url = "https://10.0.3.79/internal/pdmservice/module/bd/select";
    
    
    public static String getAreaSalesMonthlyCatalog(Context context,String mouth)throws Exception{
    	String strR="";
	    System.out.println("Start====>"+mouth);
        StringList objectSelects = new StringList();
        objectSelects.add("id");
        objectSelects.add("name");
        String sWhere = "attribute[LGiM_ModuleAreaSalesMonthlyCatalogDate].value=='"+ mouth +"'";
        MapList mouthlist = DomainObject.findObjects(context,
                "LGiM_ModuleAreaSalesMonthlyCatalog",                           // type filter
                "*",                               // vault filter
                sWhere,                            // where clause
                objectSelects);
        //理论上应该只有一个
        if(mouthlist.size()>0){
            Map mouthmap= (Map) mouthlist.get(0);
            strR= (String) mouthmap.get("id");
            
        }else{
			//i++;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
			Calendar caCurrent = Calendar.getInstance();
			
			Date date = format.parse(mouth);
			caCurrent.setTime(date);
			caCurrent.add(Calendar.MONTH, -1);
			caCurrent.set(Calendar.DAY_OF_MONTH,2);
			//caCurrent.add(Calendar.MONTH, -i);
			
        	Calendar caStart = Calendar.getInstance();
        	caStart.set(Calendar.MONTH, 9);
        	caStart.set(Calendar.YEAR, 2022);
        	caStart.set(Calendar.DAY_OF_MONTH,1);
        	
    		String strMouth = format.format(caCurrent.getTime());
    		System.out.println("strMouth==@@@@@@@@@==>"+strMouth);
        	if(caCurrent.before(caStart)){

        	}else{
        		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        		//String strMouth = format.format(caCurrent.getTime());
        		strR=getAreaSalesMonthlyCatalog(context,strMouth);
        	}
        }
        System.out.println("strR>>>>====Id=========="+strR);
		return strR;
    }
    
    
    

    public static Set<String> getInCodeStr2(Context context,String dateMonth) throws Exception{
        Set<String> resultList = new HashSet<>();
        try {
        	String strCatalogId= getAreaSalesMonthlyCatalog(context,dateMonth);
            if(UIUtil.isNotNullAndNotEmpty(strCatalogId)){
            	DomainObject  objCatalog=DomainObject.newInstance(context, strCatalogId);
            	StringList objectSelects = new StringList();
                objectSelects.add("id");
                objectSelects.add("name");
                //objectSelects.add("attribute[LONGiCodeCNDescription]");
                MapList inlist = objCatalog.getRelatedObjects(context, "LGiM_ModuleCatalog2BenchmarkCodeIn", "*",
                        objectSelects, null, false, true, (short) 0, null, null, (short) 0);
                resultList.addAll(EngineeringUtil.getValueForKey(inlist, "name"));
                //resultList= (Set<String>) EngineeringUtil.getValueForKey(inlist, "name");
            }
            
            /* String result = RestInterfaceUtil.doPost(url,new HashMap<String, String>() {{
                put("Menu", "SalesRegionalSalesCatalog");
            }}, "bpm_basic", "bpm_basic");
            if(StringUtils.isNotBlank(result)){
                 String message = ((JSONObject) JSONObject.parse(result)).getString("message");
                 if(Objects.equals(message,"success")){
                     JSONObject resultData = ((JSONObject) JSONObject.parse(result)).getJSONObject("data");
                     JSONArray resultJSONArray = resultData.getJSONArray("SalesRegionalSalesCatalog");
//                     log.info("JSON->{}",JSON.toJSON(resultJSONArray));
                     JSONObject jsonObject = null;
                     for (Object jsonObj : resultJSONArray) {
                         if(((JSONObject)jsonObj).getString("SalesRegionalSalesCatalog_DATE").contains(dateMonth)){
                             jsonObject = (JSONObject)jsonObj;
                             break;
                         }
                     }
                     JSONArray jsonArray = jsonObject.getJSONArray("SalesRegionalSalesCatalog_ITEMS");
                     jsonArray.forEach(json2->{
                         JSONArray jsonArray3 =((JSONObject) json2).getJSONArray("COMP_ITEMS");
                         jsonArray3.forEach(json3->{
                             String code = ((JSONObject) json3).getString("ITEMCODE");
                             if(!code.endsWith("R")){
                                 resultList.add(code);
                             }
                         });
                     });
                 }
            }*/
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
        
        return resultList;
    }


    public static Set<CodeData> getAllCodeRange(List<String> months) {
        Set<CodeData> results = new HashSet<>();
        results.addAll(getAllCode(DateTimeFormatter.ofPattern("yyyy-MM").format(
                LocalDate.now().minusMonths(1))));
//        months.forEach(month->{
//            Set<CodeData> result = getAllCode(month);
//            results.addAll(result);
//        });
        return results;
    }

    public static Set<String> getInCodeStrRange(List<String> months) {
        Set<String> results = new HashSet<>();
        months.forEach(month->{
            Set<String> result = getInCodeStr(month);
            results.addAll(result);
        });
        return results;
    }

    public static Set<CodeData> getAllCode2() {
        Set<CodeData> resultList = new HashSet<>();
        String result = RestInterfaceUtil.doPost(url, new HashMap<String, String>() {{
            put("Menu", "ProductCode");
        }}, "bpm_basic", "bpm_basic");
        if (StringUtils.isNotBlank(result)) {
            String message = ((JSONObject) JSONObject.parse(result)).getString("message");
            if (Objects.equals(message, "success")) {
                JSONObject resultData = ((JSONObject) JSONObject.parse(result)).getJSONObject("data");
                JSONArray resultJSONArray = resultData.getJSONArray("ProductCodeData");
                resultJSONArray.forEach(json -> {
                    String date = ((JSONObject) json).getString("RELEASE_DATE");
                    String area = ((JSONObject) json).getString("SALESAREA");
                    String code = ((JSONObject) json).getString("NAME");
                    String code_desc= ((JSONObject) json).getString("ITEMCODEDESC_CN");
                    if(!code.endsWith("R")){
                        resultList.add(CodeData.builder().code(code).date(date).area(area).codeDesc(code_desc).build());
                    }
                });
            }
        }
        return resultList;
    }

    public static Set<CodeData> getInCode(Set<CodeData> allCode,Set<String> inCode) {
        Set<CodeData> resultList = new HashSet<>();
        if(CollectionUtils.isNotEmpty(allCode) && CollectionUtils.isNotEmpty(inCode)){
            allCode.forEach(codeEntity->{
                        if(inCode.contains(codeEntity.getCode())){
                            resultList.add(codeEntity);
                        }
            });
        }
        return resultList;
    }

    public static Set<CodeData> getOutCode(Set<CodeData> allCode,Set<String> inCode) {
        Set<CodeData> resultList = new HashSet<>();
        if(CollectionUtils.isNotEmpty(allCode) && CollectionUtils.isNotEmpty(inCode)){
            allCode.forEach(codeEntity->{
                if(!inCode.contains(codeEntity.getCode())){
                    resultList.add(codeEntity);
                }
            });
        }
        // "澳洲"
//        log.info("目录外数据->{}",JSONArray.toJSON(resultList));
        return resultList;
    }

    public static void writeFile(Context context, String fileName){
       
        /*String uploadFileSavePath = System.getProperty("user.dir")
                + File.separator + "dbData/PLMDataCode/";
      
        File folder = new File(uploadFileSavePath);
        if (!folder.exists()) {
            
        	folder.mkdirs();
        }
        int len = folder.listFiles().length + 1;
        String version = len < 10 ? "0" + len : String.valueOf(len);*/
       
        try { 
        	
        	Set<String> inCodeSet = getInCodeStr2(context,DateTimeFormatter.ofPattern("yyyy-MM").format(
                LocalDate.now().minusMonths(1)));
	        Set<CodeData> allCodeSet = getAllCode2();
	        Map<String, Object> map = new HashMap<String, Object>() {{
	            put("inCodeSet", inCodeSet);
	            put("allCodeSet", allCodeSet);
	            put("version", "01");
	        }};
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        	jedis.set(fileName, JSON.toJSONString(map));
            //FileWriterUtil.writeFilePLMDataCode(fileName, JSON.toJSONString(map));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static Set<String> getInCodeStr(String dateMonth){
        Set<String> resultList = new HashSet<>();
        JSONObject jsonObject = FileWriterUtil.parseFile2("plm-code-"
                +dateMonth,"PLMDataCode");
        if(Objects.nonNull(jsonObject)) {
            JSONArray jsonArray1 = jsonObject.getJSONArray("inCodeSet");
            jsonArray1.forEach(json -> resultList.add((String) json));
        }
        return resultList;
    }

    public static Set<CodeData> getAllCode(String dateMonth) {
        Set<CodeData> resultList = new HashSet<>();
        JSONObject jsonObject = FileWriterUtil.parseFile2("plm-code-"
                + dateMonth, "PLMDataCode");
       
        if (Objects.nonNull(jsonObject)) {
            JSONArray jsonArray1 = jsonObject.getJSONArray("allCodeSet");
            jsonArray1.forEach(json -> {
                JSONObject jsonObject1 = (JSONObject) json;
                CodeData codeData = CodeData.builder()
                        .code(jsonObject1.getString("code"))
                        .area(jsonObject1.getString("area"))
                        .date(jsonObject1.getString("date"))
                        .codeDesc(jsonObject1.getString("codeDesc"))
                        .build();
                resultList.add(codeData);
            });
        }
        return resultList;
    }

    public static String getVersion(String dateMonth){
        String version = "01";
        JSONObject jsonObject = FileWriterUtil.parseFile2("plm-code-"
                +dateMonth,"PLMDataCode");
        if(Objects.nonNull(jsonObject)) {
            version  = jsonObject.getString("version");
        }
        return version;
    }

    public static List<InCodeData> getInCodeFirstDate(){
        TreeMap<String,Set<String>> resultMap = new TreeMap<>();
        String result = RestInterfaceUtil.doPost(url,new HashMap<String, String>() {{
            put("Menu", "SalesRegionalSalesCatalog");
        }}, "bpm_basic", "bpm_basic");
        if(StringUtils.isNotBlank(result)){
            String message = ((JSONObject) JSONObject.parse(result)).getString("message");
            if(Objects.equals(message,"success")){
                JSONObject resultData = ((JSONObject) JSONObject.parse(result)).getJSONObject("data");
                JSONArray resultJSONArray = resultData.getJSONArray("SalesRegionalSalesCatalog");
//                log.info("JSON->{}",JSON.toJSON(resultJSONArray));
                for (Object jsonObj : resultJSONArray) {
                    JSONObject jsonObject = (JSONObject)jsonObj;
                    String date = jsonObject.getString("SalesRegionalSalesCatalog_DATE");
                    Set<String> resultSet = new HashSet<>();
                    JSONArray jsonArray = jsonObject.getJSONArray("SalesRegionalSalesCatalog_ITEMS");
                    jsonArray.forEach(json2->{
                        JSONArray jsonArray3 =((JSONObject) json2).getJSONArray("COMP_ITEMS");
                        jsonArray3.forEach(json3->{
                            String code = ((JSONObject) json3).getString("ITEMCODE");
                            if(!code.endsWith("R")){
                                resultSet.add(code);
                            }
                        });
                    });
                    resultMap.put(date,resultSet);
                }

            }
        }
        return getInCodeData(resultMap);
    }

    private static List<InCodeData> getInCodeData(TreeMap<String,Set<String>> treeMap) {
        List<InCodeData> result = new ArrayList<>();
        TreeSet<String> keys = new TreeSet<>(treeMap.keySet());
        List<String> keysList = new TreeList<>(keys);
        for (int i = 1; i < keysList.size(); i++) {
            Set<String> value1 = treeMap.get(keysList.get(i-1));
            String firstDate = keysList.get(i);
            Set<String> value2 = treeMap.get(firstDate);
            value2.remove(value1);
            if(CollectionUtils.isNotEmpty(value2)){
                value2.forEach(v-> result.add(new InCodeData(){{
                    setCode(v);
                    setFirstDate(firstDate);
                }}));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        getInCodeFirstDate();
    }
}
