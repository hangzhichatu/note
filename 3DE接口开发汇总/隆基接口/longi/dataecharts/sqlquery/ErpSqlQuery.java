package longi.dataecharts.sqlquery;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import longi.dataecharts.DatabaseUtil;
import longi.dataecharts.DateUtil;
import longi.dataecharts.FileWriterUtil;
import longi.dataecharts.entity.SkuData;
import longi.dataecharts.sql.QuerySqlConstant;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static longi.dataecharts.DateUtil.getMonthBetween;

@Slf4j
public class ErpSqlQuery {

    public static List<SkuData> queryErpData(String flag) {
        List<SkuData> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseUtil.getErpConnection();
            String erpSql = QuerySqlConstant.erpSql;
            if (Objects.equals(flag, "3")) {
                erpSql = erpSql.replace("${Ksrq}", DateUtil.getLastThreeMonthFirstDay())
                        .replace("${Jsrq}", DateUtil.getLastDay());
            } else {
                erpSql = erpSql.replace("${Ksrq}", DateUtil.getFirstDay())
                        .replace("${Jsrq}", DateUtil.getLastDay());
            }
            ps = connection.prepareStatement(erpSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData skuData = new SkuData();
                skuData.setSku(rs.getString(11));
                skuData.setWatt(rs.getString(15));
                list.add(skuData);
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询Erp异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        return list;
    }

    public static void writeFile(String fileName, String flag) {
        List<SkuData> list = queryErpData(flag);
        try {
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        	jedis.set(fileName, JSON.toJSONString(list));
            //FileWriterUtil.writeFileErp(fileName, JSON.toJSONString(list),"erp");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<SkuData> list(String flag) {
        List<SkuData> result = new ArrayList<>();
        String fileFlag = "erp-";
        if (Objects.equals(flag, "3")) {
            fileFlag = "erp-3-";
        }
        JSONArray jsonArray = FileWriterUtil.parseFile(
                fileFlag + FileWriterUtil.getFileNameMonth(), "erp");
        if (Objects.nonNull(jsonArray) && jsonArray.size() > 0) {
            jsonArray.forEach(json -> {
                JSONObject jsonObject = (JSONObject) json;
                result.add(new SkuData() {{
                    setWatt(jsonObject.getString("watt"));
                    setSku(jsonObject.getString("sku"));
                }});
            });
        }
        return result;
    }


    public static void writeFile(String fileName,String startTime,String endTime) {
        List<SkuData> list = queryErpData(startTime,endTime);
        System.out.println("list>>>>>>=writeFile==ERP===="+list.size());
        try {
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        	jedis.set(fileName, JSON.toJSONString(list));
            //FileWriterUtil.writeFileErp(fileName, JSON.toJSONString(list),"erp_1");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static List<SkuData> queryErpData(String startTime, String endTime) {
        List<SkuData> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	System.out.println("queryErpDataSSSS=");
            connection = DatabaseUtil.getErpConnection();
            System.out.println("connection");
            String erpSql = QuerySqlConstant.erpSql
                    .replace("${Ksrq}", startTime)
                        .replace("${Jsrq}", endTime);
            ps = connection.prepareStatement(erpSql);
            rs = ps.executeQuery();
            System.out.println("while sss");
            while (rs.next()) {
                SkuData skuData = new SkuData();
                skuData.setSku(rs.getString(11));
                skuData.setWatt(rs.getString(15));
                list.add(skuData);
            }
            System.out.println("while end");
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询Erp异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        return list;
    }


    public static List<SkuData> list2() {
        List<SkuData> result = new ArrayList<>();
        String end;
        String start;
        if(StringUtils.isBlank(DateUtil.queryRangeMonths2)) {
            end = DateTimeFormatter.ofPattern("yyyy-MM").format(
                    LocalDate.now().minusMonths(1));
            start = DateTimeFormatter.ofPattern("yyyy-MM").format(
                    LocalDate.now().minusMonths(3));

        }else{
            String[] months = DateUtil.queryRangeMonths2.split(" - ");
            end = months[1];
            start = months[0];
        }
        List<String> allMonth = getMonthBetween(start, end);

        allMonth.forEach(month->{
            JSONArray jsonArray = FileWriterUtil.parseFile(
                    "erp-1-" + month, "erp_1");
            if (Objects.nonNull(jsonArray) && jsonArray.size() > 0) {
                jsonArray.forEach(json -> {
                    JSONObject jsonObject = (JSONObject) json;
                    result.add(new SkuData() {{
                        setWatt(jsonObject.getString("watt"));
                        setSku(jsonObject.getString("sku"));
                    }});
                });
            }
        });

        List<SkuData> erpResult = result.stream()
                .collect(Collectors.toMap(SkuData::getSku, a -> a, (o1, o2)-> {
                    o1.setWatt(String.valueOf(getValue(o1.getWatt(),o2.getWatt())));
                    return o1;
                })).values().stream().collect(Collectors.toList());


        return erpResult;
    }

    private static String getValue(String watt1, String watt2) {
        if(StringUtils.isNotBlank(watt1) || StringUtils.isNotBlank(watt2)){
            if(StringUtils.isNotBlank(watt1)){
                if(StringUtils.isNotBlank(watt2)){
                    return String.valueOf(new BigDecimal(watt1)
                            .add(new BigDecimal(watt2)));
                }else{
                    return watt1;
                }
            }else {
                return watt2;
            }
        }
        return "";
    }

}
