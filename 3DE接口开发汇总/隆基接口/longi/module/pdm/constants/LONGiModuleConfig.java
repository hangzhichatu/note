package longi.module.pdm.constants;
import longi.common.util.LONGiPropertiesUtil;
import matrix.util.StringList;


/** 
* @ClassName: LONGiModuleConfig
* @Description: TODO 系统参数配置
* @author: Longi.suwei
* @date: Jun 15, 2020 5:42:34 PM
*/

public class LONGiModuleConfig {
//	public final static String CTX_HOST = "http://dev.atoz.com:8070/internal";
	public final static String CTX_HOST = LONGiPropertiesUtil.readInterfaceProperties("CTX_HOST");
	public final static String CTX_USERNAME = LONGiPropertiesUtil.readInterfaceProperties("CTX_USERNAME");
	public final static String CTX_PASSWORD = LONGiPropertiesUtil.readInterfaceProperties("CTX_PASSWORD");
	public final static String CTX_VAULT = "eService Production";
//	public final static String CTX_VAULT = "eService Administration";
	public final static String CTX_ROLE = "VPLMProjectLeader";
//	public final static String CTX_ORG = "Company Name";
	public final static String CTX_ORG = "LONGi";
	public final static String CTX_PRJ = "Common Space";
	
	public final static String CTX_CONTEXT = "ctx::" + CTX_ROLE + "." + CTX_ORG + "." + CTX_PRJ;
	
	
	
	public final static String BPM_ROUTE_FILE_PATH = "/dsplm/temp/bpm/upload/";
	public final static String PRODUCT_DOWNLOAD_FILE_PATH = "/dsplm/temp/product/download/";
	public final static String ROUTE_DOWNLOAD_FILE_PATH = "/dsplm/temp/route/download/";
	public final static String YZFCS_DOWNLOAD_FILE_PATH = "/dsplm/temp/yzfcs/download/";
	//for test
//	public final static String BPM_ROUTE_FILE_PATH = "D:\\suwei3\\Desktop\\TEMP";
	
	public final static String CAS_NAVI_URL = LONGiPropertiesUtil.readInterfaceProperties("CAS_NAVI_URL");
	public static final String INIT_FAST_PRODUCT_FOLDERS_XML_PATH = LONGiPropertiesUtil.readInterfaceProperties("INIT_FAST_PRODUCT_FOLDERS_XML_PATH");;
	//	public final static String CAS_NAVI_URL = "https://plmdev.longi-silicon.com/3dspace/common/emxNavigator.jsp?objectId=";
	public static StringList CONTROLED_TYPE_LIST = new StringList();
	static {
		CONTROLED_TYPE_LIST.addElement("ACAD Drawing");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductDrawings");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductSpecification");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductQualityStandards");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductBOM");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductInstallationManual");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductTechnicalStandards");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleMaterialTechnicalStandard");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleDFMEA");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleBarcodeCodingMarkNameplateFormat");
		CONTROLED_TYPE_LIST.addElement("LONGiModulePackagingDrawings");
		CONTROLED_TYPE_LIST.addElement("LONGiModuleProductLoadingSpecifications");
		CONTROLED_TYPE_LIST.addElement("LONGiModulePackingAndUnpackingOperationManual");
	}

	public static StringList CONTROLED_FAST_TYPE_LIST = new StringList();
	static {

		CONTROLED_FAST_TYPE_LIST.addElement("ACAD Drawing");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleProductDrawings");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleProductQualityStandards");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleMaterialTechnicalStandard");
		//缺少类型：变更点提示及对应方案建议
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleChangePlanDocument");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleProcessDocuments");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleBarcodeCodingMarkNameplateFormat");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModulePackagingDrawings");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleProductLoadingSpecifications");
		//新增包装拆箱作业手册为受控文件
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModulePackingAndUnpackingOperationManual");
		//缺少类型组件电流分档规则
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleGradingRulesDocument");
		CONTROLED_FAST_TYPE_LIST.addElement("LONGiModuleDFMEA");
	}

	public static StringBuilder CONTROLED_FAST_TYPE_LIST_SELECT_TYPE = new StringBuilder();
	static{
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("ACAD Drawing,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleProductDrawings,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleProductQualityStandards,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleMaterialTechnicalStandard,");
		//缺少类型：变更点提示及对应方案建议
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleChangePlanDocument,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleProcessDocuments,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleBarcodeCodingMarkNameplateFormat,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModulePackagingDrawings,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleProductLoadingSpecifications,");
		//新增包装拆箱作业手册为受控文件
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModulePackingAndUnpackingOperationManual,");
		//缺少类型组件电流分档规则
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleGradingRulesDocument,");
		CONTROLED_FAST_TYPE_LIST_SELECT_TYPE.append("LONGiModuleDFMEA");


	}

	// 表示定义数据库的用户名
	public final static String BOMDB_USERNAME = LONGiPropertiesUtil.readInterfaceProperties("BOMDB_USERNAME");
	// 定义数据库的密码
	public final static  String BOMDB_PASSWORD = LONGiPropertiesUtil.readInterfaceProperties("BOMDB_PASSWORD");
	// 定义数据库的驱动信息
	public final static  String BOMDB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	// 定义访问数据库的地址
	public final static  String BOMDB_URL = LONGiPropertiesUtil.readInterfaceProperties("BOMDB_URL");
	
	public final static String CODE_SYS_NAVI_URL = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_NAVI_URL");
	public final static String CODE_SYS_NAVI_APP_URL = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_NAVI_APP_URL");
	
	// 外购物料SRM编码信息获取
	public final static String CODE_SYS_MDM_SRM_URL = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYS_MDM_SRM_URL");
}
