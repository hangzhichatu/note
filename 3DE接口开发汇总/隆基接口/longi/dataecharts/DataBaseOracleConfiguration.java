package longi.dataecharts;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import longi.common.util.LONGiPropertiesUtil;

@Data
@Slf4j
public class DataBaseOracleConfiguration {

    /*private static String userValueErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_USERNAME");
    private static String passwordValueErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_PASSWORD");
    private static String urlValueErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_URL");

    private static String userValueWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_USERNAME");
    private static String passwordValueWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_PASSWORD");
    private static String urlValueWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_URL");

    private static String userValueOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_USERNAME");
    private static String passwordValueOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_PASSWORD");
    private static String urlValueOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_URL");

    private static String userValueDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_USERNAME");
    private static String passwordValueDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_PASSWORD");
    private static String urlValueDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_URL");*/
	private static String userValueErp="LGIPLM";
    private static String passwordValueErp="plm#LGI#202305201341";
    private static String urlValueErp="jdbc:oracle:thin:@erp-prod-dg.longi.com:1521:PROD";

    private static String userValueWms="plmquery";
    private static String passwordValueWms="longi#2023quePLM";
    private static String urlValueWms="jdbc:oracle:thin:@wmsdb-st.longi.com:1521:WMSDB";

    private static String userValueOracle41="plmquery";
    private static String passwordValueOracle41="Plm#2023Query";
    private static String urlValueOracle41="jdbc:oracle:thin:@bidbxa-dg.longi.com:1521/bigdata";

    private static String userValueDp="BDQUERY";
    private static String passwordValueDp="BigData2099query";
    private static String urlValueDp="jdbc:mysql://10.0.139.37:3306/aps-dp-mg?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8";

    private static String driverClassNameValue = "oracle.jdbc.OracleDriver";
    /*//@Value("${erp.oracle.user}")
    private String userErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_USERNAME");
    //@Value("${erp.oracle.password}")
    private String passwordErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_PASSWORD");
    //@Value("${erp.oracle.url}")
    private String urlErp=LONGiPropertiesUtil.readInterfaceProperties("ERP_DB_URL");

    //@Value("${wms.oracle.user}")
    private String userWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_USERNAME");
    //@Value("${wms.oracle.password}")
    private String passwordWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_PASSWORD");
    //@Value("${wms.oracle.url}")
    private String urlWms=LONGiPropertiesUtil.readInterfaceProperties("WMS_DB_URL");

    //@Value("${oracle14.oracle.user}")
    private String userOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_USERNAME");
    //@Value("${oracle14.oracle.password}")
    private String passwordOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_PASSWORD");
    //@Value("${oracle14.oracle.url}")
    private String urlOracle41=LONGiPropertiesUtil.readInterfaceProperties("BI_DB_URL");

    //@Value("${dp.mysql.user}")
    private String userDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_USERNAME");
    //@Value("${dp.mysql.password}")
    private String passwordDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_PASSWORD");
    //@Value("${dp.mysql.url}")
    private String urlDp=LONGiPropertiesUtil.readInterfaceProperties("DP_DB_URL");*/

    public static Connection getErpConnection() {
        try {
            Class.forName(driverClassNameValue);
            Connection conn =DriverManager.getConnection(urlValueErp, userValueErp, passwordValueErp);
            conn.setSchema("BDQUERY");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
        	e.printStackTrace();
            //log.error("获取数据库连接异常：", e);
        }
        return null;
    }

    public static Connection getWmsConnection() {
        try {
            Class.forName(driverClassNameValue);
            Connection conn = DriverManager.getConnection(urlValueWms, userValueWms, passwordValueWms);
            conn.setSchema("WMS_PROD");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
        	 e.printStackTrace();
            //log.error("获取数据库连接异常：", e);
        }
        return null;
    }


    public static Connection getOracle41Connection() {
        try {
            Class.forName(driverClassNameValue);
            Connection conn = DriverManager.getConnection(urlValueOracle41
                    , userValueOracle41, passwordValueOracle41);
            conn.setSchema("LDW_APP");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
        	 e.printStackTrace();
            //log.error("获取数据库连接异常：", e);
        }
        return null;
    }

    public static Connection getDpConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(urlValueDp, userValueDp, passwordValueDp);
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
        	System.out.println("DP数据库连接异常！！！！！");
        	 e.printStackTrace();
            //log.error("获取数据库连接异常：", e);
        }
        return null;
    }

    /*@PostConstruct
    private void init() {
        userValueErp = userErp;
        passwordValueErp = passwordErp;
        urlValueErp = urlErp;

        userValueWms = userWms;
        passwordValueWms = passwordWms;
        urlValueWms = urlWms;

        userValueOracle41 = userOracle41;
        passwordValueOracle41 = passwordOracle41;
        urlValueOracle41 = urlOracle41;

        userValueDp = userDp;
        passwordValueDp = passwordDp;
        urlValueDp = urlDp;
    }*/

}
