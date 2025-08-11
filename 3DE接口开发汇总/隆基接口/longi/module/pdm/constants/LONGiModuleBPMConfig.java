package longi.module.pdm.constants;

import longi.common.util.LONGiPropertiesUtil;
import matrix.util.StringList;

/**
* @ClassName: RouteConfig
* @Description: TODO 流程相关的静态变量
* @author: Longi.suwei
* @date: Jun 17, 2020 9:48:39 PM
*/
public class LONGiModuleBPMConfig {
	/**
	* @Fields PROJECT_NAME : TODO
	*/
	public final static String PROJECT_NAME = "组件研发PDM系统";
	public final static String SYS_TODO_CREATOR = "";

	public final static String PROJECT_MEMBER_NOTIFY = "您已被项目经理添加为项目成员，请查阅";
	public final static String PROJECT_MEMBER_COMPLETE_NOTIFY = "您所参与的项目已到达完成状态，请查阅";
	public final static String TASK_INWORK_MESSAGE = "任务工作已分配,请及时完成相关任务工作";
	public final static String TASK_REJECT_BY_MANAGER = "任务复核被驳回";
	public final static String TASK_REVIEW_MESSAGE = "任务分派人已完成任务工作，请及时复核完成情况";
	public static final String TASK_ASSIGNEE_MESSAGE = "您已被项目经理分配此任务，请及时沟通并完成该任务工作";

	public static final String SERVER_APP = "PLM";

	public static final String SERVER_MODULE = "PDM";
    public static final String PROJECT_OBJECT_RELEASED = "您的该数据已达到发布状态，请查阅";

    public static final String FD_TEMPLATEID = "172dfbb3af3f0d1cea8263645dda3fdb";
    public static final String OBJECT_RELEASE_NOTIFY = "数据发布通知";

	public final static String PROJECT_CONTROL_ORDER_REJECT_MESSAGE = "受控流程被驳回，请修订后重新提交";
	public final static String CODE_SYSTEM_REQUEST_APPROVE_MESSAGE = "请审核";
    public static final String CODE_SYSTEM_REQUEST_OWNER_REJECT_MESSAGE = "已驳您的编码申请单，请修订";
    //编码系统采购经理访问的对象Id
	public static final String CODE_SYSTEM_MANAGER_ID = LONGiPropertiesUtil.readInterfaceProperties("CODE_SYSTEM_MANAGER_ID");
    public static final StringList SOLVE_PORGRAM_LIST = new StringList();
    public static final String ROUTE_STOP_MESSAGE = "流程已被驳回或者停止，请及时查阅并处理";
    //add by xuqiang
	public static final String PROJECT_OBJECT_RELEASED_Notice = "标准定制BOM已达到发布状态，请查阅";
    static {
		SOLVE_PORGRAM_LIST.addElement("NewProject");
		SOLVE_PORGRAM_LIST.addElement("ChangeProject");
		SOLVE_PORGRAM_LIST.addElement("JustTechSpec");
		SOLVE_PORGRAM_LIST.addElement("QuickImportScheme");
	}
//    public static final String[] TECH_FILE_TYPE = {"MaterialDrawings","ProductDrawings","TechnicalStandards","FamousBrands/Marks","BOM","ProductSpecifications","ProductionManuals","CodingRules","QuickImportScheme","Other"};

    public static StringList TECH_FILE_TYPE_LIST = new StringList();
    static {
		TECH_FILE_TYPE_LIST.addElement("MaterialDrawings");
		TECH_FILE_TYPE_LIST.addElement("ProductDrawings");
		TECH_FILE_TYPE_LIST.addElement("TechnicalStandards");
		TECH_FILE_TYPE_LIST.addElement("FamousBrands/Marks");
		TECH_FILE_TYPE_LIST.addElement("BOM");
		TECH_FILE_TYPE_LIST.addElement("ProductSpecifications");
		TECH_FILE_TYPE_LIST.addElement("ProductionManuals");
		TECH_FILE_TYPE_LIST.addElement("CodingRules");
		TECH_FILE_TYPE_LIST.addElement("QuickImportScheme");
		TECH_FILE_TYPE_LIST.addElement("Other");
	}
	public static StringList DIS_RELEASE_NOTIFY_TYPE = new StringList();
    static {
    	//研发成品编码
		DIS_RELEASE_NOTIFY_TYPE.addElement("LONGiModuleEBOMFinishedProductCode");
		//组件成品编码
		DIS_RELEASE_NOTIFY_TYPE.addElement("LONGiModulePBOMFinishedProductCode");
		//包装虚拟件(标准&定制)
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModulePackVirtualPart_SC");

		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_BatteryModule");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_BeforeLaminationModule");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_AfterLaminationModule");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_LabelModule");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_MatchModule");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModulePackVirtualPart_RD");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModulePackVirtualPart_MBOM");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModulePackVirtualPart_PlanBOM");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModulePackVirtualPart_FastPlan");

		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModuleStandardBenchmarkCode");
		DIS_RELEASE_NOTIFY_TYPE.addElement("LGiM_ModuleCustomizedBenchmarkCode");






	}
//	public final static String BPM_URL = "http://i.longi-silicon.com:8888/km/review/km_review_main/kmReviewMain.do?method=view&fdId=";
	public final static String BPM_URL = LONGiPropertiesUtil.readInterfaceProperties("BPM_URL");

//    public final static String TODO_AXIS_WSDL = "http://i.longi-silicon.com:8888/sys/webservice/sysNotifyTodoWebService?wsdl";
    public final static String TODO_AXIS_WSDL = LONGiPropertiesUtil.readInterfaceProperties("TODO_AXIS_WSDL");
    public final static String ROUTE_AXIS_WSDL = LONGiPropertiesUtil.readInterfaceProperties("ROUTE_AXIS_WSDL");

    
    public final static String AUTH_USERNAME = LONGiPropertiesUtil.readInterfaceProperties("BPM_AUTH_USERNAME");
    public final static String AUTH_PASSWORD = LONGiPropertiesUtil.readInterfaceProperties("BPM_AUTH_PASSWORD");
	public static final String AUTH_HEADER = "http://sys.webservice.client";
    public final static int AIXS_TIMEOUT = 1000 * 60 * 20;


    // public final static String ROBERT_USER_NAME = "lihao30";
	public final static String ROBERT_USER_NAME = "plmcodeuser";
    public final static String ROBERT_USER_PASSWORD = "Aa123456";

	public final static String CODE_SYSTEM_APPLICATION_TAG = "OWNER";
    public final static String CODE_SYSTEM_PM_TAG = "PM";

}
