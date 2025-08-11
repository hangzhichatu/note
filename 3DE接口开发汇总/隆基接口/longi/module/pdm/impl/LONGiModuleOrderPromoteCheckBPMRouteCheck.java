package longi.module.pdm.impl;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;

import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import matrix.db.Context;
import matrix.util.StringList;

import java.util.Map;

/**
 * @ClassName: LONGiModuleOrderPromoteCheckBPMRouteCheck
 * @Description: TODO 检查受控单文件数量和文件大小合规,数量小于80,大小小于50M
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleOrderPromoteCheckBPMRouteCheck implements LONGiModuleOASendToDoInterface {
    @Override
    public int sendToDo(Context context, Map attrMap) {
        // TODO Auto-generated method stub
        /*String id = attrMap.get("objId").toString();
        System.out.println(id);
        DomainObject order = null;
        try {
            order = DomainObject.newInstance(context, id);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
        String relationshipPattern = "LONGiControlledOrderRelationship";
        String typePattern = "*";
        StringList objectSelects = new StringList();
        objectSelects.addElement("name");
        objectSelects.addElement("id");
        objectSelects.addElement("revision");
        objectSelects.addElement("owner");
        objectSelects.addElement("modified");
        objectSelects.addElement("attribute[LONGiModuleProjectDocumentFormalControlledNumber]");
        StringList relationshipSelects = new StringList();
        boolean getTo = false;
        boolean getFrom = true;
        short recurseToLevel = 1;
        String objectWhere = "";
        String relationshipWhere = "";
        int limit = 0;
        MapList fileList = null;
        try {
            fileList = order.getRelatedObjects(context, relationshipPattern, typePattern, objectSelects, relationshipSelects, getTo, getFrom, recurseToLevel, objectWhere, relationshipWhere, limit);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }

        System.out.println(fileList);*/
//
        return 0;
    }

}
