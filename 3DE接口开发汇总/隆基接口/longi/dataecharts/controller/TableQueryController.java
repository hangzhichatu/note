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

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import longi.dataecharts.AjaxResult;
import longi.dataecharts.DateUtil;
import longi.dataecharts.entity.CodeData;
import longi.dataecharts.entity.CodeDataChange;
import longi.dataecharts.handle.CodeInfoHandle;


import java.math.BigDecimal;
import java.util.*;


public class TableQueryController {

    /**
     * 模块名称
     */
    private static final String MODEL = "表格查询";

    /**
     * HTML页面路径前缀
     */
    private static final String HTML_PREFIX = "modules/manage/dataecharts/";


    //@LogAnnotation(model = MODEL, action = OperateLogConstant.SELECT_LIST)
    public AjaxResult queryData(String queryDateMonth) {
        if (StringUtils.isNotBlank(queryDateMonth)) {
            DateUtil.queryDate = queryDateMonth;
        } else {
            DateUtil.queryDate = "";
        }
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            // 上一个月
            Set<CodeData> allCodeEntity1 = CodeInfoHandle.getAllCode(DateUtil.getDateMonth());
            Set<String> allCodeStr1 = this.getCodeStr(allCodeEntity1);
            Set<String> inCode1 = CodeInfoHandle.getInCodeStr(DateUtil.getDateMonth());
            Set<CodeData> inCodeEntity1 = CodeInfoHandle.getInCode(allCodeEntity1, inCode1);
            Map<Integer, Set<CodeData>> inCodeMap1 = this.getCodeMap(inCodeEntity1);
            Set<CodeData> outCodeEntity1 = CodeInfoHandle.getOutCode(allCodeEntity1, inCode1);
            Set<String> outCodeStr1 = this.getCodeStr(outCodeEntity1);
            Map<Integer, Set<CodeData>> outCodeMap1 = this.getCodeMap(outCodeEntity1);

            // 上两个月
            Set<CodeData> allCodeEntity2 = CodeInfoHandle.getAllCode(DateUtil.getLast2Month());
            Set<String> allCodeStr2 = this.getCodeStr(allCodeEntity2);
            Set<String> inCode2 = CodeInfoHandle.getInCodeStr(DateUtil.getLast2Month());
            Set<CodeData> inCodeEntity2 = CodeInfoHandle.getInCode(allCodeEntity2, inCode2);
            Map<Integer, Set<CodeData>> inCodeMap2 = this.getCodeMap(inCodeEntity2);
            Set<CodeData> outCodeEntity2 = CodeInfoHandle.getOutCode(allCodeEntity2, inCode2);
            Set<String> outCodeStr2 = this.getCodeStr(outCodeEntity2);
            Map<Integer, Set<CodeData>> outCodeMap2 = this.getCodeMap(outCodeEntity2);

            // 新增编码
            Set<String> addCode = this.getCodeDiff(allCodeStr1, allCodeStr2);
            Set<String> addCodeIn = this.getCodeCross(addCode, inCode1);
            Set<CodeData> addCodeInEntity = this.getCodeEntity(addCodeIn, allCodeEntity1);
            Map<Integer, Set<CodeData>> addCodeInMap = this.getCodeMap(addCodeInEntity);
            Set<String> addCodeOut = this.getCodeDiff(addCode, addCodeIn);
            Set<CodeData> addCodeOutEntity = this.getCodeEntity(addCodeOut, allCodeEntity1);
            Map<Integer, Set<CodeData>> addCodeOutMap = this.getCodeMap(addCodeOutEntity);

            // 编码转移
            Set<String> inToOut = this.getCodeCross(inCode2, outCodeStr1);
            Set<CodeData> inToOutEntity = this.getCodeEntity(inToOut, allCodeEntity1);
            Map<Integer, Set<CodeData>> inToOutMap = this.getCodeMap(inToOutEntity);
            Set<String> outToIn = this.getCodeCross(outCodeStr2, inCode1);
            Set<CodeData> outToInEntity = this.getCodeEntity(outToIn, allCodeEntity1);
            Map<Integer, Set<CodeData>> outToInMap = this.getCodeMap(outToInEntity);

            List<CodeDataChange> codeDataChanges = this.getCodeDataChanges2();

            this.dealData(inCodeMap1, outCodeMap1, inCodeMap2, outCodeMap2, codeDataChanges
                    , addCodeInMap, addCodeOutMap, inToOutMap, outToInMap);

            String dateDay = DateUtil.getDateRange().replaceAll("-", "/");
            Integer[] dateMonth = DateUtil.getMonth();
            return ajaxResultBuilder.result(new HashMap<String, Object>() {{
                put("codeDataChanges", codeDataChanges);
                put("dateDay", dateDay);
                put("dateMonth", dateMonth);
                put("versionArray", new String[]{CodeInfoHandle.getVersion(DateUtil.getLast2Month())
                        , CodeInfoHandle.getVersion(DateUtil.getDateMonth())});
            }}).build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }

    private Set<CodeData> getCodeEntity(Set<String> codeStr, Set<CodeData> allCodeEntity) {
        Set<CodeData> codeDataSet = new HashSet<>();
        allCodeEntity.forEach(codeData -> {
            for (String code : codeStr) {
                if (Objects.equals(codeData.getCode(), code)) {
                    codeDataSet.add(codeData);
                    break;
                }
            }
        });
        return codeDataSet;
    }

    private Set<String> getCodeCross(Set<String> code1, Set<String> code2) {
        Set<String> set = new HashSet<>();
        set.addAll(code1);
        set.retainAll(code2);
        return set;
    }

    private Set<String> getCodeDiff(Set<String> allCodeStr1, Set<String> allCodeStr2) {
        Set<String> set = new HashSet<>();
        set.addAll(allCodeStr1);
        set.removeAll(allCodeStr2);
        return set;
    }

    private Set<String> getCodeStr(Set<CodeData> allCode) {
        Set<String> set = new HashSet<>();
        allCode.forEach(codeData -> set.add(codeData.getCode()));
        return set;
    }

    private void dealData(Map<Integer, Set<CodeData>> inCodeMap1
            , Map<Integer, Set<CodeData>> outCodeMap1
            , Map<Integer, Set<CodeData>> inCodeMap2
            , Map<Integer, Set<CodeData>> outCodeMap2
            , List<CodeDataChange> codeDataChanges
            , Map<Integer, Set<CodeData>> addCodeInMap
            , Map<Integer, Set<CodeData>> addCodeOutMap
            , Map<Integer, Set<CodeData>> inToOutMap
            , Map<Integer, Set<CodeData>> outToInMap) {
        // 中国0  拉美1  巴西2  亚太3  印度4  韩国5  美国6  加拿大7  欧洲8  法国9  中东非10

        inCodeMap2.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue2(String.valueOf(value.size()));
        });

        outCodeMap2.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue3(String.valueOf(value.size()));
        });

        inCodeMap1.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue9(String.valueOf(value.size()));
        });

        outCodeMap1.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue10(String.valueOf(value.size()));
        });

        addCodeInMap.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue4(String.valueOf(value.size()));
        });

        addCodeOutMap.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue5(String.valueOf(value.size()));
        });

        outToInMap.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue7(String.valueOf(value.size()));
        });

        inToOutMap.forEach((key, value) -> {
            CodeDataChange codeDataChange = codeDataChanges.get(key);
            codeDataChange.setValue8(String.valueOf(value.size()));
        });

        codeDataChanges.forEach(codeDataChange -> {
            if (StringUtils.isNotBlank(codeDataChange.getValue4()) && StringUtils.isNotBlank(codeDataChange.getValue2())) {
                codeDataChange.setValue11(this.getValue3(new BigDecimal(codeDataChange.getValue4())
                        , new BigDecimal(codeDataChange.getValue2())));
            } else {
                codeDataChange.setValue11("-");
            }

            if (StringUtils.isNotBlank(codeDataChange.getValue5()) && StringUtils.isNotBlank(codeDataChange.getValue3())) {
                codeDataChange.setValue12(this.getValue3(new BigDecimal(codeDataChange.getValue5()),
                        new BigDecimal(codeDataChange.getValue3())));
            } else {
                codeDataChange.setValue12("-");
            }
        });

        CodeDataChange codeDataChange11 = codeDataChanges.get(11);
        for (int i = 0; i < codeDataChanges.size() - 2; i++) {
            codeDataChange11.setValue2(this.getValue(codeDataChange11.getValue2()
                    , codeDataChanges.get(i).getValue2()));
            codeDataChange11.setValue3(this.getValue(codeDataChange11.getValue3()
                    , codeDataChanges.get(i).getValue3()));
            codeDataChange11.setValue4(this.getValue(codeDataChange11.getValue4()
                    , codeDataChanges.get(i).getValue4()));
            codeDataChange11.setValue5(this.getValue(codeDataChange11.getValue5()
                    , codeDataChanges.get(i).getValue5()));
            codeDataChange11.setValue6(this.getValue(codeDataChange11.getValue6()
                    , codeDataChanges.get(i).getValue6()));
            codeDataChange11.setValue7(this.getValue(codeDataChange11.getValue7()
                    , codeDataChanges.get(i).getValue7()));
            codeDataChange11.setValue8(this.getValue(codeDataChange11.getValue8()
                    , codeDataChanges.get(i).getValue8()));
            codeDataChange11.setValue9(this.getValue(codeDataChange11.getValue9()
                    , codeDataChanges.get(i).getValue9()));
            codeDataChange11.setValue10(this.getValue(codeDataChange11.getValue10()
                    , codeDataChanges.get(i).getValue10()));
        }
        codeDataChange11.setValue11(this.getValue3(new BigDecimal(codeDataChange11.getValue4())
                , new BigDecimal(codeDataChange11.getValue2())));
        codeDataChange11.setValue12(this.getValue3(new BigDecimal(codeDataChange11.getValue5()),
                new BigDecimal(codeDataChange11.getValue3())));
        CodeDataChange codeDataChange12 = codeDataChanges.get(12);
        // 2,3
        codeDataChange12.setValue1(this.getValue(codeDataChange11.getValue2(), codeDataChange11.getValue3()));
        // 4,5
        codeDataChange12.setValue2(this.getValue(codeDataChange11.getValue4(), codeDataChange11.getValue5()));
        // 7,8
        codeDataChange12.setValue3(this.getValue(codeDataChange11.getValue7(), codeDataChange11.getValue8()));
        // 9,10
        codeDataChange12.setValue4(this.getValue(codeDataChange11.getValue9(), codeDataChange11.getValue10()));
    }

    private String getAddValue(String value1, String value2) {
        if (StringUtils.isBlank(value1)) {
            value1 = "0";
        }
        if (StringUtils.isBlank(value2)) {
            value2 = "0";
        }
        Integer result = Integer.parseInt(value1) - Integer.parseInt(value2);
        return Objects.equals(0, result) ? "" : String.valueOf(result);
    }

    private String getValue3(BigDecimal bigDecimal, BigDecimal bigDecimal1) {
        if (BigDecimal.ZERO.compareTo(bigDecimal1) == 0) {
            return new BigDecimal(0.00) + "%";
        }
        bigDecimal = bigDecimal.multiply(new BigDecimal(100));
        return bigDecimal.divide(bigDecimal1, 0, BigDecimal.ROUND_HALF_UP) + "%";
    }

    private String getValue(String value11, String value) {
        Integer valueTotal = 0;
        valueTotal += Integer.parseInt(StringUtils.isNotBlank(value11) ? value11 : "0");
        valueTotal += Integer.parseInt(StringUtils.isNotBlank(value) ? value : "0");
        return valueTotal.toString();
    }

    private Map<Integer, Set<CodeData>> getCodeMap(Set<CodeData> codeEntitys) {
        Map<Integer, Set<CodeData>> resultMap = new HashMap<>();
        codeEntitys.forEach(codeEntity -> {
            if (Objects.equals(codeEntity.getArea(), "中国")) {
                if (CollectionUtils.isEmpty(resultMap.get(0))) {
                    resultMap.put(0, new HashSet<>());
                }
                resultMap.get(0).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "拉美")) {
                if (CollectionUtils.isEmpty(resultMap.get(1))) {
                    resultMap.put(1, new HashSet<>());
                }
                resultMap.get(1).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "巴西")) {
                if (CollectionUtils.isEmpty(resultMap.get(2))) {
                    resultMap.put(2, new HashSet<>());
                }
                resultMap.get(2).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "亚太")) {
                if (CollectionUtils.isEmpty(resultMap.get(3))) {
                    resultMap.put(3, new HashSet<>());
                }
                resultMap.get(3).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "印度")) {
                if (CollectionUtils.isEmpty(resultMap.get(4))) {
                    resultMap.put(4, new HashSet<>());
                }
                resultMap.get(4).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "韩国")) {
                if (CollectionUtils.isEmpty(resultMap.get(5))) {
                    resultMap.put(5, new HashSet<>());
                }
                resultMap.get(5).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "美国")) {
                if (CollectionUtils.isEmpty(resultMap.get(6))) {
                    resultMap.put(6, new HashSet<>());
                }
                resultMap.get(6).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "加拿大")) {
                if (CollectionUtils.isEmpty(resultMap.get(7))) {
                    resultMap.put(7, new HashSet<>());
                }
                resultMap.get(7).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "欧洲")) {
                if (CollectionUtils.isEmpty(resultMap.get(8))) {
                    resultMap.put(8, new HashSet<>());
                }
                resultMap.get(8).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "法国")) {
                if (CollectionUtils.isEmpty(resultMap.get(9))) {
                    resultMap.put(9, new HashSet<>());
                }
                resultMap.get(9).add(codeEntity);
            } else if (Objects.equals(codeEntity.getArea(), "中东非")) {
                if (CollectionUtils.isEmpty(resultMap.get(10))) {
                    resultMap.put(10, new HashSet<>());
                }
                resultMap.get(10).add(codeEntity);
            }
        });
        return resultMap;
    }


    private List<CodeDataChange> getCodeDataChanges2() {
        List<CodeDataChange> result = Lists.newArrayList();
        result.add(CodeDataChange.builder()
                .value1("中国").build());
        result.add(CodeDataChange.builder()
                .value1("拉美").build());
        result.add(CodeDataChange.builder()
                .value1("巴西").build());
        result.add(CodeDataChange.builder()
                .value1("亚太").build());
        result.add(CodeDataChange.builder()
                .value1("印度").build());
        result.add(CodeDataChange.builder()
                .value1("韩国").build());
        result.add(CodeDataChange.builder()
                .value1("美国").build());
        result.add(CodeDataChange.builder()
                .value1("加拿大").build());
        result.add(CodeDataChange.builder()
                .value1("欧洲").build());
        result.add(CodeDataChange.builder()
                .value1("法国").build());
        result.add(CodeDataChange.builder()
                .value1("中东非").build());
        result.add(CodeDataChange.builder()
                .value1("小计").build());
        result.add(CodeDataChange.builder()
                .build());
        return result;
    }


    private List<CodeDataChange> getCodeDataChanges() {
        List<CodeDataChange> result = Lists.newArrayList();
        result.add(CodeDataChange.builder()
                .value1("中国").value2("29").value3("121").value4("").value5("8").value6("")
                .value7("").value8("").value9("29").value10("129").value11("-").value12("7%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("拉美").value2("14").value3("14").value4("").value5("1").value6("")
                .value7("").value8("").value9("14").value10("15").value11("-").value12("7%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("巴西").value2("25").value3("26").value4("").value5("6").value6("")
                .value7("").value8("").value9("25").value10("32").value11("-").value12("23%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("亚太").value2("20").value3("50").value4("2").value5("2").value6("")
                .value7("").value8("").value9("22").value10("52").value11("10%").value12("4%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("印度").value2("5").value3("20").value4("").value5("").value6("")
                .value7("").value8("").value9("5").value10("20").value11("-").value12("-")
                .build());
        result.add(CodeDataChange.builder()
                .value1("韩国").value2("3").value3("3").value4("").value5("").value6("")
                .value7("").value8("").value9("3").value10("3").value11("-").value12("-")
                .build());
        result.add(CodeDataChange.builder()
                .value1("美国").value2("16").value3("15").value4("").value5("").value6("")
                .value7("").value8("").value9("16").value10("15").value11("-").value12("-")
                .build());
        result.add(CodeDataChange.builder()
                .value1("加拿大").value2("18").value3("3").value4("").value5("").value6("")
                .value7("").value8("").value9("18").value10("3").value11("-").value12("-")
                .build());
        result.add(CodeDataChange.builder()
                .value1("欧洲").value2("44").value3("178").value4("1").value5("3").value6("")
                .value7("").value8("").value9("45").value10("181").value11("2%").value12("2%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("法国").value2("9").value3("44").value4("").value5("1").value6("")
                .value7("").value8("").value9("9").value10("45").value11("-").value12("2%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("中东非").value2("13").value3("43").value4("2").value5("3").value6("")
                .value7("").value8("").value9("15").value10("46").value11("15%").value12("7%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("小计").value2("196").value3("517").value4("5").value5("24").value6("0")
                .value7("0").value8("0").value9("201").value10("541").value11("3%").value12("5%")
                .build());
        result.add(CodeDataChange.builder()
                .value1("713").value2("29").value3("0").value4("742")
                .build());

        return result;
    }
}
