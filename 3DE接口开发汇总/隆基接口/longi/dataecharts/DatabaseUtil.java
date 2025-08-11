package longi.dataecharts;

import lombok.extern.slf4j.Slf4j;
import longi.dataecharts.DataBaseOracleConfiguration;

import java.sql.*;

@Slf4j
public class DatabaseUtil {

    public static Connection getErpConnection() {
        return DataBaseOracleConfiguration.getErpConnection();
    }

    public static Connection getWmsConnection() {
        return DataBaseOracleConfiguration.getWmsConnection();
    }

    public static Connection getOracle41Connection() {
        return DataBaseOracleConfiguration.getOracle41Connection();
    }

    public static Connection getDpConnection() {
        return DataBaseOracleConfiguration.getDpConnection();
    }

    public static void close(Statement st, Connection conn, ResultSet rs){
        if (st!=null){
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
