package longi.module.pdm.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.enovia.e6wv2.foundation.db.PropertyUtil;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.matrixone.apps.program.ProgramCentralUtil;

import longi.module.pdm.model.LONGiModuleTargetBean;
import matrix.db.Context;

public class LONGiModuleFastJsonUtil {
	private static Context m_ctx;
	ObjectMapper mapper = new ObjectMapper();
	public LONGiModuleFastJsonUtil() {
	}
	  /**  
	* @Title: beanToJson  
	* @Description: TODO(Bean对象转JSON)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param object
	* @param @param dataFormatString
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/  
	public static String beanToJson(Object object, String dataFormatString) {

		if (object != null) {
			if (ProgramCentralUtil.isNullString(dataFormatString)) {
				
				return JSONObject.toJSONString(object);
			}
			return JSON.toJSONStringWithDateFormat(object, dataFormatString);
		} else {
			return null;
		}
	}

	/**  
	* @Title: beanToJson  
	* @Description: TODO(Bean对象转JSON)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param object
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/  
	public static String beanToJson(Object object) {
		com.fasterxml.jackson.databind.ObjectMapper ob =new com.fasterxml.jackson.databind.ObjectMapper();
		//json转bean时忽略大小写
//		ob.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,true);
		String jsonStr = "";
		try {
			jsonStr = ob.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
//		if (object != null) {
//			return JSON.toJSONString(object);
//		} else {
//			return null;
//		}
	}

	/**
	 * @Title: beanToJson
	 * @Description: TODO(Bean对象转JSON,OA人员组织结构专用,首字母存在大写字段)
	 * @author suw@avic-digital.com
	 * @date 2019年10月14日
	 * @param @param object
	 * @param @return    参数
	 * @return String    返回类型
	 * @throws
	 */
	public static String beanToJsonLongiOA(Object object) {
		com.fasterxml.jackson.databind.ObjectMapper ob =new com.fasterxml.jackson.databind.ObjectMapper();
		//json转bean时忽略大小写
//		ob.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,true);
		String jsonStr = "";
		Map tempMap = null;
		try {
			jsonStr = ob.writeValueAsString(object);
			tempMap = jsonToMap(jsonStr);
			if(tempMap.containsKey("loginName")){
				tempMap.remove("loginName");
			}
			jsonStr = ob.writeValueAsString(tempMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
//		if (object != null) {
//			return JSON.toJSONString(object);
//		} else {
//			return null;
//		}
	}
	/**  
	* @Title: stringToJsonByFastjson  
	* @Description: TODO(String转JSON字符串)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param key
	* @param @param value
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/  
	public static String stringToJsonByFastjson(String key, String value) {
		if (ProgramCentralUtil.isNullString(key) || ProgramCentralUtil.isNullString(value)) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put(key, value);
		return beanToJson(map, null);
	}

	/**  
	* @Title: jsonToBean  
	* @Description: TODO(将json字符串转换成对象)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param json
	* @param @param clazz
	* @param @return    参数  
	* @return Object    返回类型  
	* @throws  
	*/  
	public static Object jsonToBean(String json, Object clazz) {
		if (ProgramCentralUtil.isNullString(json) || clazz == null) {
			return null;
		}
		return JSON.parseObject(json, clazz.getClass());
	}

	/**  
	* @Title: jsonToMap  
	* @Description: TODO(json字符串转map)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param json
	* @param @return    参数  
	* @return Map<String,Object>    返回类型  
	* @throws  
	*/  
	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsonToMap(String json) {
		if (ProgramCentralUtil.isNullString(json)) {
			return null;
		}
		return JSON.parseObject(json, Map.class);
	}
	
	/**
	   * 转换bean为map
	   *
	   * @param source 要转换的bean
	   * @param <T>    bean类型
	   * @return 转换结果
	   */
	public static <T> Map<String, Object> bean2Map(T source) throws IllegalAccessException {
		Map<String, Object> result = new HashMap<>();

		Class<?> sourceClass = source.getClass();
		// 拿到所有的字段,不包括继承的字段
		Field[] sourceFiled = sourceClass.getDeclaredFields();
		for (Field field : sourceFiled) {
			field.setAccessible(true);// 设置可访问,不然拿不到private
			// 配置了注解的话则使用注解名称,作为header字段
			LONGiModuleFieldName fieldName = field.getAnnotation(LONGiModuleFieldName.class);
			if (fieldName == null) {
				if(!field.getName().startsWith("attribute_")) {
					continue;
				}
				String schAttrName = PropertyUtil.getSchemaName(m_ctx, field.getName());
				if(ProgramCentralUtil.isNotNullString(schAttrName) && ProgramCentralUtil.isNotNullString(field.get(source).toString())) {
					result.put(schAttrName, field.get(source).toString());
				}else {
					continue;
				}
			} else {
				if (fieldName.Ignore())
					continue;
				result.put(fieldName.value(), field.get(source));
			}
		}
		return result;
	}
	  
	  /**
	   * map转bean
	   * @param source   map属性
	   * @param instance 要转换成的备案
	   * @return 该bean
	   */
	public static <T> T map2Bean(Map<String, Object> source, Class<T> instance) {
		try {
			T object = instance.newInstance();
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				LONGiModuleFieldName fieldName = field.getAnnotation(LONGiModuleFieldName.class);
				if (fieldName != null) {
					field.set(object, source.get(fieldName.value()));
				} else {
					field.set(object, source.get(field.getName()));
				}
			}
			return object;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
