/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 ‭‭‭‭‭‭‭‭‭‭‭‭[smallbun] www.smallbun.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package longi.dataecharts.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.list.TreeList;
//org.apache.commons.collections.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import longi.dataecharts.DateUtil;
import longi.dataecharts.entity.*;
import longi.dataecharts.handle.CodeInfoHandle;
import longi.dataecharts.sqlquery.Oracle41SqlQuery;
import longi.dataecharts.PageableResult;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



@Slf4j
public class OrderTableQueryController {

    /**
     * 模块名称
     */
    private static final String MODEL = "订单表格查询";

    /**
     * HTML页面路径前缀
     */
    private static final String HTML_PREFIX = "modules/manage/dataecharts/";




    //@LogAnnotation(model = MODEL, action = OperateLogConstant.SELECT_LIST)
    public PageableResult queryData(Page<SkuData3> page, OrderTableQuery query) {
        if (StringUtils.isNotBlank(query.getQueryRangeMonths1())) {
            DateUtil.queryRangeMonths1 = query.getQueryRangeMonths1();
        } else {
            DateUtil.queryRangeMonths1 = "";
        }
        if (StringUtils.isNotBlank(query.getQueryRangeMonths2())) {
            DateUtil.queryRangeMonths2 = query.getQueryRangeMonths2();
        } else {
            DateUtil.queryRangeMonths2 = "";
        }
        PageableResult.PageableResultBuilder pageableResultBuilder = PageableResult.builder();
        try {
            Map<String, Set<SkuData2>> mapAreaCode = this.getQueryMapAreaCode();
//            System.out.println(mapAreaCode.size() + ", mapAreaCode----> " + mapAreaCode);
            this.dealList(mapAreaCode, page);
            return pageableResultBuilder.page(page).build();
        } catch (Exception e) {
            log.error("OrderTableQueryController", e);
            pageableResultBuilder.msg(e.getMessage());
            pageableResultBuilder.status("500");
            return pageableResultBuilder.build();
        }
    }

    public Map<String, Set<SkuData2>> getQueryMapAreaCode() {
        Set<CodeData> allCode = CodeInfoHandle.getAllCodeRange(DateUtil.getMonths1());
        Set<String> inCode = CodeInfoHandle.getInCodeStrRange(DateUtil.getMonths1());
        Set<CodeData> inCodeEntity = CodeInfoHandle.getInCode(allCode, inCode);
//        System.out.println("inCodeEntity====="+inCodeEntity.size()+"====="+inCodeEntity.toString());
//        Map<String, String> mapExcelResult = this.getOldToNewCode();
        Map<String, Set<SkuData2>> mapAreaCode = new HashMap<>();
        List<SkuData2> results1 = Oracle41SqlQuery.queryCmsData2();
        for (SkuData2 skuData2 : results1) {
            if (StringUtils.isNotBlank(skuData2.getWatt())) {
                skuData2.setWatt(String.valueOf(new BigDecimal(skuData2.getWatt())
                        .divide(new BigDecimal(1000000), 2, BigDecimal.ROUND_HALF_UP)));
            }
        }
        List<SkuData2> results2 = Oracle41SqlQuery.queryYTData2();
        results1.addAll(results2);
        List<SkuData2> result = results1.stream()
                .collect(Collectors.toMap(SkuData2::getSku, a -> a, (o1, o2)-> {
                    o1.setWatt(String.valueOf(this.getValue(o1.getWatt(),o2.getWatt())));
                    o1.setShortTypeCode(!Objects.equals(o1.getShortTypeCode(),o2.getShortTypeCode())
                            ?"集中式/分布式":o1.getShortTypeCode());
                    return o1;
                })).values().stream().collect(Collectors.toList());
        Map<String, SkuData2> mapResult = new HashMap<>();
        BigDecimal allWatt = new BigDecimal(0);
        BigDecimal allNum = new BigDecimal(0);
//       System.out.println("result====="+result.size()+"====="+result.toString());
        for (SkuData2 skuData2 : result) {
            if (StringUtils.isBlank(skuData2.getSku())) {
                continue;
            }
            // 过滤异常数据
            if (skuData2.getSku().trim().length() != 10){
                continue;
            }
            mapResult.put(skuData2.getSku().trim().substring(0, 10), skuData2);
            if (StringUtils.isNotBlank(skuData2.getWatt())) {
                allWatt = allWatt.add(new BigDecimal(skuData2.getWatt()));
            }
            allNum = allNum.add(new BigDecimal(skuData2.getNum()));

        }

        for (CodeData in : inCodeEntity) {
            if (mapAreaCode.get(in.getArea()) == null) {
                mapAreaCode.put(in.getArea(), new HashSet<>());
            }
            SkuData2 skuData2 = mapResult.get(in.getCode());
            if (Objects.nonNull(skuData2)) {
                skuData2.setNumPercent(this.getPercent(skuData2.getNum(), allNum, 4));
                skuData2.setWattPercent(this.getPercent(skuData2.getWatt(), allWatt, 4));
                skuData2.setSkuDesc(in.getCodeDesc());
                skuData2.setDateV01(in.getDate());
                skuData2.setDateFirst("");
                mapAreaCode.get(in.getArea()).add(skuData2);
            } else {
                if (StringUtils.isNotBlank(in.getCode())) {
                    SkuData2 skuData3 = new SkuData2();
                    skuData3.setSku(in.getCode());
                    skuData3.setNumPercent("");
                    skuData3.setWattPercent("");
                    skuData3.setSkuDesc(in.getCodeDesc());
                    skuData3.setDateV01(in.getDate());
                    skuData3.setDateFirst("");
                    mapAreaCode.get(in.getArea()).add(skuData3);
                }
            }
        }
        return mapAreaCode;
    }

    private String getValue(String watt1, String watt2) {
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

    private void dealList(Map<String, Set<SkuData2>> mapAreaCode, Page<SkuData3> page) {
        List<SkuData3> result = this.getList(mapAreaCode);
        long pageNumber = page.getCurrent();
        long limit = page.getSize();
        long startRow = (pageNumber - 1) * limit;

        int total = result.size();
        long pages = total % limit == 0 ? total / limit : total / limit + 1;
        List<SkuData3> subList = result.stream().sorted(Comparator.comparing(SkuData3::getAreaName))
                .skip(startRow).limit(limit).collect(Collectors.toList());
        page.setRecords(subList);
        page.setPages(pages);
        page.setTotal(total);
    }

    public List<SkuData3> getList(Map<String, Set<SkuData2>> mapAreaCode) {
        Map<String, String> map = CodeInfoHandle.getInCodeFirstDate().stream()
                .collect(Collectors.toMap(InCodeData::getCode, InCodeData::getFirstDate, (k1, k2) -> k1));
        List<SkuData3> result = new TreeList();
        mapAreaCode.forEach((key, value) ->
                value.forEach(v -> {
                    SkuData3 skuData3 = new SkuData3();
                    skuData3.setSku(v.getSku());
                    skuData3.setAreaName(key);

                    skuData3.setNum(v.getNum());
                    skuData3.setWattStr(v.getWatt());
                    skuData3.setWatt(StringUtils.isBlank(v.getWatt()) ? new BigDecimal(0) : new BigDecimal(v.getWatt()));
                    skuData3.setNumPercentStr(v.getNumPercent());
                    skuData3.setNumPercent(StringUtils.isBlank(v.getNumPercent()) ? new BigDecimal(0)
                            : new BigDecimal(v.getNumPercent().replaceAll("%", "")));
                    skuData3.setWattPercentStr(v.getWattPercent());
                    skuData3.setWattPercent(StringUtils.isBlank(v.getWattPercent()) ? new BigDecimal(0)
                            : new BigDecimal(v.getWattPercent().replaceAll("%", "")));
                    skuData3.setSkuDesc(v.getSkuDesc());
                    skuData3.setShortTypeCode(v.getShortTypeCode());
                    skuData3.setDateV01(v.getDateV01());
                    skuData3.setDateFirst(map.get(v.getSku()));
                    result.add(skuData3);
                }));
        return result;
    }

    private String getPercent(String num, BigDecimal allNum, int scale) {
        if (StringUtils.isBlank(num) || Objects.equals("0.00", num)) {
            return "";
        }
        return new BigDecimal(num).multiply(new BigDecimal(100))
                .divide(allNum, scale, BigDecimal.ROUND_HALF_UP)
                .setScale(scale, BigDecimal.ROUND_HALF_UP)
                + "%";
    }

  
    public void export(HttpServletResponse response) throws IOException {
        Map<String, Set<SkuData2>> mapAreaCode = this.getQueryMapAreaCode();
        List<SkuData3> list = this.getList(mapAreaCode);
        //导出
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("目录内编码在订单中的使用率"
                        , "目录内编码在订单中的使用率"), SkuData3.class,
                list);
        //ExcelUtil.export("目录内编码在订单中的使用率" + ".xls", workbook, response);
    }
}
