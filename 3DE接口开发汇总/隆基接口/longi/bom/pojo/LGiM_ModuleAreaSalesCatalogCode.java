package longi.bom.pojo;

import java.util.Map;

/**
 * @ClassName LGiM_ModuleAreaSalesCatalogCode
 * @Description TODO
 * @Author Longi.liqitong
 * @Date 2022-8-24 10:58
 * Version 1.0
 */
public class LGiM_ModuleAreaSalesCatalogCode {

    private String salesRegional;
    private String itemCode;
    private String itemRegional;
    private String startDate;
    private String endDate;
    private String itemSchemeDesc;
    private String itemSchemeDescEN;
    private String itemBOMCostVariance;
    private String itemBasicCode;
    private String itemUseContext;
    private String itemCodeCNDesc;
    private String releaseDate;
    private String itemProductLine;
    private String itemProductSeries;
    private String itemVersionExtension;
    private Map itemDataMap;

    public String getSalesRegional() {
        return salesRegional;
    }

    public void setSalesRegional(String salesRegional) {
        this.salesRegional = salesRegional;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemRegional() {
        return itemRegional;
    }

    public void setItemRegional(String itemRegional) {
        this.itemRegional = itemRegional;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getItemSchemeDesc() {
        return itemSchemeDesc;
    }

    public void setItemSchemeDesc(String itemSchemeDesc) {
        this.itemSchemeDesc = itemSchemeDesc;
    }

    public String getItemSchemeDescEN() {
        return itemSchemeDescEN;
    }

    public void setItemSchemeDescEN(String itemSchemeDescEN) {
        this.itemSchemeDescEN = itemSchemeDescEN;
    }

    public String getItemBOMCostVariance() {
        return itemBOMCostVariance;
    }

    public void setItemBOMCostVariance(String itemBOMCostVariance) {
        this.itemBOMCostVariance = itemBOMCostVariance;
    }

    public String getItemBasicCode() {
        return itemBasicCode;
    }

    public void setItemBasicCode(String itemBasicCode) {
        this.itemBasicCode = itemBasicCode;
    }

    public String getItemUseContext() {
        return itemUseContext;
    }

    public void setItemUseContext(String itemUseContext) {
        this.itemUseContext = itemUseContext;
    }

    public String getItemCodeCNDesc() {
        return itemCodeCNDesc;
    }

    public void setItemCodeCNDesc(String itemCodeCNDesc) {
        this.itemCodeCNDesc = itemCodeCNDesc;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getItemProductLine() {
        return itemProductLine;
    }

    public void setItemProductLine(String itemProductLine) {
        this.itemProductLine = itemProductLine;
    }

    public String getItemProductSeries() {
        return itemProductSeries;
    }

    public void setItemProductSeries(String itemProductSeries) {
        this.itemProductSeries = itemProductSeries;
    }

    public String getItemVersionExtension() {
        return itemVersionExtension;
    }

    public void setItemVersionExtension(String itemVersionExtension) {
        this.itemVersionExtension = itemVersionExtension;
    }

    public Map getItemDataMap() {
        return itemDataMap;
    }

    public void setItemDataMap(Map itemDataMap) {
        this.itemDataMap = itemDataMap;
    }

    @Override
    public String toString() {
        return "LGiM_ModuleAreaSalesCatalogCode{" +
                "itemCode='" + itemCode + '\'' +
                ", itemProductLine='" + itemProductLine + '\'' +
                ", itemProductSeries='" + itemProductSeries + '\'' +
                ", itemVersionExtension='" + itemVersionExtension + '\'' +
                '}';
    }
}
