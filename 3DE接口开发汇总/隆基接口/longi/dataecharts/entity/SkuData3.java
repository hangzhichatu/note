package longi.dataecharts.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuData3 {
    @Excel(name = "区域", orderNum = "1", width = 15, isImportField = "true")
    private String areaName;
    @Excel(name = "编码", orderNum = "2", width = 15, isImportField = "true")
    private String sku;
    @Excel(name = "订单量MW统计值", orderNum = "3", width = 20, isImportField = "true", type = 10)
    private BigDecimal watt;
    private String wattStr;
    //    @Excel(name = "订单量数量", orderNum = "4", width = 25, isImportField = "false")
    private String num;
    @Excel(name = "容量统计利用率(%)", orderNum = "5", width = 20, isImportField = "true"
            ,type = 10)
    private BigDecimal wattPercent;
    private String wattPercentStr;
    @Excel(name = "数量统计利用率(%)", orderNum = "6", width = 20, isImportField = "true"
            ,type = 10)
    private BigDecimal numPercent;
    private String numPercentStr;
    @Excel(name = "编码描述", orderNum = "7", width = 65, isImportField = "true")
    private String skuDesc;
    @Excel(name = "分布式/集中式", orderNum = "8", width = 15, isImportField = "true")
    private String shortTypeCode;
    @Excel(name = "编码V01版本发布日期", orderNum = "9", width = 20, isImportField = "true")
    private String dateV01;
    @Excel(name = "编码首次加入的可销售目录的时间", orderNum = "10", width = 20, isImportField = "true")
    private String dateFirst;
}
