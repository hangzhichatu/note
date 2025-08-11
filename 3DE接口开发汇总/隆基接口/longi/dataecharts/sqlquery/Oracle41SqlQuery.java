package longi.dataecharts.sqlquery;

import lombok.extern.slf4j.Slf4j;
import longi.dataecharts.DatabaseUtil;
import longi.dataecharts.DateUtil;
import longi.dataecharts.entity.SkuData;
import longi.dataecharts.entity.SkuData2;
import longi.dataecharts.sql.QuerySqlConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Oracle41SqlQuery {

    public static List<SkuData> queryCmsData(int num) {
        List<SkuData> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	//System.out.println("num=========="+num);
            connection = DatabaseUtil.getOracle41Connection();
            String sql = "";
            if (1 == num) {
                String[] months = DateUtil.getMonths2();
                
                System.out.println("months[]====="+months[0]+"========"+months[1]);
                sql = QuerySqlConstant.cmsSql
                        .replace("${DataMonth1}", months[0])
                        .replace("${DataMonth2}", months[1]);
            } else {
            	 System.out.println("months[]====="+DateUtil.getLastThreeMonthFirstDay2()+"========"+DateUtil.getLastOneMonthLastDay2());
                sql = QuerySqlConstant.cmsSql
                        .replace("${DataMonth1}", DateUtil.getLastThreeMonthFirstDay2())
                        .replace("${DataMonth2}", DateUtil.getLastOneMonthLastDay2());
            }

            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData skuData = new SkuData();
                //System.out.println("queryCmsData(1)====="+rs.getString(1));
                //System.out.println("queryCmsData(2)====="+rs.getString(2));
                skuData.setSku(rs.getString(1));
                skuData.setWatt(rs.getString(2));
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	System.out.println("一码拉通编码统计报表查询CMS异常===queryCmsData====");
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询CMS异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        System.out.println("queryCmsData==list===-----"+list.size());
        return list;
    }

    public static List<SkuData2> queryCmsData2() {
        List<SkuData2> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String[] months = DateUtil.getMonths2();
        try {
            connection = DatabaseUtil.getOracle41Connection();
            String sql = QuerySqlConstant.cmsSql3
                    .replace("${DataMonth1}", months[0])
                    .replace("${DataMonth2}", months[1]);
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData2 skuData = new SkuData2();
                skuData.setSku(rs.getString(1));
                skuData.setWatt(rs.getString(2));
                skuData.setNum(rs.getString(3));
                skuData.setShortTypeCode(Objects.equals(rs.getString(4), "1022")
                        || Objects.equals(rs.getString(4), "1024 ") ? "分布式" : "集中式");
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询CMS 3个月数据异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        return list;
    }

    public static List<SkuData2> queryYTData2() {
        List<SkuData2> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String[] months = DateUtil.getMonths2();
        try {
            connection = DatabaseUtil.getOracle41Connection();
            String sql = QuerySqlConstant.ytSql3
                    .replace("${DataMonth1}", months[0])
                    .replace("${DataMonth2}", months[1]);
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData2 skuData = new SkuData2();
                skuData.setSku(rs.getString(1));
                skuData.setWatt(rs.getString(2));
                skuData.setNum(rs.getString(3));
                skuData.setShortTypeCode(rs.getString(4));
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
        	//log.error("一码拉通编码统计报表查询【预投】异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        return list;
    }

    public static List<SkuData> queryYTData(int num) {
        List<SkuData> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseUtil.getOracle41Connection();
            String sql;
            if (1 == num) {
                String[] months = DateUtil.getMonths2();
                sql = QuerySqlConstant.ytSql
                        .replace("${DataMonth1}", months[0])
                        .replace("${DataMonth2}", months[1]);
            } else {
                sql = QuerySqlConstant.ytSql
                        .replace("${DataMonth1}", DateUtil.getLastThreeMonthFirstDay2())
                        .replace("${DataMonth2}", DateUtil.getLastOneMonthLastDay2());
            }
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData skuData = new SkuData();
                //System.out.println("queryYTData(1)====="+rs.getString(1));
                //System.out.println("queryYTData(2)====="+rs.getString(2));
                skuData.setSku(rs.getString(1));
                skuData.setWatt(rs.getString(2));
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	System.out.println("一码拉通编码统计报表查询【预投】异常===queryYTData====");

        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询【预投】异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        System.out.println("queryYTData==list===-----"+list.size());
        return list;
    }


}
