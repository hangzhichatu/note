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

import longi.dataecharts.FileWriterUtil;
import longi.dataecharts.handle.CodeInfoHandle;
import longi.dataecharts.sqlquery.ErpSqlQuery;
import longi.dataecharts.sqlquery.WmsSqlQuery;
import matrix.db.Context;
import longi.dataecharts.AjaxResult;


import java.util.ArrayList;
import java.util.List;



public class DataSyncController {


    public AjaxResult queryErpData() {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            ErpSqlQuery.writeFile("erp-"
                    + FileWriterUtil.getFileNameMonth() + ".txt", "1");
            return ajaxResultBuilder.build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }

    public AjaxResult queryErpData3() {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            ErpSqlQuery.writeFile("erp-3-"
                    + FileWriterUtil.getFileNameMonth(), "3");
            return ajaxResultBuilder.build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }


    public AjaxResult queryWmsData() {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            WmsSqlQuery.writeFile("wms-3-"
                    + FileWriterUtil.getFileNameMonth() + ".txt");
            return ajaxResultBuilder.build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }


    public AjaxResult queryPLMDataCode(Context context) {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        try {
            CodeInfoHandle.writeFile(context,"plm-code-"
                    + FileWriterUtil.getFileNameMonth() + ".txt");
            return ajaxResultBuilder.build();
        } catch (Exception e) {
            ajaxResultBuilder.msg(e.getMessage());
            ajaxResultBuilder.status("500");
            ajaxResultBuilder.result(null);
            return ajaxResultBuilder.build();
        }
    }



    public AjaxResult queryWmsData2() {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        List<String> params = new ArrayList<String>() {{
            add("2022-08##2022-08-31");
            add("2022-09##2022-09-30");
            add("2022-10##2022-10-31");
            add("2022-11##2022-11-30");
            add("2022-12##2022-12-31");
            add("2023-01##2023-01-31");
            add("2023-02##2023-02-28");
            add("2023-03##2023-03-31");
        }};
        params.forEach(param -> {
            String[] array = param.split("##");
            try {
                WmsSqlQuery.writeFile("wms-1-"
                        + array[0] + ".txt", array[0] + "-01", array[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ajaxResultBuilder.build();
    }

    public AjaxResult queryErpData2() {
        AjaxResult.AjaxResultBuilder ajaxResultBuilder = AjaxResult.builder();
        List<String> params = new ArrayList<String>() {{
            add("2022-08##2022-08-31");
            add("2022-09##2022-09-30");
            add("2022-10##2022-10-31");
            add("2022-11##2022-11-30");
            add("2022-12##2022-12-31");
            add("2023-01##2023-01-31");
            add("2023-02##2023-02-28");
            add("2023-03##2023-03-31");
        }};
        params.forEach(param -> {
            String[] array = param.split("##");
            try {
                ErpSqlQuery.writeFile("erp-1-"
                        + array[0] + ".txt", array[0] + "-01", array[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ajaxResultBuilder.build();

    }
}
