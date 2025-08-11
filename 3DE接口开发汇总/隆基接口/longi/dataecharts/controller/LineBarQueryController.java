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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import longi.dataecharts.DateUtil;
import longi.dataecharts.entity.CodeData;
import longi.dataecharts.entity.ParamQuery;
import longi.dataecharts.entity.SkuData;
import longi.dataecharts.handle.CodeInfoHandle;
import longi.dataecharts.sqlquery.DpSqlQuery;
import longi.dataecharts.sqlquery.ErpSqlQuery;
import longi.dataecharts.sqlquery.Oracle41SqlQuery;
import longi.dataecharts.sqlquery.WmsSqlQuery;

import longi.dataecharts.AjaxResult;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class LineBarQueryController {

    /**
     * 模块名称
     */
    private static final String MODEL = "饼图查询";

    /**
     * HTML页面路径前缀
     */
    private static final String HTML_PREFIX = "modules/manage/dataecharts/";



    //@LogAnnotation(model = MODEL, action = OperateLogConstant.SELECT_LIST)
    public AjaxResult queryData1(ParamQuery query) {
        if (StringUtils.isNotBlank(query.getQueryRangeMonths1())) {
            DateUtil.queryRangeMonths1 = query.getQueryRangeMonths1();
        } else {
            DateUtil.queryRangeMonths1 = "";
        }
        if (Objects.equals(query.getDefaultFlag(), "1")) {
            DateUtil.queryRangeMonths2 = "";
        } else {
            if (StringUtils.isNotBlank(query.getQueryRangeMonths2())) {
                DateUtil.queryRangeMonths2 = query.getQueryRangeMonths2();
            } else {
                DateUtil.queryRangeMonths2 = "";
            }
        }

        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            Map<String, Object> result = new HashMap<>();

            Set<CodeData> allCode = CodeInfoHandle.getAllCodeRange(DateUtil.getMonths1());
            Set<String> inCode = CodeInfoHandle.getInCodeStrRange(DateUtil.getMonths1());
            Set<CodeData> inCodeEntity = CodeInfoHandle.getInCode(allCode, inCode);
            Set<CodeData> outCodeEntity = CodeInfoHandle.getOutCode(allCode, inCode);

            List<SkuData> dpData = DpSqlQuery.queryDpData();

            List<SkuData> cmsData = Oracle41SqlQuery.queryCmsData(3);
            List<SkuData> ytData = Oracle41SqlQuery.queryYTData(3);
            List<SkuData> allData = this.getAllData(cmsData, ytData);

            List<SkuData> erpData = ErpSqlQuery.list2();

            Map<String, List<SkuData>> wmsData = WmsSqlQuery.map2();
            List<SkuData> wmsInData = wmsData.get("in");
            List<SkuData> wmsOutData = wmsData.get("out");

            Set<String> totalUseInCode = new HashSet<>();
            Set<String> totalUseOutCode = new HashSet<>();


            Map<String, Object> dpMap1 = this.getValue(inCodeEntity, dpData, totalUseInCode);
            Map<String, Object> dpMap2 = this.getValue(outCodeEntity, dpData, totalUseOutCode);

            Map<String, Object> allMap1 = this.getValue(inCodeEntity, allData, totalUseInCode);
            Map<String, Object> allMap2 = this.getValue(outCodeEntity, allData, totalUseOutCode);

            Map<String, Object> erpMap1 = this.getValue(inCodeEntity, erpData, totalUseInCode);
            Map<String, Object> erpMap2 = this.getValue(outCodeEntity, erpData, totalUseOutCode);

            Map<String, Object> wmsInMap1 = this.getValue(inCodeEntity, wmsInData, totalUseInCode);
            Map<String, Object> wmsInMap2 = this.getValue(outCodeEntity, wmsInData, totalUseOutCode);

            Map<String, Object> wmsOutMap1 = this.getValue(inCodeEntity, wmsOutData, totalUseInCode);
            Map<String, Object> wmsOutMap2 = this.getValue(outCodeEntity, wmsOutData, totalUseOutCode);

            Map<String, Object> totalMap1 = this.getTotalValue(inCodeEntity, totalUseInCode);
            Map<String, Object> totalMap2 = this.getTotalValue(outCodeEntity, totalUseOutCode);


            Map<String, Object[]> map1 = this.getResultMap(dpMap1, allMap1, erpMap1, wmsInMap1, wmsOutMap1, totalMap1, "in");
            Map<String, Object[]> map2 = this.getResultMap(dpMap2, allMap2, erpMap2, wmsInMap2, wmsOutMap2, totalMap2, "out");
            map1.putAll(map2);

            result.put("data", map1);
            result.put("dateTime1", DateUtil.getLastThreeMonthFirstDay2().replaceAll("-", "/")
                    + "~" + DateUtil.getLastOneMonthLastDay2().replaceAll("-", "/"));
            String start = DateUtil.getLastThreeMonthFirstDay2().substring(0, 7);
            String end = DateUtil.getLastOneMonthLastDay2().substring(0, 7);
            result.put("dateTime2", start + " - " + end);
            String[] month11 = start.split("-");
            String[] month12 = end.split("-");
            int year1 = Integer.parseInt(month11[0]);
            int month1 = Integer.parseInt(month11[1]);
            int year2 = Integer.parseInt(month12[0]);
            int month2 = Integer.parseInt(month12[1]);
            int months = (year2 - year1) * 12 + (month2 - month1) + 1;
            result.put("months", months);
            return ajaxResultBuilder.result(result).build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }

    private Map<String, Object> getTotalValue(Set<CodeData> codeEntity, Set<String> totalUseCode) {
        Map<String, Object> resultMap = new HashMap<String, Object>() {{
            put("all", codeEntity.size());
            put("use", totalUseCode.size());
            put("percent", new BigDecimal(0));
        }};
        resultMap.put("percent", this.getValue3(new BigDecimal((Integer) resultMap.get("use")),
                new BigDecimal((Integer) resultMap.get("all"))));
        return resultMap;
    }

    private Map<String, Object[]> getResultMap(Map<String, Object> dpMap1, Map<String, Object> allMap1
            , Map<String, Object> erpMap1, Map<String, Object> wmsInMap1, Map<String, Object> wmsOutMap1
            , Map<String, Object> totalMap1, String flag) {
        Integer[] data1 = {((Integer) dpMap1.get("all")), ((Integer) allMap1.get("all"))
                , ((Integer) erpMap1.get("all")), ((Integer) wmsInMap1.get("all")), ((Integer) wmsOutMap1.get("all"))
                , ((Integer) totalMap1.get("all"))};
        Integer[] data2 = {((Integer) dpMap1.get("use")), ((Integer) allMap1.get("use"))
                , ((Integer) erpMap1.get("use")), ((Integer) wmsInMap1.get("use")), ((Integer) wmsOutMap1.get("use"))
                , ((Integer) totalMap1.get("use"))};
        BigDecimal[] data3 = {((BigDecimal) dpMap1.get("percent")), ((BigDecimal) allMap1.get("percent"))
                , ((BigDecimal) erpMap1.get("percent")), ((BigDecimal) wmsInMap1.get("percent"))
                , ((BigDecimal) wmsOutMap1.get("percent")), ((BigDecimal) totalMap1.get("percent"))};
        Map<String, Object[]> resultMap = new HashMap<>();
        resultMap.put(flag + "Data1", data1);
        resultMap.put(flag + "Data2", data2);
        resultMap.put(flag + "Data3", data3);
        return resultMap;
    }

    private Map<String, Object> getValue(Set<CodeData> codeEntity, List<SkuData> useData, Set<String> totalUseCode) {
        Map<String, Object> resultMap = new HashMap<String, Object>() {{
            put("all", 0);
            put("use", 0);
            put("percent", new BigDecimal(0));
        }};
        if (CollectionUtils.isNotEmpty(codeEntity) && CollectionUtils.isNotEmpty(useData)) {
            Set<String> useCode = new HashSet<>();
            useData.forEach(use -> {
                if (StringUtils.isNotBlank(use.getSku()) && use.getSku().length() > 9) {
                    useCode.add(use.getSku().substring(0, 10));
                }
            });
            codeEntity.forEach(codeE ->
                    useCode.forEach(code -> {
                        if (Objects.equals(codeE.getCode(), code)) {
                            totalUseCode.add(code);
                            resultMap.put("use", ((Integer) resultMap.get("use")) + 1);
                        }
                    }));
            resultMap.put("all", codeEntity.size());
            resultMap.put("percent", this.getValue3(new BigDecimal((Integer) resultMap.get("use")),
                    new BigDecimal((Integer) resultMap.get("all"))));
        }
        return resultMap;
    }

    private BigDecimal getValue3(BigDecimal bigDecimal, BigDecimal bigDecimal1) {
        if (BigDecimal.ZERO.compareTo(bigDecimal1) == 0) {
            return new BigDecimal(0.00);
        }
        bigDecimal = bigDecimal.multiply(new BigDecimal(100));
        return bigDecimal.divide(bigDecimal1, 2, BigDecimal.ROUND_HALF_UP);
    }

    private List<SkuData> getAllData(List<SkuData> cmsData, List<SkuData> ytData) {
        List<SkuData> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cmsData)) {
            list.addAll(cmsData);
        }
        if (CollectionUtils.isNotEmpty(ytData)) {
            list.addAll(ytData);
        }
        List<SkuData> result = list.stream()
                .collect(Collectors.toMap(SkuData::getSku, a -> a, (o1, o2)-> {
                    o1.setWatt(String.valueOf(new BigDecimal(o1.getWatt())
                            .add(new BigDecimal(o2.getWatt()))));
                    return o1;
                })).values().stream().collect(Collectors.toList());
        return result;
    }
}
