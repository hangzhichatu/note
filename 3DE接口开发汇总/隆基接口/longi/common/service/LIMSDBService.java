package longi.common.service;

import longi.common.bean.bps.CustomAttr;
import longi.common.bean.bps.DpInfo;
import longi.common.bean.bps.LoginUserInfo;
import longi.common.bean.bps.UserInfo;
import longi.common.constants.GlobalConfig;

//import org.apache.poi.ss.formula.functions.T;
import java.sql.*;
import java.util.*;

public class LIMSDBService {

	// 定义数据库的链接
	private static Connection connection;
	// 定义sql语句的执行对象
	private static PreparedStatement pstmt;
	// 定义查询返回的结果集合
	private static ResultSet resultSet;
//    private final static Logger logger = LoggerFactory.getLogger(LIMSDBService.class);

	public LIMSDBService() {
		try {
			Class.forName(GlobalConfig.ORACLE_DB_DRIVER);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 定义获得数据库的链接
	public Connection getConnection() throws SQLException {
		try {
//            logger.info("开始连接LIMS同步数据库：" + GlobalConfig.LIMS_DB_URL);

			connection = DriverManager.getConnection(GlobalConfig.LIMS_DB_URL, GlobalConfig.LIMS_DB_USERNAME,
					GlobalConfig.LIMS_DB_PASSWORD);
//            logger.info("初始化LIMS同步数据库成功");
		} catch (SQLException e) {
//            logger.error("初始化LIMS数据库连接异常：" + e.toString());
			// TODO: handle exception
			e.printStackTrace();
			throw new SQLException(e);
		}
		return connection;
	}

	public Map findSimpleResult(String sql, List params) throws SQLException {
		Map map = new HashMap();
		int index = 1;
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();// 返回查询结果
		// 获取此 ResultSet 对象的列的编号、类型和属性。
		ResultSetMetaData metaData = resultSet.getMetaData();
		int col_len = metaData.getColumnCount();// 获取列的长度
		while (resultSet.next())// 获得列的名称
		{
			for (int i = 0; i < col_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null)// 列的值没有时，设置列值为“”
				{
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
		}
		return map;
	}

	public List findMoreResult(String sql, List params) throws SQLException {
		List list = new ArrayList();
		int index = 1;
		pstmt = connection.prepareStatement(sql);
		if (params != null && !params.isEmpty()) {
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(index++, params.get(i));
			}
		}
		resultSet = pstmt.executeQuery();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int cols_len = metaData.getColumnCount();
		while (resultSet.next()) {
			Map map = new HashMap();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
		return list;
	}

	public static ArrayList<DpInfo> doQueryDept(String sql) throws SQLException {
		try {

			ArrayList<DpInfo> departList = new ArrayList<DpInfo>();
			DpInfo depart = new DpInfo();
//            System.out.println("sql="+sql);
			pstmt = connection.prepareStatement(sql);
			// 为preparedStatement对象的sql中的占位符设置参数
			// pstmt.setString(1, "%张%");
			// 执行sql语句，并返回结果集到resultSet中
			resultSet = pstmt.executeQuery();
			// 遍历结果集
			while (resultSet.next()) {
				String id = resultSet.getString("DEPTID");
				String description = resultSet.getString("DESCR");
				String level = resultSet.getString("LGIDEPTLEVEL");
				String managerName = resultSet.getString("MANAGERNAME");
				String managerId = resultSet.getString("MANAGERID");
//                System.out.println(id + "--" + id + "--" + description);
				depart = new DpInfo();
				depart.setId("Org|" + id);
//                depart.setDpInfodesc(description);
				depart.setText(description);
//                depart.setDepartlevel(level);

//                depart.setType("Dep");
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
//                    logger.error("pstmt关闭异常：" + e.toString());
					throw new SQLException(e);
				}
			}
		}
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: doQueryPerson
	 * @Description: 根据部门编号查询人员
	 * @param: @param searchSql
	 * @param: @return
	 * @param: @throws SQLException
	 * @date: 2022年5月9日 下午4:58:25
	 */
	public static List<UserInfo> doQueryPerson(String searchSql) throws SQLException {
		try {

			List<UserInfo> userInfoList = new ArrayList<UserInfo>();
			UserInfo userInfo = new UserInfo();

//            System.out.println("sql="+sql);

			pstmt = connection.prepareStatement(searchSql);
			// 为preparedStatement对象的sql中的占位符设置参数
			// pstmt.setString(1, "%张%");
			// 执行sql语句，并返回结果集到resultSet中
			resultSet = pstmt.executeQuery();
			// 遍历结果集
			while (resultSet.next()) {
				String id = resultSet.getString("EMPLID");
				String description = resultSet.getString("NAME");
				String poDesc = resultSet.getString("POSITIONNBRDESCR");
				String deptDesc = resultSet.getString("DEPTDESCR");
				String longinId = resultSet.getString("OPRID");
				userInfo = new UserInfo();
				userInfo.setEmplid("User|"+longinId);
				userInfo.setName(description);
				userInfo.setDeptdescr(deptDesc);
				userInfo.setPositionnbrdescr(poDesc);
				userInfo.setNumber(id);
				userInfoList.add(userInfo);
			}
			// System.out.println("单个部门查询结果："+LONGiModuleFastJsonUtil.beanToJson(departList));
			return userInfoList;
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
	 * @Author: Longi.liqitong
	 * @Title: doQueryPerson
	 * @Description: 根据帐号查询人员
	 * @param: @param searchSql
	 * @param: @return
	 * @param: @throws SQLException
	 * @date: 2022年12月26日 下午1:01:33
	 */
	public static List<LoginUserInfo> doQueryUser(String searchSql) throws SQLException {
		try {
			List<LoginUserInfo> userInfoList = new ArrayList<LoginUserInfo>();
			LoginUserInfo userInfo = new LoginUserInfo();
			pstmt = connection.prepareStatement(searchSql);
			resultSet = pstmt.executeQuery();
			// 遍历结果集
			while (resultSet.next()) {
				String name = resultSet.getString("NAME");
				String oprId = resultSet.getString("OPRID");
				String emplId = resultSet.getString("EMPLID");
				String gpsLongDescr = resultSet.getString("GPSLONGDESCR");
				String positionNbrDescr = resultSet.getString("POSITIONNBRDESCR");
				String deptDescr = resultSet.getString("DEPTDESCR");
				userInfo = new LoginUserInfo();
				userInfo.setName(name);
				userInfo.setOprId(oprId);
				userInfo.setEmplId(emplId);
				userInfo.setGpsLongDescr(gpsLongDescr);
				userInfo.setPositionNbrDescr(positionNbrDescr);
				userInfo.setDeptDescr(deptDescr);
				userInfoList.add(userInfo);
			}
			return userInfoList;
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					throw new SQLException(e);
				}
			}
		}
	}

	/**
	 * 获取数据库中所有表名称
	 *
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static List<String> getTables(Connection conn) throws SQLException {
		DatabaseMetaData databaseMetaData = conn.getMetaData();
		ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
		ArrayList<String> tablesList = new ArrayList<String>();
		while (tables.next()) {
			tablesList.add(tables.getString("TABLE_NAME"));
		}
		return tablesList;
	}

	public String getRootBuInfo() throws SQLException {
		String sql = "select erp_eco_increment.nextval from dual";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		return printRs(rs);
	}

	/**
	 * 根据结果集输出
	 *
	 * @param rs
	 */
	public static String printRs(ResultSet rs) throws SQLException {
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
//            logger.error("sql执行结果输出：" + e.toString());
			throw new SQLException(e);
		}
		return seq;
	}

	public static void releaseConn() throws SQLException {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//                logger.error("resultSet关闭异常：" + e.toString());
				throw new SQLException(e);
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//                logger.error("pstmt关闭异常：" + e.toString());
				throw new SQLException(e);
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
//                logger.error("connection关闭异常：" + e.toString());
				throw new SQLException(e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//        System.out.println(getTables(connection));
//        List paramList = new ArrayList();
//        paramList.add("TEST");
//        paramList.add(1);
		System.out.println("main");
		doQueryDept("select * from LIMS.T_LIMS_HR_DEPT where LGIDEPTLEVEL='L0'");

	}

}