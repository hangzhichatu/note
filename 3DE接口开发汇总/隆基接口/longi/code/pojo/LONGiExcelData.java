package longi.code.pojo;
import java.util.List;

/**
 * @ClassName: LONGiExcelData
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-6-10 10:02
 */

public class LONGiExcelData {

    private String sheetName;
    private List headerList;
    private List contentList;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public List getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List headerList) {
        this.headerList = headerList;
    }

    public List getContentList() {
        return contentList;
    }

    public void setContentList(List contentList) {
        this.contentList = contentList;
    }

    public LONGiExcelData(String sheetName, List headerList, List contentList) {
        this.sheetName = sheetName;
        this.headerList = headerList;
        this.contentList = contentList;
    }

    @Override
    public String toString() {
        return "LONGiExcelData{" +
                "sheetName='" + sheetName + '\'' +
                ", headerList=" + headerList +
                ", contentList=" + contentList +
                '}';
    }
}
