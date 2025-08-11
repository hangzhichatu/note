package longi.module.pdm.util;


import com.dassault_systemes.platform.restServices.DiscoveryServices;
import com.matrixone.apps.domain.DomainObject;

import longi.module.pdm.constants.LONGiModuleConfig;
import matrix.db.Context;
import matrix.db.Policy;
import matrix.db.StateRequirement;
import matrix.db.StateRequirementList;
import matrix.util.MatrixException;

public class LONGiModuleUserContext{
	 private static Context m_ctx;
//	 private static final Logger logger = LoggerConfigUtil.getLogger();
	/**  
	* @Title: getContext  
	* @Description: TODO(单例模式的上下文构造)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @return    参数  
	* @return Context    返回类型  
	* @throws  
	*/  
	public static Context getContext(){
		if(null == m_ctx || !m_ctx.checkContext()){
			try{
				m_ctx = new Context(LONGiModuleConfig.CTX_HOST);
				m_ctx.setUser(LONGiModuleConfig.CTX_USERNAME);
				m_ctx.setPassword(LONGiModuleConfig.CTX_PASSWORD);
				m_ctx.setVault(LONGiModuleConfig.CTX_VAULT);
				m_ctx.setRole(LONGiModuleConfig.CTX_CONTEXT);
				m_ctx.connect();
			} catch (Exception e) {
//				logger.error("Initialize Context fail");
				m_ctx.close();
				m_ctx = null;
				e.printStackTrace();
			}
		}
		
		return m_ctx;
	}
	
		
	//
	public static void setContext(Context context) {
		if(null == m_ctx || !m_ctx.checkContext()){
			m_ctx = context;
		}
	}

	public static void main(String[] args) {
		getContext();
		System.out.println("~------------"+m_ctx.getRole());
		try {
			String strRouteId = "37544.43048.37898.20245";
//			m_ctx.resetVault("eService Administration");
//			System.out.println(m_ctx.getVault().getName());
//	        DomainObject domainObject = DomainObject.newInstance(m_ctx,strRouteId);
//	        System.out.println(domainObject.getAttributeMap(m_ctx));
//	        domainObject.promote(m_ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
