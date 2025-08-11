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




public class BarStackQueryController{

    /**
     * 模块名称
     */
    private static final String MODEL = "堆叠柱状图查询";

    /**
     * HTML页面路径前缀
     */
    private static final String HTML_PREFIX = "modules/manage/dataecharts/";


    /**
     * 返回页面
     *
     * @return {@link ModelAndView}
     */





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
        	
        	System.out.println("DateUtil.getMonths1()=====111======="+DateUtil.getMonths1());
            Set<CodeData> allCode = CodeInfoHandle.getAllCodeRange(DateUtil.getMonths1());
            System.out.println("allCode====111========"+allCode.size());
            Set<String> inCode = CodeInfoHandle.getInCodeStrRange(DateUtil.getMonths1());
            System.out.println("inCode===111========="+inCode.size());
            Set<CodeData> inCodeEntity = CodeInfoHandle.getInCode(allCode, inCode);
            System.out.println("inCodeEntity===111========="+inCodeEntity.size());
            Map<Integer, Set<String>> inCodeMap = this.getCodeMap(inCodeEntity);
            Set<CodeData> outCodeEntity = CodeInfoHandle.getOutCode(allCode, inCode);
            System.out.println("outCodeEntity====111========"+outCodeEntity.size());

            Map<Integer, Set<String>> outCodeMap = this.getCodeMap(outCodeEntity);

            List<SkuData> cmsData = Oracle41SqlQuery.queryCmsData(1);

            cmsData.forEach(cms -> {
                BigDecimal val = new BigDecimal(cms.getWatt());
                cms.setWatt(BigDecimal.ZERO.compareTo(val) == 0 ? "0" :
                        String.valueOf(val.divide(new BigDecimal(1000000), 2, BigDecimal.ROUND_HALF_UP)));
            });
            List<SkuData> ytData = Oracle41SqlQuery.queryYTData(1);

            List<SkuData> allData = this.getAllData(cmsData, ytData);

            Map<Integer, Set<String>> inCodeUseMap = new HashMap<>();
            inCodeMap.forEach((key, value) ->
                    allData.forEach(data -> {
                        if (StringUtils.isNotBlank(data.getSku()) && data.getSku().length() > 9) {
                            if (value.contains(data.getSku().substring(0, 10))) {
                                if (CollectionUtils.isEmpty(inCodeUseMap.get(key))) {
                                    inCodeUseMap.put(key, new HashSet<>());
                                }
                                inCodeUseMap.get(key).add(data.getSku().substring(0, 10));
                            }
                        }
                    }));

            Map<Integer, Set<String>> outCodeUseMap = new HashMap<>();
            outCodeMap.forEach((key, value) ->
                    allData.forEach(data -> {
                        if (StringUtils.isNotBlank(data.getSku()) && data.getSku().length() > 9) {
                            if (value.contains(data.getSku().substring(0, 10))) {
                                if (CollectionUtils.isEmpty(outCodeUseMap.get(key))) {
                                    outCodeUseMap.put(key, new HashSet<>());
                                }
                                outCodeUseMap.get(key).add(data.getSku().substring(0, 10));
                            }
                        }
                    }));
            Map<String, Object[]> map1 = this.getResultMap(inCodeMap, inCodeUseMap, "in");
            Map<String, Object[]> map2 = this.getResultMap(outCodeMap, outCodeUseMap, "out");
            Map<String, Object> result = new HashMap<>();
            map1.putAll(map2);
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

    private Map<String, Object[]> getResultMap(Map<Integer, Set<String>> codeMap
            , Map<Integer, Set<String>> codeUseMap, String flag) {
        Map<String, Object[]> resultMap = new HashMap<>();
        int maxSize = Collections.max(codeMap.keySet()) + 1;
        Integer[] data1 = new Integer[maxSize];
        Integer[] data2 = new Integer[maxSize];
        BigDecimal[] data3 = new BigDecimal[maxSize];

        codeMap.forEach((key, value) -> {
            int total = value.size();
            int use = 0;
            if (CollectionUtils.isNotEmpty(codeUseMap.get(key))) {
                use = codeUseMap.get(key).size();
            }
            data1[key] = total - use;
            data2[key] = use;
            data3[key] = this.getValue3(new BigDecimal(use), new BigDecimal(total));
        });
        resultMap.put(flag + "Data1", data1);
        resultMap.put(flag + "Data2", data2);
        resultMap.put(flag + "Data3", data3);
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
            }
        });
        return resultMap;
    }
}
