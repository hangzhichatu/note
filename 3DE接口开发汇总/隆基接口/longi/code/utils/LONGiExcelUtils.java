package longi.code.utils;

import longi.code.pojo.LONGiExcelData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ExcelUtils
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-6-10 10:02
 */

public class LONGiExcelUtils {

    /* excel文件后缀 */
    private final static String excel2003L = ".xls"; // 2003- 版本的excel
    private final static String excel2007U = ".xlsx"; // 2007+ 版本的excel

    /**
     * @Author: Longi.liqitong
     * @Title: getListByExcel
     * @Description: Excel导入
     * @Param: [fileName]
     * @Return: java.util.List<java.util.List < java.lang.Object>>
     * @Date: 2020-6-10 11:35
     */
    public static List<LONGiExcelData> readExcel(String fileName) throws Exception {
        List<LONGiExcelData> excelDateList = new ArrayList<LONGiExcelData>();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            // 创建Excel工作薄
            Workbook workBook = getWorkbook(inputStream, fileName);
            if (null == workBook) {
                throw new Exception("创建Excel工作薄为空！");
            }
            Sheet sheet = null;
            String sheetName = null;
            Row row = null;
            Cell cell = null;
            int rowIndex = 0;
            int columnIndex = 0;
            // 遍历Excel中所有的sheet
            for (int sheetIndex = 0; sheetIndex < workBook.getNumberOfSheets(); sheetIndex++) {
                sheet = workBook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                sheetName = sheet.getSheetName();
                int firstRowIndex = sheet.getFirstRowNum();
                // 获取标题行
                row = sheet.getRow(firstRowIndex);
                if (row == null) {
                    continue;
                }
                List<String> headerList = new ArrayList<String>();
                for (columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
                    cell = row.getCell(columnIndex);
                    if (cell == null) {
                        break;
                    }
                    headerList.add(getCellValue(cell));
                }
                List<List<String>> contentList = new ArrayList<List<String>>();
                // 遍历当前sheet中除表头的的所有行
                for (rowIndex = firstRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    // 读取内容行
                    row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }
                    boolean isEmpty = isRowEmpty(row);
                    if (isEmpty) {
                        break;
                    }
                    // 遍历内容列
                    List<String> rowList = new ArrayList<String>();
                    for (columnIndex = 0; columnIndex < headerList.size(); columnIndex++) {
                        cell = row.getCell(columnIndex);
                        if (cell == null) {
                            cell = row.createCell(columnIndex);
                        }
                        String value = getCellValue(cell);
                        rowList.add(value);
                    }
                    contentList.add(rowList);
                }
                LONGiExcelData excelData = new LONGiExcelData(sheetName, headerList, contentList);
                excelDateList.add(excelData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return excelDateList;
    }

    /**
     * @Author: Longi.liqitong
     * @Title: getWorkbook
     * @Description: 根据文件后缀，自适应上传文件的版本
     * @Param: [inStr, fileName]
     * @Return: org.apache.poi.ss.usermodel.Workbook
     * @Date: 2020-6-10 11:34
     */

    public static Workbook getWorkbook(InputStream inStr, String fileName) {
        Workbook workBook = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        try {
            if (excel2003L.equals(fileType)) {
                workBook = new HSSFWorkbook(inStr); // 2003-
            } else if (excel2007U.equals(fileType)) {
                workBook = new XSSFWorkbook(inStr); // 2007+
            } else {
                throw new Exception("解析的文件格式有误！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workBook;
    }

    public static boolean isRowEmpty(Row row) {
        boolean isEmpty = false;
        int firstCellNum = row.getFirstCellNum();
        if(firstCellNum == -1) {
            return true;
        }
        for (int columnIndex = firstCellNum; columnIndex < 1; columnIndex++) {
            Cell cell = row.getCell(columnIndex);
            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    /**
     * @Author: Longi.liqitong
     * @Title: getCellValue
     * @Description: 对表格中数值进行格式化
     * @Param: [cell]
     * @Return: java.lang.Object
     * @Date: 2020-6-10 11:35
     */
    public static String getCellValue(Cell cell) {
        String value = null;
        DecimalFormat df = new DecimalFormat("0"); // 格式化字符类型的数字
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"); // 日期格式化
        DecimalFormat df2 = new DecimalFormat("0"); // 格式化数字
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            value = cell.getRichStringCellValue().getString();
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                value = df.format(cell.getNumericCellValue());
            } else if ("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
                value = sdf.format(cell.getDateCellValue());
            } else {
                value = df2.format(cell.getNumericCellValue());
            }
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            value = String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            value = cell.getStringCellValue().toString();
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            value = "";
        }
        return value;
    }

}