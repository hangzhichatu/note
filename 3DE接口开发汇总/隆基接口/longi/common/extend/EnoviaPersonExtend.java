package longi.common.extend;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.util.PropertyUtil;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.ExpansionWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.util.Pattern;
import matrix.util.SelectList;

/** 
* @ClassName: EnoviaPersonExtend
* @Description: 用于添加3de人员相关的一些扩展工具类
* @author: Longi.suwei
* @date: 2021 Oct 29 09:17:51
*/
public class EnoviaPersonExtend extends Person{
    /**
	* @Fields serialVersionUID : 
	* @author: Longi.suwei 
	* @date 2021 Oct 29
	*/
	private static final long serialVersionUID = -7579929049478496375L;
	public EnoviaPersonExtend(){
        super();
    }
    /**
    * @Author: Longi.suwei
    * @Title: getPersonBusinessUnit
    * @Description: 获取人的最底层业务单位名称
    * @param: @param context
    * @param: @param personName
    * @param: @return
    * @param: @throws Exception
    * @date: 2021 Oct 29 09:18:25
    */
    @SuppressWarnings("deprecation")
	public static String getPersonBusinessUnit(Context context,String personName) throws Exception {
        Person personObj = Person.getPerson(context,personName);
        ExpansionWithSelect personSelect    = null;
        BusinessObject boGeneric            = null;
        RelationshipWithSelectItr relItr    = null;
        SelectList selectRelStmts = new SelectList();
        boGeneric = new BusinessObject(personObj.getObjectId(context));
        boGeneric.open(context);
        String sRelBusUnitEmp   = PropertyUtil.getSchemaProperty(context,"relationship_Member");
        String sBusinessUnit    = PropertyUtil.getSchemaProperty(context,"type_BusinessUnit");
        Pattern relPattern  = new Pattern(sRelBusUnitEmp);
        Pattern typePattern = new Pattern(sBusinessUnit);
        // build select params
        SelectList selectStmts  = new SelectList();
        selectStmts.addName();
        selectStmts.addDescription();
        personSelect = boGeneric.expandSelect(context,relPattern.getPattern(),typePattern.getPattern(),
                selectStmts, selectRelStmts, true, false, (short)1);
        // check the folder Member relations
        relItr = new RelationshipWithSelectItr(personSelect.getRelationships());
        boGeneric.close(context);

        // loop thru the rels and get the folder object
        // get all the document objects connected to project vault
        BusinessObject boUnit = null;
        String sId = "";
        String sUnitName = "";
        while (relItr != null && relItr.next())
        {
            boUnit  = relItr.obj().getFrom();
            sId = (String)boUnit.getObjectId();
            boGeneric = new BusinessObject(sId);
            boGeneric.open(context);
            sUnitName = boGeneric.getName();
            boGeneric.close(context);
        }
        return sUnitName;
    }
}
