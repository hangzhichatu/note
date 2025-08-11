package longi.module.pdm.util;

import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.impl.LONGiModuleCodeSystemPurchasingDoneAction;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LONGiModuleDBUitl {

	// 定义数据库的链接
	private Connection connection;
	// 定义sql语句的执行对象
	private PreparedStatement pstmt;
	// 定义查询返回的结果集合
	private ResultSet resultSet;
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleDBUitl.class);
	public LONGiModuleDBUitl() {
		try {
			Class.forName(LONGiModuleConfig.BOMDB_DRIVER);
			System.out.println("注册驱动成功!!");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 定义获得数据库的链接
	public Connection getConnection() throws SQLException {
		try {
			logger.info("开始连接BOM专用同步数据库："+LONGiModuleConfig.BOMDB_URL);
			connection = DriverManager.getConnection(LONGiModuleConfig.BOMDB_URL, LONGiModuleConfig.BOMDB_USERNAME,
					LONGiModuleConfig.BOMDB_PASSWORD);
			logger.info("初始化BOM专用同步数据库成功");
		} catch (SQLException e) {
			logger.error("初始化数据库连接异常："+e.toString());
			// TODO: handle exception
			throw new SQLException(e);
		}
		return connection;
	}

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

	public T findSimpleRefResult(String sql, List params, Class cls) throws Exception {
		T resultObject = null;
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
			// 通过反射机制创建实例
			resultObject = (T) cls.newInstance();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				// 返回一个 Field 对象，该对象反映此 Class 对象所表示的类或接口的指定已声明字段。
				Field field = cls.getDeclaredField(cols_name);
				field.setAccessible(true);// 打开javabean的访问private权限
				field.set(resultObject, cols_value);// 为resultObject对象的field的属性赋值
			} // 上面的两行红题字就是要求实体类中的属性名一定要和数据库中的字段名一定要严//格相同（包括大小写）,而oracle数据库中一般都是大写的，如何让oracle区分大小写，请看博///文：http://blog.sina.com.cn/s/blog_7ffb8dd501013xkq.html
		}
		return resultObject;
	}

	public List findMoreRefResult(String sql, List params, Class cls) throws Exception {
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
			T resultObject = (T) cls.newInstance();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = metaData.getColumnName(i + 1);
				Object cols_value = resultSet.getObject(cols_name);
				if (cols_value == null) {
					cols_value = "";
				}
				Field field = cls.getDeclaredField(cols_name);
				field.setAccessible(true);
				field.set(resultObject, cols_value);
			}
			list.add(resultObject);
		}
		return list;
	}

	public String getBOMSyncCount(String pdm_id) throws SQLException {
		String sql = "select count(*) from erp_sync_info where pdm_id='" + pdm_id + "'";
		System.out.println("sql=" + sql);
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		return printRs(rs);
	}
	
	public String getPartSyncCount(String pdm_id) throws SQLException {
		String sql = "select count(*) from erp_sync_part_info where pdm_id='" + pdm_id + "'";
		System.out.println("sql=" + sql);
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		return printRs(rs);
	}

	public String getBOMSupSyncCount(String pdm_id) throws SQLException {
		String sql = "select count(*) from erp_sup_sync_info where pdm_id='" + pdm_id + "'";
		System.out.println("sql=" + sql);
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		return printRs(rs);
	}

	public String getECOSeq() throws SQLException {
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
			logger.error("sql执行结果输出："+e.toString());
			throw new SQLException(e);
		}
		return seq;
	}

	public void releaseConn() throws SQLException {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("resultSet关闭异常："+e.toString());
				throw new SQLException(e);
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("pstmt关闭异常："+e.toString());
				throw new SQLException(e);
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("connection关闭异常："+e.toString());
				throw new SQLException(e);
			}
		}
	}

	public static void writeProperties(String url, String user, String password) {
		Properties pro = new Properties();
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream("Config.ini");
			pro.put("url", url);
			pro.put("user", user);
			pro.put("password", password);
			pro.store(fileOut, "My Config");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (fileOut != null)
					fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static List readProperties() {
		List list = new ArrayList();
		Properties pro = new Properties();
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream("Config.ini");
			pro.load(fileIn);
			list.add(pro.getProperty("url"));
			list.add(pro.getProperty("user"));
			list.add(pro.getProperty("password"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (fileIn != null)
					fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		LONGiModuleDBUitl jdbcUtils = new LONGiModuleDBUitl();
		jdbcUtils.getConnection();
		// String sql = "insert into userinfo(username,pswd) values(?,?)";
		// Listparams = new ArrayList();
		// params.add("rose");
		// params.add("123");
		// try {
		// boolean flag = jdbcUtils.updateByPreparedStatement(sql, params);
		// System.out.println(flag);
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		List paramList = new ArrayList();
		paramList.add("TEST");
		paramList.add(1);

//		String sql = "insert into erp_sync_info(pdm_id,status) values(?,?)";
//        jdbcUtils.updateByPreparedStatement(sql,paramList);
		jdbcUtils.getBOMSyncCount("30103.58115.43200.27173");
//        String seqString = jdbcUtils.getBOMSyncCount("11233.123.123115533333");
//        jdbcUtils.releaseConn();
//        System.out.println("seqString="+seqString);
//        String sql2 = "select * from erp_sync_info where pdm_id=?";

//        List map = jdbcUtils.findMoreResult(sql2,paramList);
////        jdbcUtils.
//        System.out.println(Objects.isNull(map)||map.isEmpty());
//        jdbcUtils.releaseConn();
		// Listparams = new ArrayList();
		// params.add(1);
	}

}