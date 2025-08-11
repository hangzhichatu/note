package longi.bom.service;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import longi.common.util.DomainRelationshipUtil;
import matrix.db.Context;
import matrix.util.StringList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: BOMService
 * @Description: BOM管理功能类, 协助处理供应商, 替代件维护
 * @author: Longi.suwei
 * @date: Jun 8, 2021 11:16:22 AM
 */
public class BOMManagerService {
    private static Map<String, String> typeMatchRelMap = new HashMap<String, String>();
    private static Map<String, String> typeMatchSubstituteCodeRelMap = new HashMap<String, String>();

    static {
        typeMatchRelMap.put("LONGiModuleEBOMBenchmarkCode", "EBOM");
        typeMatchRelMap.put("LONGiModuleSBOMFinishedProductCode", "LONGiModuleSBOM");
        typeMatchRelMap.put("LONGiModulePBOMBenchmarkCode", "LONGiModulePBOM");

        typeMatchSubstituteCodeRelMap.put("EBOM", "EBOM Substitute");
        typeMatchSubstituteCodeRelMap.put("LONGiModuleSBOM", "LONGiModuleSBOMSubstitute");
        typeMatchSubstituteCodeRelMap.put("LONGiModulePBOM", "LONGiModulePBOMSubstitute");

        //add by xuqiang 20210924
        typeMatchRelMap.put("LGiM_ModuleEBOMBenchmarkCode", "EBOM");
        typeMatchRelMap.put("LGiM_ModuleStandardBenchmarkCode", "LGiM_ModuleSCBOM");
        typeMatchRelMap.put("LGiM_ModuleCustomizedBenchmarkCode", "LGiM_ModuleSCBOM");
        typeMatchRelMap.put("LGiM_ModuleMBOMCode", "LGiM_ModuleMBOM");

        typeMatchSubstituteCodeRelMap.put("LGiM_ModuleSCBOM", "LGiM_ModuleSCBOMSubstitue");
        typeMatchSubstituteCodeRelMap.put("LGiM_ModuleMBOM", "LGiM_ModuleMBOMSubstitue");
        //add end
    }

    /**
     * @Author: Longi.suwei
     * @Title: addSupplier
     * @Description: 为原件或者替代件添加供应商
     * @param: @param context
     * @param: @param fromRelId BOM关系ID
     * @param: @param toSupId 供应商ID
     * @date: Jun 8, 2021 11:16:08 AM
     */
    public static void addSupplier(Context context, String fromRelId, String toSupId) throws FrameworkException {
        //先判断该供应商是否已存在
        String currentSupplierIdStr = MqlUtil.mqlCommand(context, false, "print connection " + fromRelId + " select frommid[LONGiModulePartSupplier].to.id dump", true);
        StringList currentSupplierIdList = FrameworkUtil.split(currentSupplierIdStr, ",");
        //不包含供应商,再添加
        if (!currentSupplierIdList.contains(toSupId)) {
            MqlUtil.mqlCommand(context, false, "add connection 'LONGiModulePartSupplier' fromrel " + fromRelId + " to " + toSupId + " select id dump", false);
        }
    }

    /**
     * @Author: Longi.suwei
     * @Title: addSuppliers
     * @Description: 为原件或者替代件添加供应商
     * @param: @param context
     * @param: @param fromRelId BOM关系ID
     * @param: @param toSupIds 供应商IDs
     * @date: Jun 8, 2021 11:16:09 AM
     */
    public static void addSuppliers(Context context, String fromRelId, StringList toSupIds) throws FrameworkException {
        //先判断该供应商是否已存在
        String currentSupplierIdStr = MqlUtil.mqlCommand(context, false, "print connection " + fromRelId + " select frommid[LONGiModulePartSupplier].to.id dump", true);
        StringList currentSupplierIdList = FrameworkUtil.split(currentSupplierIdStr, ",");
        //不包含供应商,再添加
        for (Iterator<String> iterator = toSupIds.iterator(); iterator.hasNext(); ) {
            String toSupId = iterator.next();
            if (!currentSupplierIdList.contains(toSupId)) {
                MqlUtil.mqlCommand(context, false, "add connection 'LONGiModulePartSupplier' fromrel " + fromRelId + " to " + toSupId + " select id dump", false);
            }
        }
    }

    /**
     * @Author: Longi.suwei
     * @Title: addSubstitute
     * @Description: 为原件添加替代件
     * @param: @param context
     * @param: @param fromRelId BOM关系ID
     * @param: @param toPartId 零件ID
     * @param: @param stituteRel 替代件关系名
     * @param: @param attributeMap 替代件属性集合
     * @date: Jun 8, 2021 11:16:12 AM
     */
    public static String addSubstitute(Context context, String fromRelId, String toPartId, String stituteRel, Map attributeMap) throws FrameworkException {
        //查询原件的替代件
        String strCommand = "print connection " + fromRelId + " select frommid[" + stituteRel
                + "].id frommid[" + stituteRel + "].attribute[Find Number] frommid[" + stituteRel + "].to.id dump |";
        String strMessage = MqlUtil.mqlCommand(context, false, strCommand, true);
        //用于计算最大序号
        String strCommand2 = "print connection " + fromRelId + " select frommid[" + stituteRel + "].attribute[Find Number] dump |";
        String strMessage2 = MqlUtil.mqlCommand(context, false, strCommand2, true);
        //所有的替代件序号
        StringList slEBOMSubNumbers = FrameworkUtil.split(strMessage2, "|");
        int maxNumber = 1;
        if (slEBOMSubNumbers != null && slEBOMSubNumbers.size() > 0) {
            slEBOMSubNumbers.sort();
            //取得最大序号,用于生成find Number
            maxNumber = Integer.parseInt(slEBOMSubNumbers.get(slEBOMSubNumbers.size() - 1));

        }
        //若替代件已存在
        if (strMessage.indexOf(toPartId) >= 0) {
            return "";
        }
        else {
            //该替代件不存在，新增替代件,返回替代件关系ID
            String repRelId = MqlUtil.mqlCommand(context, false,"add connection '" + stituteRel + "' fromrel " + fromRelId
                    + " to " + toPartId + " select id dump",false);
            DomainObject repObj = DomainObject.newInstance(context, toPartId);
            DomainRelationship tempRepRel = DomainRelationship.newInstance(context, repRelId);
//            tempRepRel.setAttributeValue(context, "LONGiModuleDesignUnitConsumption", dnc);
//            tempRepRel.setAttributeValue(context, "LONGiModuleDesignConsumptionRate", loss);
            //根据计算的位号进行位号设置
            if(attributeMap.containsKey("Find Number") && UIUtil.isNotNullAndNotEmpty(attributeMap.get("Find Number").toString())){
                attributeMap.put("Find Number",  Integer.toString(maxNumber++));
            }
            //使用Part的单位值,赋值
            attributeMap.put("Unit of Measure",repObj.getAttributeValue(context, "Unit of Measure"));
            tempRepRel.setAttributeValues(context,attributeMap);
            //String line = emxGetParameter(request, lineIdKey);
            //设置属性值

        }
        //所有的替代件
        StringList slEBOMSubs = FrameworkUtil.split(strMessage, "|");
        return "";
    }

    /**
     * @Author: Longi.suwei
     * @Title: getSuppliers
     * @Description: 获取供应商
     * @param: @param context
     * @param: @param relId BOM关系ID
     * @param: @return 所有供应商Id List
     * @throws FrameworkException
     * @date: Jun 8, 2021 11:16:16 AM
     */
    public static StringList getSuppliers(Context context, String relId) throws FrameworkException {
        String currentSupplierIdStr = MqlUtil.mqlCommand(context, false, "print connection " + relId + " select frommid[LONGiModulePartSupplier].to.id dump", true);
        StringList currentSupplierIdList = FrameworkUtil.split(currentSupplierIdStr, ",");
        return currentSupplierIdList;
    }

    /**
     * @Author: Longi.suwei
     * @Title: getSubstitutes
     * @Description: 获取所有替代件(自动根据find number 排序)
     * @param: @param context
     * @param: @param relId BOM关系ID
     * @param: @return 所有替代件Id List
     * @date: Jun 8, 2021 11:16:18 AM
     */
    public static StringList getSubstitutes(Context context, String relId) throws Exception {
//        DomainRelationship dr =  DomainRelationship.newInstance(context,relId);
//        String relName = DomainRelationshipUtil.getTypeName(context, dr);
//        String stituteRel = typeMatchSubstituteCodeRelMap.get(relName).toString();
//        //查询原件的替代件
//        String strCommand = "print connection " + relId + " select frommid[" + stituteRel
//                + "].id dump |";
//        String strMessage = MqlUtil.mqlCommand(context, false, strCommand, true);
//        //所有的替代件序号
//        StringList stituteRelList = FrameworkUtil.split(strMessage, "|");
        StringList resultList = new StringList();
        String[] relIds = {relId};
        StringList selectList = new StringList();
        String stituName = typeMatchSubstituteCodeRelMap.get(DomainRelationshipUtil.getTypeName(context,DomainRelationship.newInstance(context,relId)));
        selectList.addElement("frommid["+stituName+"].id");
        selectList.addElement("frommid["+stituName+"].attribute[Find Number]");
        MapList mpList = DomainRelationship.getInfo(context,relIds,selectList);
        MapList conMapList = new MapList();
        if(Objects.nonNull(mpList)){
            Map stituMap = (Map)mpList.get(0);
            try {
                StringList stituteIdList = (StringList) stituMap.get("frommid["+stituName+"].id");
                StringList stituteFindList = (StringList) stituMap.get("frommid["+stituName+"].attribute[Find Number]");
                for (int i = 0; i < stituteIdList.size(); i++) {
                    Map<String,String> tempMap = new HashMap<String,String>();
                    String tempId = stituteIdList.get(i);
                    String tempFindNumber = stituteFindList.get(i);
                    tempMap.put("tempId",tempId);
                    tempMap.put("tempFindNumber",tempFindNumber);
                    conMapList.add(tempMap);
                }
                conMapList.addSortKey("tempFindNumber","ascending","integer");
                conMapList.sort();
                resultList = EngineeringUtil.getValueForKey(conMapList,"tempId");
            } catch (ClassCastException e) {
                String stituteId = (String) stituMap.get("frommid["+stituName+"].id");
                resultList.addElement(stituteId);
            }
        }
        return resultList;
    }

}
