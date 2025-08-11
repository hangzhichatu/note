package longi.common.constants;

import longi.common.util.LONGiPropertiesUtil;

public class GlobalConfig {
	public final static String REDIS_SERVER_IP = LONGiPropertiesUtil.readInterfaceProperties("REDIS_SERVER_IP");
	public final static String REDIS_SERVER_PASSWORD = LONGiPropertiesUtil.readInterfaceProperties("REDIS_SERVER_PASSWORD");
	// 定义数据库的驱动信息
	public final static  String ORACLE_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";

	// 表示定义数据库的用户名
	public final static String LIMS_DB_USERNAME = LONGiPropertiesUtil.readInterfaceProperties("LIMS_DB_USERNAME");
//	public final static String LIMS_DB_USERNAME = "plmquery";
//	public final static String LIMS_DB_USERNAME = "limstest";
	// 定义数据库的密码
	public final static  String LIMS_DB_PASSWORD = LONGiPropertiesUtil.readInterfaceProperties("LIMS_DB_PASSWORD");
//	public final static  String LIMS_DB_PASSWORD = "lims2022#PLM";
//	public final static  String LIMS_DB_PASSWORD = "limstest2021";
	// 定义数据库的驱动信息
//	public final static  String LIMS_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public final static  String LIMS_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	// 定义访问数据库的地址
	public final static  String LIMS_DB_URL = LONGiPropertiesUtil.readInterfaceProperties("LIMS_DB_URL");
//	public final static  String LIMS_DB_URL = "jdbc:oracle:thin:@10.0.88.112:1521:limstest";
	
//	public final static  String LIMS_DB_URL = "jdbc:oracle:thin:@prd-limsdb.longi.com:1521:LIMS";
}
