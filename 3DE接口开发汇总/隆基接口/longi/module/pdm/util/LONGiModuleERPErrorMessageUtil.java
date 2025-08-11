package longi.module.pdm.util;

import java.util.HashMap;
import java.util.Map;

public class LONGiModuleERPErrorMessageUtil {
    private static Map errorMessageMap = new HashMap();

    public static void setERPErrorMessage(String objectId, String errorSeq, String errorInfo) {
        errorMessageMap.put(objectId + "_" + errorSeq, errorInfo);
    }

    public static String getERPErrorMessage(String objectId, String errorSeq) {
        if (errorMessageMap.containsKey(objectId + "_" + errorSeq)) {
            return errorMessageMap.get(objectId + "_" + errorSeq).toString();
        } else {
            return "";
        }
    }
}
