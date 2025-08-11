package longi.common.util;

import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;

import com.matrixone.apps.domain.util.MqlUtil;
import longi.module.pdm.constants.LONGiModuleConfig;
import matrix.db.Context;
import matrix.util.StringList;


import java.util.Hashtable;

public class DomainRelationshipUtil{

    
	public static String getTypeName(Context context, DomainRelationship dr) throws Exception {
        Hashtable relTypeNametable = dr.getRelationshipData(context, new StringList("type")) ;
        StringList relTypeNameList = (StringList)relTypeNametable.get("type");
        String relTypeName = relTypeNameList.get(0);
	    return relTypeName;
    }
	public static String getFromId(Context context, String relId) throws Exception {
		String strCommand = "print connection " + relId + " select to.id dump ";
		return MqlUtil.mqlCommand(context, false, strCommand, true);
    }
	public static String getToId(Context context, String relId) throws Exception {
		String strCommand = "print connection " + relId + " select to.id dump ";
		return MqlUtil.mqlCommand(context, false, strCommand, true);
    }

//    public static void main(String[] args) {
//        String partId = "30103.58115.13352.10131";
//        Context m_ctx = null;
//        if(null == m_ctx || !m_ctx.checkContext()){
//            try{
//                m_ctx = new Context("http://10.0.88.87:8070/internal");
//                m_ctx.setUser("admin_platform");
//                m_ctx.setPassword("Aa123456");
//                m_ctx.setVault(LONGiModuleConfig.CTX_VAULT);
//                m_ctx.setRole(LONGiModuleConfig.CTX_CONTEXT);
//                m_ctx.connect();
//            } catch (Exception e) {
////				logger.error("Initialize Context fail");
//                m_ctx.close();
//                m_ctx = null;
//                e.printStackTrace();
//            }
//            System.out.println("sucess connect");
//        }
//        try {
//			DomainRelationship drDomainRelationship = DomainRelationship.newInstance(m_ctx, partId);
//			System.out.println(drDomainRelationship.getTo().getObjectId());
//        } catch (FrameworkException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
}
