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
import longi.dataecharts.entity.SkuData2;
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
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static longi.dataecharts.DateUtil.getMonthBetween;

@Slf4j
public class WmsSqlQuery {

    public static Map<String, List<SkuData>> queryWmsData() {
        List<SkuData> listIn = new ArrayList<>();
        List<SkuData> listOut = new ArrayList<>();
        Map<String, List<SkuData>> resultMap = new HashMap<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseUtil.getWmsConnection();
            ps = connection.prepareStatement(QuerySqlConstant.wmsSql
                    .replace("${Ksrq}", DateUtil.getLastThreeMonthFirstDay())
                    .replace("${Jsrq}", DateUtil.getLastDay()));
            rs = ps.executeQuery();
            while (rs.next()) {
                SkuData wmsData = new SkuData();
                wmsData.setSku(rs.getString(3));
                if (Objects.equals("生产入库", rs.getString(1))) {
                    listIn.add(wmsData);
                } else if (Objects.equals("销售出库", rs.getString(1))) {
                    listOut.add(wmsData);
                }
            }
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询WMS异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        resultMap.put("in", listIn);
        resultMap.put("out", listOut);
        return resultMap;
    }

    public static void writeFile(String fileName) {
        Map<String, List<SkuData>> map = queryWmsData();
        try {
            FileWriterUtil.writeFileWms(fileName, JSON.toJSONString(map),"wms");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Map<String, List<SkuData>> map() {
        Map<String, List<SkuData>> result = new HashMap<>();
        JSONObject jsonObject = FileWriterUtil.parseFile2("wms-3-"
                + FileWriterUtil.getFileNameMonth() + ".txt", "wms");
        if (Objects.nonNull(jsonObject)) {
            JSONArray jsonArray1 = jsonObject.getJSONArray("in");
            JSONArray jsonArray2 = jsonObject.getJSONArray("out");

            List<SkuData> inList = new ArrayList<>();
            jsonArray1.forEach(json -> {
                JSONObject jsonObject1 = (JSONObject) json;
                inList.add(new SkuData() {{
                    setWatt(jsonObject1.getString("watt"));
                    setSku(jsonObject1.getString("sku"));
                }});
            });

            List<SkuData> outList = new ArrayList<>();
            jsonArray2.forEach(json -> {
                JSONObject jsonObject2 = (JSONObject) json;
                outList.add(new SkuData() {{
                    setWatt(jsonObject2.getString("watt"));
                    setSku(jsonObject2.getString("sku"));
                }});
            });
            result.put("in", inList);
            result.put("out", outList);
        }
        return result;
    }


    public static void writeFile(String fileName,String startTime,String endTime) {
    	
    	System.out.println("fileName========="+fileName+"startTime========"+startTime+"endTime========="+endTime);
        Map<String, List<SkuData>> map = queryWmsData(startTime,endTime);
        System.out.println("map>>>>>>>>>>>>>>>>>"+map.size());
        try {
        	System.out.println("========="+longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        
        	System.out.println("连接成功=="+jedis.ping());
        	System.out.println("LLLLLL====="+ JSON.toJSONString(map).length());
        	jedis.set(fileName, JSON.toJSONString(map));
            //FileWriterUtil.writeFileWms(fileName, JSON.toJSONString(map),"wms_1");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Map<String, List<SkuData>> queryWmsData(String startTime, String endTime) {
        List<SkuData> listIn = new ArrayList<>();
        List<SkuData> listOut = new ArrayList<>();
        Map<String, List<SkuData>> resultMap = new HashMap<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	System.out.println("SSSSSS");
            connection = DatabaseUtil.getWmsConnection();
            System.out.println("connection");
            ps = connection.prepareStatement(QuerySqlConstant.wmsSql
                    .replace("${Ksrq}", startTime)
                    .replace("${Jsrq}", endTime));
            rs = ps.executeQuery();
            System.out.println("whilesTTTTTTT");
            while (rs.next()) {
                SkuData wmsData = new SkuData();
                wmsData.setSku(rs.getString(3));
                if (Objects.equals("生产入库", rs.getString(1))) {
                    listIn.add(wmsData);
                } else if (Objects.equals("销售出库", rs.getString(1))) {
                    listOut.add(wmsData);
                }
            }
            System.out.println("ENDDDDD");
            DatabaseUtil.close(ps, connection, rs);
        } catch (SQLException e) {
        	e.printStackTrace();
            //log.error("一码拉通编码统计报表查询WMS异常：", e);
            DatabaseUtil.close(ps, connection, rs);
            throw new RuntimeException(e);
        }
        resultMap.put("in", listIn);
        resultMap.put("out", listOut);
        return resultMap;
    }


    public static Map<String, List<SkuData>> map2() {
        Map<String, List<SkuData>> result = new HashMap<>();
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
        List<SkuData> inList = new ArrayList<>();
        List<SkuData> outList = new ArrayList<>();
        allMonth.forEach(month->{
            JSONObject jsonObject = FileWriterUtil.parseFile2("wms-1-"
                    + month, "wms_1");
            if (Objects.nonNull(jsonObject)) {
                JSONArray jsonArray1 = jsonObject.getJSONArray("in");
                JSONArray jsonArray2 = jsonObject.getJSONArray("out");
                jsonArray1.forEach(json -> {
                    JSONObject jsonObject1 = (JSONObject) json;
                    inList.add(new SkuData() {{
                        setWatt(jsonObject1.getString("watt"));
                        setSku(jsonObject1.getString("sku"));
                    }});
                });

                jsonArray2.forEach(json -> {
                    JSONObject jsonObject2 = (JSONObject) json;
                    outList.add(new SkuData() {{
                        setWatt(jsonObject2.getString("watt"));
                        setSku(jsonObject2.getString("sku"));
                    }});
                });

            }
        });

        List<SkuData> inResult = inList.stream()
                .collect(Collectors.toMap(SkuData::getSku, a -> a, (o1, o2)-> {
                    o1.setWatt(String.valueOf(getValue(o1.getWatt(),o2.getWatt())));
                    return o1;
                })).values().stream().collect(Collectors.toList());
        List<SkuData> outResult = outList.stream()
                .collect(Collectors.toMap(SkuData::getSku, a -> a, (o1, o2)-> {
                    o1.setWatt(String.valueOf(getValue(o1.getWatt(),o2.getWatt())));
                    return o1;
                })).values().stream().collect(Collectors.toList());

        result.put("in", inResult);
        result.put("out", outResult);
        return result;
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
