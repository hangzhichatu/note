package longi.module.pdm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.matrixone.apps.program.ProgramCentralUtil;
import matrix.db.Context;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LONGiModuleJacksonUtil {
    private static Context m_ctx;
    static ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper mapperLowerCaseWithUnderScores = new ObjectMapper();

    //在这里进行配置全局
    static {
        //有时JSON字符串中含有我们并不需要的字段，那么当对应的实体类中不含有该字段时，会抛出一个异常，告诉你有些字段（java 原始类型）没有在实体类中找到
        //设置为false即不抛出异常，并设置默认值 int->0 double->0.0
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapperLowerCaseWithUnderScores.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //序列化时添加下划线
        mapperLowerCaseWithUnderScores.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());
    }
    public LONGiModuleJacksonUtil() {
    }


    /**
     * @param @param  object
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: beanToJson
     * @Description: TODO(Bean对象转JSON)
     * @author suw@avic-digital.com
     * @date 2019年10月14日
     */
    public static String beanToJson(Object object) {
        //json转bean时忽略大小写
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    /**
     * @param @param  object
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: beanToJson
     * @Description: TODO(Bean对象转JSON, OA人员组织结构专用, 首字母存在大写字段)
     * @author suw@avic-digital.com
     * @date 2019年10月14日
     */
    public static String beanToJsonLongiOA(Object object) {
        //json转bean时忽略大小写
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        String jsonStr = "";
        Map tempMap = null;
        try {
            jsonStr = mapper.writeValueAsString(object);
            tempMap = jsonToMap(jsonStr,String.class,String.class);
            if (tempMap.containsKey("loginName")) {
                tempMap.remove("loginName");
            }
            jsonStr = mapper.writeValueAsString(tempMap);
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
     * @param @param  key
     * @param @param  value
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: stringToJsonByFastjson
     * @Description: TODO(String转JSON字符串)
     * @author suw@avic-digital.com
     * @date 2019年10月14日
     */
    public static String stringToJsonByFastjson(String key, String value) {
        if (ProgramCentralUtil.isNullString(key) || ProgramCentralUtil.isNullString(value)) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return beanToJson(map);
    }

    /**
     * @param @param  json
     * @param @param  clazz
     * @param @return 参数
     * @return Object    返回类型
     * @throws
     * @Title: jsonToBean
     * @Description: TODO(将json字符串转换成对象)
     * @author suw@avic-digital.com
     * @date 2019年10月14日
     */
    public static <T> T jsonToBean(String json, Class<T> classType) {
        if (StringUtils.isNotBlank(json) && classType != null) {
            try {
                return mapper.readValue(json, classType);
            } catch (IOException e) {
                System.out.println("json to bean exception");
            }
        }
        return null;
    }

    public static <T> List<T> jsonToList(String json, Class<T> classType) {
        if (StringUtils.isNotBlank(json) && classType != null) {
            try {
                return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, classType));
            } catch (IOException e) {
                System.out.println("json to list exception");
            }
        }
        return null;
    }

    /**
     * @param @param  json
     * @param @return 参数
     * @return Map<String, Object>    返回类型
     * @throws
     * @Title: jsonToMap
     * @Description: TODO(json字符串转map)
     * @author suw@avic-digital.com
     * @date 2019年10月14日
     */
    public static <k, v> Map<k, v> jsonToMap(String json, Class<k> kType, Class<v> vType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kType, vType));
            } catch (IOException e) {
                System.out.println("json to Map exception");
            }
        }
        return null;
    }
    public static String beanToJsonByLowerCase(Object object) {
        if (object != null) {
            try {
                return mapperLowerCaseWithUnderScores.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                System.out.println("bean to json with lowerCase exception");
            }
        }
        return "";
    }

    public static <T> T jsonToBeanByLowerCase(String json, Class<T> classType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return mapperLowerCaseWithUnderScores.readValue(json, classType);
            } catch (IOException e) {
                System.out.println("json to Bean with lowerCase exception");
            }
        }
        return null;
    }

    // 将对象转成字符串
    public static String objectToString(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }
    // 将Map转成指定的Bean
    public static Object mapToBean(Map map, Class clazz) throws Exception {
        return mapper.readValue(objectToString(map), clazz);
    }
    // 将Bean转成Map
    public static Map beanToMap(Object obj) throws Exception {
        return mapper.readValue(objectToString(obj), Map.class);
    }
}
