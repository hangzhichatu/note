import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


// public class ImportNewCustoAttribute {
public class ImportNewCustoAttribute_mxJPO{

        private static final String VAULT = "eService Production";
        private static final String TASKESTIMATEDURATION = "1.0";

        public static List<String[]> readExcel(File excelFile, int rowNum)
                        throws IOException {
                List<String[]> list = new ArrayList<String[]>();
                XSSFWorkbook rwb = null;
                XSSFCell cell = null;

                InputStream stream = new FileInputStream(excelFile);
                rwb = new XSSFWorkbook(stream);
                XSSFSheet sheet = rwb.getSheetAt(0);
                for (int i = rowNum - 1; i <= sheet.getLastRowNum(); i++) {
                        XSSFRow row = sheet.getRow(i);
                        String[] str = new String[row.getLastCellNum()];
                        for (int j = 0; j < row.getLastCellNum(); j++) {
                                cell = row.getCell(j);
                                if (cell != null) {
                                        String str1 = getStringValueFromCell(cell);
                                        str[j] = str1;
                                }
                        }
                        list.add(str);
                }
                return list;
        }

        public MapList ImportExcel(Context context, String[] args) {
                System.out.println("ImportExcel-------->");
                HashMap programMap;
                String objectId;
                String excelFileName;
                MapList returnMapList = new MapList();
                try {
                        programMap = (HashMap) JPO.unpackArgs(args);
                        objectId = (String) programMap.get("objectId");
                        excelFileName = (String) programMap.get("relFilePath");
                        System.out.println("excelFileName======" + excelFileName);
                        List<String[]> alist = readExcel(new File(excelFileName), 1);
                        for (int n = 1; n < alist.size(); n++) {
                                String[] str = (String[]) alist.get(n);
                                String name = str[1];
                                if (name.getBytes().length > 120) {
                                        return null;
                                }
                        }
                        List<String[]> list = readExcel(new File(excelFileName), 1);
                        returnMapList = parseExcel(context, objectId, list);

                } catch (Exception e) {
                        returnMapList = null;
                        e.printStackTrace();
                }

                return returnMapList;
        }

        public MapList parseExcel(Context context, String objectId,
                        List<String[]> list) {
                MapList mapList = new MapList();
                try {
                        for (int i = 1; i < list.size(); i++) {
                                Map map = new HashMap();
                                String strName = "";
                                String strType = "";
                                String strUnit = "";
                                String strRangeValues = "";
                                String strDefaultValue = "";
                                String[] str = (String[]) list.get(i);
                                strName = str[2];
                                strType = str[3];
                                if (str.length > 4) {
                                        strUnit = str[4];
                                }
                                if (str.length > 5) {
                                        strRangeValues = str[5];
                                }
                                if (str.length > 6) {
                                        strDefaultValue = str[6];
                                }
                                map.put("name", strName);
                                map.put("type", strType);
                                map.put("unit", strUnit);
                                map.put("rangeValues", strRangeValues);
                                map.put("defaultValue", strDefaultValue);
                                mapList.add(map);
                        }
                } catch (Exception e) {
                        mapList = null;
                        e.printStackTrace();
                } finally {
                        return mapList;
                }
        }

        public static String getStringValueFromCell(XSSFCell cell) {
                // System.out.println("getStringValueFromCell");
                SimpleDateFormat sFormat = new SimpleDateFormat("MM/dd/yyyy");
                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                String cellValue = "";
                if (cell == null) {
                        return cellValue;
                } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {//XSSFCell.CELL_TYPE_STRING
                        cellValue = cell.getStringCellValue();
                } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {//XSSFCell.CELL_TYPE_NUMERIC
                        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                //double d = cell.getNumericCellValue();
                                //Date date = HSSFDateUtil.getJavaDate(d);
								LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
								
                                //cellValue = sFormat.format(date);
								cellValue = sFormat.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        } else {
                                cellValue = decimalFormat.format((cell.getNumericCellValue()));
                        }
                } else if (cell.getCellType() ==org.apache.poi.ss.usermodel.CellType.BLANK ) {//XSSFCell.CELL_TYPE_BLANK
                        cellValue = "";
                } else if (cell.getCellType() ==org.apache.poi.ss.usermodel.CellType.BOOLEAN ) {//XSSFCell.CELL_TYPE_BOOLEAN
                        cellValue = String.valueOf(cell.getBooleanCellValue());
                } else if (cell.getCellType() ==org.apache.poi.ss.usermodel.CellType.ERROR ) {//XSSFCell.CELL_TYPE_ERROR
                        cellValue = "";
                } else if (cell.getCellType() ==org.apache.poi.ss.usermodel.CellType.FORMULA ) {//XSSFCell.CELL_TYPE_FORMULA
                        cellValue = cell.getCellFormula().toString();
                }
                // System.out.println("cellValue : " + cellValue);
                return cellValue;
        }

}