package longi.common.interceptors;

import com.matrixone.apps.domain.util.MapList;
import longi.common.bean.bps.LoginUserInfo;
import longi.common.constants.GlobalConfig;
import longi.common.pojo.CodeSystemLoginInfo;
import longi.common.util.LONGiPropertiesUtil;
import longi.module.pdm.constants.LONGiModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @ClassName: LoginInfoManager
 * @Descriptio: 编码系统登陆信息数据库管理类
 * @author: longi.suwei3
 * @date: 2022年5月25日 下午1:21:53
 */
public class LoginInfoManager {
    private final static Logger logger = LoggerFactory.getLogger(LoginInfoManager.class);
    // 定义数据库的链接
    private Connection connection;
    // 定义sql语句的执行对象
    private PreparedStatement pstmt;
    // 定义查询返回的结果集合
    private ResultSet resultSet;

    public LoginInfoManager() {
        try {
            Class.forName(LONGiModuleConfig.BOMDB_DRIVER);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    // 定义获得数据库的链接

    /**
     * @author: longi.suwei3
     * @Title: getConnection
     * @email: suwei3@longi.com
     * @description: 获取数据库连接的方法
     * @date: 2022年5月25日 下午1:21:53
     * @return: {@link Connection}
     */
    public Connection getConnection() throws SQLException {

        try {
//			logger.info("开始连接BOM专用同步数据库："+LONGiModuleConfig.BOMDB_URL);
//			connection = DriverManager.getConnection(LONGiModuleConfig.BOMDB_URL, LONGiModuleConfig.BOMDB_USERNAME,
//					LONGiModuleConfig.BOMDB_PASSWORD);
//			connection = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:PDM", "bom_user", "pdm123");
            connection = DriverManager.getConnection(LONGiPropertiesUtil.readInterfaceProperties("BOMDB_URL"), LONGiPropertiesUtil.readInterfaceProperties("BOMDB_USERNAME"), LONGiPropertiesUtil.readInterfaceProperties("BOMDB_PASSWORD"));
            logger.info("初始化编码系统同步数据库成功");
        } catch (SQLException e) {
            logger.error("初始化编码系统同步数据库连接异常：" + e.toString());
            // TODO: handle exception
            throw new SQLException(e);
        }
        return connection;
    }

    /**
     * @Author: Longi.suwei
     * @Title: updateLoginInfo
     * @Description: 更新登陆信息表，表code_login_info
     * @param: @param  userName
     * @param: @param  ip
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:21:53
     */
    public void updateLoginInfo(String userName, String ip) throws SQLException {
        try {
            String sql = "update code_login_info set user_name = ? where ip = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setString(2, ip);
            int result = pstmt.executeUpdate();// 返回值代表收到影响的行数
            if (result > 0) {
                logger.info("更新编码系统同步数据库 登陆信息成功：" + userName + ":" + ip);
            } else {
                logger.info("初始化编码系统同步数据库失败sql：" + sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Author: Longi.suwei
     * @Title: updateByPreparedStatement
     * @Description: 确定有无已有数据
     * @param: @param  sql
     * @param: @param  params
     * @param: @return
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:22:37
     */
    public boolean updateByPreparedStatement(String sql, List params) throws SQLException {
        boolean flag = false;
        int result = -1;// 表示当用户执行添加删除和修改的时候所影响数据库的行数
        pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int index = 1;
        // 填充sql语句中的占位符
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        result = pstmt.executeUpdate();
// The generated order id
        flag = result > 0 ? true : false;
        return flag;
    }

    /**
     * @param userName
     * @param ip
     * @author: longi.suwei3
     * @Title: markLoginInfo
     * @email: suwei3@longi.com
     * @description: 更新登陆情况，IP数据时更新用户名，IP无数据时插入数据
     * @date: 2021/9/6 15:23
     * @return: {@link String}
     */
    public void markLoginInfo(String userName, String ip) throws SQLException {

        String sql = "MERGE INTO CODE_LOGIN_INFO T1" + " USING (SELECT '" + ip + "' AS ip,'" + userName
                + "' AS user_name FROM dual) T2" + " ON ( T1.ip=T2.ip)" + " WHEN MATCHED THEN"
                + " UPDATE SET T1.user_name = T2.user_name" + " WHEN NOT MATCHED THEN"
                + " INSERT (ip,user_name) VALUES(T2.ip,T2.user_name)";

        logger.info("更新internal登录人信息,插入CODE_LOGIN_INFO sql:" + sql);
        try {
            System.out.println("sql=" + sql);
            Statement st = connection.createStatement();
            int i = st.executeUpdate(sql);
            insertLoginHistoryInfo(userName, ip);
            System.out.println("i=" + i);
        } catch (SQLException throwables) {
            logger.error("更新internal登录人信息失败,插入CODE_LOGIN_INFO表失败,sql:" + sql + "\n失败信息:" + throwables.getMessage());
            throw new SQLException(throwables);
        }
    }

    /**
     * @Author: Longi.suwei
     * @Title: insertLoginHistoryInfo
     * @Description: 插入登陆历史数据
     * @param: @param  userName
     * @param: @param  ip
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:23:20
     */
    public void insertLoginHistoryInfo(String userName, String ip) throws SQLException {
        String sql = "insert into CODE_LOGIN_HISTORY_INFO(USER_NAME,IP) values(?,?)";
        List paramList = new ArrayList();
        paramList.add(userName);
        paramList.add(ip);
        try {
            updateByPreparedStatement(sql, paramList);
        } catch (SQLException throwables) {
            logger.error("更新internal登录人历史信息失败,插入CODE_LOGIN_HISTORY_INFO表失败:" + paramList.toString() + "\n失败信息:"
                    + throwables.getMessage());
            throw new SQLException(throwables);
        }

    }

    /**
     * @Author: Longi.suwei
     * @Title: doQueryPersonWithIp
     * @Description: 根据登录IP查询人员信息
     * @param: @param  loginIp
     * @param: @return
     * @param: @throws SQLException
     * @date: 2022年5月30日 下午1:29:20
     */
    public CodeSystemLoginInfo doQueryPersonWithIp(String loginIp) throws SQLException {
        try {
            String sql = "select T1.USER_NAME,T2.NAME from CODE_LOGIN_INFO T1 LEFT JOIN PLM_SHARING_USER T2 ON T1.USER_NAME = T2.OPR_ID where T1.IP = '" + loginIp + "'";
//			String sql = "select USER_NAME,FULL_NAME from CODE_LOGIN_INFO where IP = '" + loginIp + "'";
            System.out.println(sql);
            pstmt = connection.prepareStatement(sql);
            // 为preparedStatement对象的sql中的占位符设置参数
            // pstmt.setString(1, "%张%");
            // 执行sql语句，并返回结果集到resultSet中
            resultSet = pstmt.executeQuery();
            CodeSystemLoginInfo userInfo = new CodeSystemLoginInfo();
            // 遍历结果集
            while (resultSet.next()) {
                String name = resultSet.getString("USER_NAME");
                String fullName = resultSet.getString("NAME");
                userInfo.setName(name);
                userInfo.setFullName(fullName);
            }
            // System.out.println("单个部门查询结果："+LONGiModuleFastJsonUtil.beanToJson(departList));
            return userInfo;
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
//                    logger.error("pstmt关闭异常：" + e.toString());
                    throw new SQLException(e);
                }
            }
        }
    }

    /**
     * @Author: Longi.suwei
     * @Title: printRs
     * @Description: 打印RS
     * @param: @param  rs
     * @param: @return
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:23:49
     */
    public String printRs(ResultSet rs) throws SQLException {
        int columnsCount = 0;
        boolean f = false;
        String seq = "";
        try {
            if (!rs.next()) {
                return "";
            } else {

                ResultSetMetaData rsmd = rs.getMetaData();
                columnsCount = rsmd.getColumnCount();// 数据集的列数

                seq = rs.getString(1);
                rs.close();
            }
        } catch (SQLException e) {
            logger.error("sql执行结果输出：" + e.toString());
            throw new SQLException(e);
        }
        return seq;
    }

    /**
     * @Author: Longi.suwei
     * @Title: releaseConn
     * @Description: 释放连接
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:24:00
     */
    public void releaseConn() throws SQLException {
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

    /**
     * @Author: Longi.suwei
     * @Title: close
     * @Description: 关闭数据库的连接
     * @param: @throws SQLException
     * @date: 2022年5月25日 下午1:24:39
     */
    public void close() throws SQLException {
        if (resultSet != null)
            resultSet.close();
        if (pstmt != null)
            pstmt.close();
        if (connection != null)
            connection.close();
    }

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        LoginInfoManager jdbcUtils = new LoginInfoManager();
        jdbcUtils.getConnection();

//		jdbcUtils.markLoginInfo("lihao30", "192.168.2.13");
        boolean isExist = jdbcUtils.checkLoginUserExist("chengyao");
        System.out.println(isExist);
        jdbcUtils.close();

    }

    /**
     * @Author: Longi.suwei
     * @Title: checkLoginUserExist
     * @Description: 检查登陆人账号是否存在
     * @param: @param  user
     * @param: @return
     * @date: 2022年5月30日 下午3:47:50
     */
    public boolean checkLoginUserExist(String user) {
        // TODO Auto-generated method stub
        try {
            String sql = "select NAME from PLM_SHARING_USER where OPR_ID = '" + user + "'";
            pstmt = connection.prepareStatement(sql);
            // 为preparedStatement对象的sql中的占位符设置参数
            // pstmt.setString(1, "%张%");
            // 执行sql语句，并返回结果集到resultSet中
            resultSet = pstmt.executeQuery();
            CodeSystemLoginInfo userInfo = new CodeSystemLoginInfo();
            // 遍历结果集

            if (resultSet.next()) {
                do {
                    return true;
                } while (resultSet.next());
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @throws SQLException
     * @Author: Longi.suwei
     * @Title: insertUserBasicInfo
     * @Description: 插入人员基本信息
     * @param: @param user
     * @param: @param fullName
     * @date: 2022年5月30日 下午4:09:53
     */
    public void insertUserBasicInfo(String user, String fullName) throws SQLException {
        // TODO Auto-generated method stub
        String sql = "insert into PLM_SHARING_USER(OPR_ID,NAME) values(?,?)";
        List paramList = new ArrayList();
        paramList.add(user);
        paramList.add(fullName);
        try {
            updateByPreparedStatement(sql, paramList);
        } catch (SQLException throwables) {
            logger.error("更新internal登录人基础信息失败,插入PLM_SHARING_USER表失败:" + paramList.toString() + "\n失败信息:"
                    + throwables.getMessage());
            throw new SQLException(throwables);
        }
    }

    public List getLoginUserRoles(String user) {
        List rolesList = new ArrayList();
        try {
            String sql = "SELECT T1.ROLE_CODE,T1.ROLE_NAME FROM PLM_SHARING_ROLE T1 LEFT JOIN PLM_SHARING_ROLE_USER T2 ON T2.ROLE_CODE = T1.ROLE_CODE WHERE T2.OPR_ID = '" + user + "'";
            pstmt = connection.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            // 遍历结果集
            while (resultSet.next()) {
                String role_code = resultSet.getString("ROLE_CODE");
                rolesList.add(role_code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rolesList;
    }

    public boolean hasRole(String user, String role_code) {
        System.out.println("hasRole..");
        boolean hasRole = false;
        try {
            List rolesList = getLoginUserRoles(user);
            hasRole = rolesList.contains(role_code);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return hasRole;
    }

    public List getLoginUserActions(String user) {
        List actionsList = new ArrayList();
        try {
            String sql = "SELECT T1.ACTION_ID,T1.ACTION_NAME FROM PLM_SHARING_ACTION T1 LEFT JOIN PLM_SHARING_USER_ACTION T2 ON T2.ACTION_ID = T1.ACTION_ID WHERE T2.OPR_ID = '" + user + "'";
            pstmt = connection.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            // 遍历结果集
            while (resultSet.next()) {
                String action_id = resultSet.getString("ACTION_ID");
                actionsList.add(action_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actionsList;
    }

    public List getRoleUsers(String role_code) {
        List<LoginUserInfo> usersList = new ArrayList();
        try {
            String sql = "SELECT T1.OPR_ID,T1.NAME FROM PLM_SHARING_USER T1 LEFT JOIN PLM_SHARING_ROLE_USER T2 ON T2.OPR_ID = T1.OPR_ID WHERE T2.ROLE_CODE = '" + role_code + "'";
            pstmt = connection.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            // 遍历结果集
            LoginUserInfo userInfo = null;
            while (resultSet.next()) {
                userInfo = new LoginUserInfo();
                userInfo.setOprId(resultSet.getString("OPR_ID"));
                userInfo.setName(resultSet.getString("NAME"));
                usersList.add(userInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usersList;
    }

    public void insertUserVisitorLogs(String oprId, String actionId) throws SQLException {
        String sql = "INSERT INTO PLM_SHARING_USER_VISITOR_LOGS(OPR_ID,ACTION_ID,ACCESS_DATE) VALUES(?,?,?)";
        List paramList = new ArrayList();
        paramList.add(oprId);
        paramList.add(actionId);
        Date date = new Date(new java.util.Date().getTime());
        paramList.add(date);
        try {
            updateByPreparedStatement(sql, paramList);
        } catch (SQLException throwables) {
            logger.error("更新internal人员访问记录失败,插入PLM_SHARING_USER_VISITOR_LOGS表失败:" + paramList.toString() + "\n失败信息:"
                    + throwables.getMessage());
            throw new SQLException(throwables);
        }
    }

}