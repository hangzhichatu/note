package longi.dataecharts.sqlquery;

import lombok.extern.slf4j.Slf4j;
import longi.dataecharts.DatabaseUtil;
import longi.dataecharts.DateUtil;
import longi.dataecharts.entity.SkuData;
import longi.dataecharts.sql.QuerySqlConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DpSqlQuery {

    public static List<SkuData> queryDpData() {
        List<SkuData> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseUtil.getDpConnection();
            ps = connection.prepareStatement(QuerySqlConstant.dpSql
                    .replace("${StartMonth}", DateUtil.getLastThreeMonthFirstDay2())
                    .replace("${EndMonth}", DateUtil.getLastOneMonthLastDay2()));
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData skuData = new SkuData();
                skuData.setSku(rs.getString(11));
                skuData.setWatt(rs.getString(13));
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询Dp异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        return list;
    }

}
