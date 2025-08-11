package longi.common.service;

import longi.common.bean.bps.CustomAttr;
import longi.common.bean.bps.DpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

/**
 * @Created by lg-xiehao3
 * @Date 2024/2/4 11:19
 */
public class PISDBService {
    // 定义数据库的链接
    private static Connection connection;
    // 定义sql语句的执行对象
    private static PreparedStatement pstmt;
    // 定义查询返回的结果集合
    private static ResultSet resultSet;

    private final static Logger logger = LoggerFactory.getLogger(PISDBService.class);

    public PISDBService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获得数据库的链接
    public Connection getConnection() throws Exception {
        try {
            String url = "jdbc:mysql://10.201.5.226:3306/pf_platform?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            String user = "pfuser";
            String password = "Longi@pfuser#mysql2022";

            // 连接PIS正式环境
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("======初始化PIS同步数据库成功=====");
        } catch (SQLException e) {
            logger.error("初始化PIS数据库连接异常：" + e.toString());
            e.printStackTrace();
            throw new SQLException(e);
        }
        return connection;
    }

    public static void releaseConn() throws SQLException {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("resultSet关闭异常：" + e.toString());
                throw new SQLException(e);
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("pstmt关闭异常：" + e.toString());
                throw new SQLException(e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("connection关闭异常：" + e.toString());
                throw new SQLException(e);
            }
        }
    }


    public static ArrayList<DpInfo> doQueryDept(String sql) throws SQLException {
        try {

            ArrayList<DpInfo> departList = new ArrayList<DpInfo>();
            DpInfo depart = new DpInfo();
            pstmt = connection.prepareStatement(sql);
            // 为preparedStatement对象的sql中的占位符设置参数
            // pstmt.setString(1, "%张%");
            // 执行sql语句，并返回结果集到resultSet中
            resultSet = pstmt.executeQuery();
            // 遍历结果集
            while (resultSet.next()) {
                String id = resultSet.getString("org_code");
                String description = resultSet.getString("org_descr");
                String level = resultSet.getString("org_level");
                String managerName = resultSet.getString("real_name");
                String managerId = resultSet.getString("manager_id");

                depart = new DpInfo();
                depart.setId("Org|" + id);
                depart.setText(description);

                CustomAttr attr = new CustomAttr();
                attr.setMANAGERID(managerId);
                attr.setMANAGERNAME(managerName);
                attr.setLGIDEPTLEVEL(level);

                depart.setAttributes(attr);

                departList.add(depart);
            }
            return departList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    logger.error("pstmt关闭异常：" + e.toString());
                    throw new SQLException(e);
                }
            }
        }
    }
}
