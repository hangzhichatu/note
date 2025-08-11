package longi.module.pdm.util;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;

import longi.module.pdm.impl.LONGiModuleProjectTaskCompleteDoneToDoAction;
import matrix.db.Context;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class LONGiModuleCommonTools {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleCommonTools.class);

    public static String genIdTimesLong(String id) {
//		return id + "_" + Long.toString(new Date().getTime());
        return Long.toString(new Date().getTime());
    }

    public static String getLanguage(String language) {
        if ("zh".equals(language)) {
            return "zh_CN";
        } else {
            return language;
        }
    }

    /**
    * @Author: Longi.suwei
    * @Title: getProductType 获取文档或者图纸的产品类型
    * @Description: TODO
    * @param: @param context
    * @param: @param doc
    * @param: @return
    * @param: @throws FrameworkException
    * @date: Nov 13, 2020 3:17:29 PM
    */
    public static int getProductType(Context context, DomainObject doc) throws FrameworkException {
        logger.info("获取文档或者图纸的产品类型方法：" + doc);
        boolean subFolder = true;
        try {
            String folderId = doc.getInfo(context, "to[Vaulted Objects].from.id");
            DomainObject folder = DomainObject.newInstance(context, folderId);
            while (subFolder) {
                String fatherId = folder.getInfo(context, "to[Sub Vaults].from.id");
                if (UIUtil.isNotNullAndNotEmpty(fatherId)) {
                    folder = DomainObject.newInstance(context, fatherId);
                } else {
                    subFolder = false;
                }
            }

            String type = folder.getInfo(context, "to[Data Vaults].from.type");
            if (UIUtil.isNotNullAndNotEmpty(type) && type.equals("LONGiModuleStandardProduct")) {
                return 1;
            } else if (type.equals("LONGiModuleNonStandardOrder")) {
                return 2;
            } else if (type.equals("LONGiModuleNonStandardProduct")) {
                return 3;
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error("获取文档或者图纸的产品类型方法异常："+e.toString());
            return 0;
        }
    }

    /**
    * @Author: Longi.suwei
    * @Title: convertVersionToLONGiStandard 用于转换版本号 1->V01...V999
    * @Description: TODO
    * @param: @param revision
    * @param: @return
    * @date: Nov 6, 2020 11:29:21 AM
    */
    public static String convertVersionToLONGiStandard(String revision) {
        int num = Integer.parseInt(revision);
        num++;
        if(num<10){
            revision = "V0";
        }else{
            revision = "V";
        }
        return revision + Integer.toString(num);
    }
}
