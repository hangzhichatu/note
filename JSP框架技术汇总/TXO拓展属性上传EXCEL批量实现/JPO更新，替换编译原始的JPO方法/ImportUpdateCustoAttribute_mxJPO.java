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
//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dassault_systemes.PageUtilities.PageResourceConfiguration;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
// public class ImportUpdateCustoAttribute {
public class ImportUpdateCustoAttribute_mxJPO{

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

	public String ImportExcel(Context context, String[] args) {
		System.out.println("ImportExcel-------->");
		HashMap programMap;
		String excelFileName;
		String error = "OK";
		try {
			programMap = (HashMap) JPO.unpackArgs(args);
			excelFileName = (String) programMap.get("relFilePath");
			List<String[]> alist = readExcel(new File(excelFileName), 1);
			for (int n = 1; n < alist.size(); n++) {
				String[] str = (String[]) alist.get(n);
				String name = str[1];
				if (name.getBytes().length > 120) {
					return null;
				}
			}
			List<String[]> list = readExcel(new File(excelFileName), 1);
			error = parseExcel(context, list);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return error;
	}

	public String parseExcel(Context context, List<String[]> list) {
		String error = "OK";
		try {
				ContextUtil.pushContext(context);
				ContextUtil.startTransaction(context, true);
				System.out.println("list:"+list);
			for (int i = 1; i < list.size(); i++) {
				String str = "", str1 = "", str2 = "", str3 = "";
				String[] strs = (String[]) list.get(i);
				String str5 = strs[1]+strs[2]+".";
				str2 = strs[4];
				str3 = strs[5];
				if ("Interface".equals(str2)) {
					str1 = strs[1]+strs[3];
					str = "emxFramework.Interface." + str1+"="+str3;;
				} else if ("Attribute".equals(str2)) {
					str1 = strs[1] + strs[2]+"."+strs[1]+strs[3];
					str = "emxFramework.Attribute." + str1+"="+str3;
				}
				if (UIUtil.isNotNullAndNotEmpty(str)
						&& UIUtil.isNotNullAndNotEmpty(str3)) {
						 String pageExists = MqlUtil.mqlCommand(context, "list page $1","emxFrameworkStringResource_en.properties");
                    if (pageExists.isEmpty())
                    {
                        //TODO Create page as per locale
                        MqlUtil.mqlCommand(context, "add page $1 content $2 ", "emxFrameworkStringResource_en.properties", "abc");
                    }
					 String pageContent = MqlUtil.mqlCommand(context, "print page $1 select content dump",
                            "emxFrameworkStringResource_en.properties");
                    String updatePageContent = pageContent + System.lineSeparator() + str;

					//PageResourceConfiguration.UpdatePage(context, str, str3,"en");
					MqlUtil.mqlCommand(context, "modify page $1 content $2", "emxFrameworkStringResource_en.properties",updatePageContent);
				} else {
					error = error + str1 + ",";
				}
			}
			ContextUtil.commitTransaction(context);
				ContextUtil.popContext(context);
		} catch (Exception e) {
			ContextUtil.abortTransaction(context);
			e.printStackTrace();

		}
		return error;
	}

	public static String getStringValueFromCell(XSSFCell cell) {
		// System.out.println("getStringValueFromCell");
		SimpleDateFormat sFormat = new SimpleDateFormat("MM/dd/yyyy");
		DecimalFormat decimalFormat = new DecimalFormat("#.#");
		String cellValue = "";
		if (cell == null) {
			return cellValue;
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
			cellValue = cell.getStringCellValue();
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
			if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
				//double d = cell.getNumericCellValue();
				//Date date = HSSFDateUtil.getJavaDate(d);
				//cellValue = sFormat.format(date);
				LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
				cellValue = sFormat.format(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				cellValue = decimalFormat.format((cell.getNumericCellValue()));
			}
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
			cellValue = "";
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BOOLEAN ) {
			cellValue = String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.ERROR) {
			cellValue = "";
		} else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
			cellValue = cell.getCellFormula().toString();
		}
		// System.out.println("cellValue : " + cellValue);
		return cellValue;
	}

}