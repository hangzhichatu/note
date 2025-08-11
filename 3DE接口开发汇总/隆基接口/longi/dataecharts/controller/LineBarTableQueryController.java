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
import longi.dataecharts.sqlquery.Oracle41SqlQuery;

import longi.dataecharts.AjaxResult;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



public class LineBarTableQueryController {

    /**
     * 模块名称
     */
    private static final String MODEL = "堆叠柱状图查询";

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
        if (StringUtils.isNotBlank(query.getQueryRangeMonths2())) {
            DateUtil.queryRangeMonths2 = query.getQueryRangeMonths2();
        } else {
            DateUtil.queryRangeMonths2 = "";
        }
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            Set<CodeData> allCode = CodeInfoHandle.getAllCodeRange(DateUtil.getMonths1());
            Set<String> inCode = CodeInfoHandle.getInCodeStrRange(DateUtil.getMonths1());
            Set<CodeData> inCodeEntity = CodeInfoHandle.getInCode(allCode, inCode);

            Map<Integer, Set<String>> inCodeMap = this.getCodeMap(inCodeEntity);
            System.out.println("inCodeMap---->" + inCodeMap.size());

            Map<Integer, Set<String>> allCodeMap = this.getCodeMap(allCode);
            System.out.println("allCodeMap--->" + allCodeMap.size());

            List<SkuData> cmsData = Oracle41SqlQuery.queryCmsData(1);
            cmsData.forEach(cms -> {
                BigDecimal val = new BigDecimal(cms.getWatt());
                cms.setWatt(BigDecimal.ZERO.compareTo(val) == 0 ? "0" :
                        String.valueOf(val.divide(new BigDecimal(1000000), 2, BigDecimal.ROUND_HALF_UP)));
            });

            List<SkuData> ytData = Oracle41SqlQuery.queryYTData(1);


            List<SkuData> allData = this.getAllData(cmsData, ytData);
            System.out.println("allData--->" + allData.size());



            Map<Integer, List<String>> inCodeUseMap = new HashMap<>();
            inCodeMap.forEach((key, value) ->
                    allData.forEach(data -> {
                        if (StringUtils.isNotBlank(data.getSku()) && data.getSku().length() > 9) {
                            if (value.contains(data.getSku().substring(0, 10))) {
                                if (CollectionUtils.isEmpty(inCodeUseMap.get(key))) {
                                    inCodeUseMap.put(key, new ArrayList<>());
                                }
                                inCodeUseMap.get(key).add(data.getWatt());
                            }
                        }
                    }));
            System.out.println("inCodeUseMap--->" + inCodeUseMap.size());

            Map<Integer, List<String>> allCodeUseMap = new HashMap<>();
            allCodeMap.forEach((key, value) ->
                    allData.forEach(data -> {
                        if (StringUtils.isNotBlank(data.getSku()) && data.getSku().length() > 9) {
                            if (value.contains(data.getSku().substring(0, 10))) {
                                if (CollectionUtils.isEmpty(allCodeUseMap.get(key))) {
                                    allCodeUseMap.put(key, new ArrayList<>());
                                }
                                allCodeUseMap.get(key).add(data.getWatt());
                            }
                        }
                    }));
            System.out.println("allCodeUseMap--->" + allCodeUseMap.size());
            //显示全部数据，inCodeMap->allCodeMap
            Map<String, Object[]> map1 = this.getResultMap(allCodeMap, inCodeUseMap, allCodeUseMap);
            Map<String, Object> result = new HashMap<>();
            result.put("data", map1);
            String[] months = DateUtil.getMonths2();
            result.put("dateTime1", months[0].replaceAll("-", "/")
                    + "~" + months[1].replaceAll("-", "/"));
            return ajaxResultBuilder.result(result).build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
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

    private Map<String, Object[]> getResultMap(Map<Integer, Set<String>> allCodeMap
            , Map<Integer, List<String>> inCodeUseMap
            , Map<Integer, List<String>> allCodeUseMap) {
        Map<String, Object[]> resultMap = new HashMap<>();
        BigDecimal[] data1 = new BigDecimal[allCodeMap.size() + 1];
        BigDecimal[] data2 = new BigDecimal[allCodeMap.size() + 1];
        BigDecimal[] data3 = new BigDecimal[allCodeMap.size() + 1];
        allCodeMap.forEach((key, value) -> {
            BigDecimal all = new BigDecimal(0);
            BigDecimal use = new BigDecimal(0);
            if (CollectionUtils.isNotEmpty(allCodeUseMap.get(key))) {
                all = this.getWattValue(allCodeUseMap.get(key));
            }
            if (CollectionUtils.isNotEmpty(inCodeUseMap.get(key))) {
                use = this.getWattValue(inCodeUseMap.get(key));
            }
            data1[key] = all;
            data2[key] = use;
            data3[key] = this.getValue3(data2[key], data1[key]);
        });
        BigDecimal total1 = new BigDecimal(0);
        for (BigDecimal value : data1) {
            if (value != null) {
                total1 = total1.add(value);
            }
        }
        BigDecimal total2 = new BigDecimal(0);
        for (BigDecimal value : data2) {
            if (value != null) {
                total2 = total2.add(value);
            }
        }
        data1[data1.length - 1] = total1;
        data2[data2.length - 1] = total2;
        data3[data3.length - 1] = this.getValue3(total2, total1);
        resultMap.put("data1", data1);
        resultMap.put("data2", data2);
        resultMap.put("data3", data3);
        return resultMap;
    }

    private BigDecimal getWattValue(List<String> lists) {
        BigDecimal use = new BigDecimal(0);
        for (String watt : lists) {
            if (StringUtils.isNotBlank(watt)) {
                use = use.add(new BigDecimal(watt));
            }
        }
        return use;
    }

    private BigDecimal getValue3(BigDecimal bigDecimal, BigDecimal bigDecimal1) {
        if (BigDecimal.ZERO.compareTo(bigDecimal1) == 0) {
            return new BigDecimal(0.00);
        }
        bigDecimal = bigDecimal.multiply(new BigDecimal(100));
        return bigDecimal.divide(bigDecimal1, 2, BigDecimal.ROUND_HALF_UP);
    }

    private Map<Integer, Set<String>> getCodeMap(Set<CodeData> codeEntitys) {
        Map<Integer, Set<String>> resultMap = new HashMap<>();
        codeEntitys.forEach(codeEntity -> {
            if (Objects.equals(codeEntity.getArea(), "中国")) {
                if (CollectionUtils.isEmpty(resultMap.get(0))) {
                    resultMap.put(0, new HashSet<>());
                }
                resultMap.get(0).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "中东非")) {
                if (CollectionUtils.isEmpty(resultMap.get(1))) {
                    resultMap.put(1, new HashSet<>());
                }
                resultMap.get(1).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "印度")) {
                if (CollectionUtils.isEmpty(resultMap.get(2))) {
                    resultMap.put(2, new HashSet<>());
                }
                resultMap.get(2).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "亚太")) {
                if (CollectionUtils.isEmpty(resultMap.get(3))) {
                    resultMap.put(3, new HashSet<>());
                }
                resultMap.get(3).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "欧洲")) {
                if (CollectionUtils.isEmpty(resultMap.get(4))) {
                    resultMap.put(4, new HashSet<>());
                }
                resultMap.get(4).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "美国")) {
                if (CollectionUtils.isEmpty(resultMap.get(5))) {
                    resultMap.put(5, new HashSet<>());
                }
                resultMap.get(5).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "拉美")) {
                if (CollectionUtils.isEmpty(resultMap.get(6))) {
                    resultMap.put(6, new HashSet<>());
                }
                resultMap.get(6).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "加拿大")) {
                if (CollectionUtils.isEmpty(resultMap.get(7))) {
                    resultMap.put(7, new HashSet<>());
                }
                resultMap.get(7).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "韩国")) {
                if (CollectionUtils.isEmpty(resultMap.get(8))) {
                    resultMap.put(8, new HashSet<>());
                }
                resultMap.get(8).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "法国")) {
                if (CollectionUtils.isEmpty(resultMap.get(9))) {
                    resultMap.put(9, new HashSet<>());
                }
                resultMap.get(9).add(codeEntity.getCode());
            } else if (Objects.equals(codeEntity.getArea(), "巴西")) {
                if (CollectionUtils.isEmpty(resultMap.get(10))) {
                    resultMap.put(10, new HashSet<>());
                }
                resultMap.get(10).add(codeEntity.getCode());
            }else if (Objects.equals(codeEntity.getArea(), "澳洲")) {
                if (CollectionUtils.isEmpty(resultMap.get(11))) {
                    resultMap.put(11, new HashSet<>());
                }
                resultMap.get(11).add(codeEntity.getCode());
            }else if (Objects.equals(codeEntity.getArea(), "欧洲&澳洲")) {
                if (CollectionUtils.isEmpty(resultMap.get(12))) {
                    resultMap.put(12, new HashSet<>());
                }
                resultMap.get(12).add(codeEntity.getCode());
            }else if (Objects.equals(codeEntity.getArea(), "亚太&拉美&中东非")) {
                if (CollectionUtils.isEmpty(resultMap.get(13))) {
                    resultMap.put(13, new HashSet<>());
                }
                resultMap.get(13).add(codeEntity.getCode());
            }
        });
        return resultMap;
    }
}
