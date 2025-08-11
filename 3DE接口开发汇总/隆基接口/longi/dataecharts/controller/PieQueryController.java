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
import longi.dataecharts.entity.SkuData;
import longi.dataecharts.handle.CodeInfoHandle;
import longi.dataecharts.sqlquery.ErpSqlQuery;

import longi.dataecharts.AjaxResult;


import java.math.BigDecimal;
import java.util.*;



public class PieQueryController {

    /**
     * 模块名称
     */
    private static final String MODEL = "饼图查询";

    /**
     * HTML页面路径前缀
     */
    private static final String HTML_PREFIX = "modules/manage/dataecharts/";



    //@LogAnnotation(model = MODEL, action = OperateLogConstant.SELECT_LIST)
    public AjaxResult queryData1(String queryDateMonth) {
        if (StringUtils.isNotBlank(queryDateMonth)) {
            DateUtil.queryDate = queryDateMonth;
        } else {
            DateUtil.queryDate = "";
        }
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            Set<CodeData> allCode = CodeInfoHandle.getAllCode(DateUtil.getDateMonth());
            Set<String> inCode = CodeInfoHandle.getInCodeStr(DateUtil.getDateMonth());
            Set<CodeData> inCodeEntity = CodeInfoHandle.getInCode(allCode, inCode);
            Set<CodeData> outCodeEntity = CodeInfoHandle.getOutCode(allCode, inCode);
            Map<String, Object> result = new HashMap<>();
            int inCodeSize = inCodeEntity.size();
            int outCodeSize = outCodeEntity.size();
            int[] data1 = {inCodeSize, outCodeSize};
            result.put("data1", data1);
            result.put("total1", inCodeSize + outCodeSize);
            result.put("dateTime1", DateUtil.getDateStart().replaceAll("-", "/"));
            result.put("versionNo", CodeInfoHandle.getVersion(DateUtil.getDateMonth()));
            return ajaxResultBuilder.result(result).build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }

    //@LogAnnotation(model = MODEL, action = OperateLogConstant.SELECT_LIST)
    public AjaxResult queryData2(String queryDateMonth) {
        if (StringUtils.isNotBlank(queryDateMonth)) {
            DateUtil.queryDate = queryDateMonth;
        } else {
            DateUtil.queryDate = "";
        }
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            // 新编码 10位基准码，"80A"或"80X"开头，末尾为"R"的是研发码，不参与统计
            List<SkuData> allSkuData = ErpSqlQuery.list("1");
            Set<SkuData> newSku = new HashSet<>();
            Set<SkuData> oldSku = new HashSet<>();
            if (CollectionUtils.isNotEmpty(allSkuData)) {
                allSkuData.forEach(skuData -> {
                    if (!skuData.getSku().endsWith("R")) {
                        if (skuData.getSku().startsWith("80A") || skuData.getSku().startsWith("80X")) {
                            newSku.add(skuData);
                        } else {
                            oldSku.add(skuData);
                        }
                    }
                });
            }
            Map<String, Object> result = new HashMap<>();
            BigDecimal newWatt = new BigDecimal(0);
            for (SkuData skuData : newSku) {
                newWatt = newWatt.add(new BigDecimal(skuData.getWatt()));
            }
            BigDecimal oldWatt = new BigDecimal(0);
            for (SkuData skuData : oldSku) {
                oldWatt = oldWatt.add(new BigDecimal(skuData.getWatt()));
            }
            BigDecimal[] data2 = {
                    newWatt.divide(new BigDecimal(1000000), 2, BigDecimal.ROUND_HALF_UP)
                    , oldWatt.divide(new BigDecimal(1000000), 2, BigDecimal.ROUND_HALF_UP)};
            result.put("data2", data2);
            result.put("total2", data2[0].add(data2[1]));
            result.put("dateTime2", DateUtil.getDateRange().replaceAll("-", "/"));
            return ajaxResultBuilder.result(result).build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }

}
