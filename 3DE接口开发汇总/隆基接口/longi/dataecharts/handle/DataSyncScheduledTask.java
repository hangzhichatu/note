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

package longi.dataecharts.handle;

import lombok.extern.slf4j.Slf4j;
import longi.dataecharts.FileWriterUtil;
import longi.dataecharts.sqlquery.ErpSqlQuery;
import longi.dataecharts.sqlquery.WmsSqlQuery;
import matrix.db.Context;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;



@Slf4j
public class DataSyncScheduledTask {


    public void queryErpData() {
        try {
        	System.out.println("同步ERP数据  开始");
            //log.info("同步ERP数据  开始");
            ErpSqlQuery.writeFile("erp-1-"
                    + FileWriterUtil.getFileNameMonth(), getStartDate(), getEndDate());
            System.out.println("同步ERP数据  结束");
            //log.info("同步ERP数据  结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void queryWmsData() {
        try {
        	System.out.println("同步WMS数据  开始");
            //log.info("同步WMS数据  开始");
            WmsSqlQuery.writeFile("wms-1-"
                    + FileWriterUtil.getFileNameMonth(), getStartDate(), getEndDate());
            System.out.println("同步WMS数据  结束");
            //log.info("同步WMS数据  结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 
    public void queryPLMDataCode(Context context) {
        try {
        	System.out.println("同步PLMCode数据  开始");
            //log.info("同步PLMCode数据  开始");
            CodeInfoHandle.writeFile(context,"plm-code-"
                    + FileWriterUtil.getFileNameMonth());
            //log.info("同步PLMCode数据  结束");
            System.out.println("同步PLMCode数据  结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStartDate() {
        return DateTimeFormatter.ofPattern("yyyy-MM").format(
                LocalDate.now().minusMonths(1)) + "-01";
    }

    public String getEndDate() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                LocalDate.now().minusMonths(1)
                        .with(TemporalAdjusters.lastDayOfMonth()));
    }

}
