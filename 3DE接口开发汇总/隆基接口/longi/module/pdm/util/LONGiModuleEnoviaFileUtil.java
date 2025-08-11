package longi.module.pdm.util;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.BusinessObject;
import matrix.db.Context;

import java.io.File;
import java.util.Arrays;

import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;

import matrix.util.StringList;

public class LONGiModuleEnoviaFileUtil {
//    private static String format = "generic";

//    public static void main(String[] args) throws Exception {
//        Context context = LONGiModuleUserContext.getContext();
//
//        String dir = LONGiModuleConfig.BPM_ROUTE_FILE_PATH + "\\" + context.getUser() + "_" + new Date().getTime();
//        String documentId = "21847.44790.40614.50139";
//        String[] fileStrings = LONGiModuleEnoviaFileUtil.checkoutVersionedDocumentFiles(context, documentId, dir);
//        System.out.println(Arrays.toString(fileStrings));
//    }

    // get document files info
//    public static String getDocumentFilesInfo(Context context, String DocumentId) throws FrameworkException {
//        String mqlCommand = "print bus " + DocumentId + " select format[" + format + "].file dump |";
//        String rString = MqlUtil.mqlCommand(context, mqlCommand);
//        System.out.println("rString=" + rString);
//        return rString;
//    }
    public static String getDocumentFilesFormatFile(Context context, String DocumentId,String formatStr) throws FrameworkException {
        String mqlCommand = "print bus " + DocumentId + " select format[" + formatStr + "].file dump |";
        String rString = MqlUtil.mqlCommand(context, mqlCommand);
        System.out.println("rString=" + rString);
        return rString;
    }
    public static String getDocumentFilesFormat(Context context, String DocumentId) throws FrameworkException {
        String mqlCommand = "print bus " + DocumentId + " select format dump |";
        String rString = MqlUtil.mqlCommand(context, mqlCommand);
        System.out.println("rString=" + rString);
        return rString;
    }

    // get document files
    public static String[] getFiles(String mqlResult) {
        String[] files = mqlResult.split("\\|");
        for (int i = 0, n = files.length; i < n; i++) {
            files[i] = files[i].substring(files[i].indexOf(":") + 1);
        }
        return files;
    }

    // checkout document files
    public static void checkoutFiles(Context context, String busId, String[] files, String dir,String format) throws Exception {
        String file = null;
        try {
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            BusinessObject bus = new BusinessObject(busId);
            for (int i = 0, n = files.length; i < n; i++) {
                file = files[i];
                ContextUtil.pushContext(context);
                System.out.println("dir=" + dir);
                bus.checkoutFile(context, false, format, file, dir);
                ContextUtil.popContext(context);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        Context context = LONGiModuleUserContext.getContext();
        String[] aaaStrings = checkoutVersionedDocumentOrderFiles(context,"21847.44790.46515.11457","D:\\suwei3\\Desktop\\TEMP");
        System.out.println(Arrays.toString(aaaStrings));
    }
    public static String[] checkoutVersionedDocumentOrderFiles(Context context, String documentId, String dir) throws Exception {

        try {
            System.out.println("documentId=" + documentId);
            System.out.println(DomainObject.newInstance(context, documentId).getTypeName());
            DomainObject domainObject = DomainObject.newInstance(context, documentId);
            System.out.println("isKindOf=" + domainObject.isKindOf(context, "ACAD Drawing"));

            StringList fileListStrings = new StringList();
            if (domainObject.isKindOf(context, "ACAD Drawing")) {
//                String[] files = getFiles(getDocumentFilesInfo(context, documentId));

                String[] files = getFiles(getDocumentFilesFormatFile(context, documentId,"generic"));
                String[] files2 = getFiles(getDocumentFilesFormatFile(context, documentId,"drw"));

                for (int i = 0; i < files.length; i++) {
                    String fileString = files[i];
                    if(UIUtil.isNotNullAndNotEmpty(fileString) && (fileString.endsWith(".pdf") || fileString.endsWith(".btw"))){
                        fileListStrings.addElement(fileString);
                    }

//                    if (fileString.endsWith(".pdf")) {
//                        fileListStrings.addElement(fileString);
//                    } else {
//                        continue;
//                    }
                }
                boolean drwPDF = false;
                if(fileListStrings.size()==0){
                    for (int i = 0; i < files2.length; i++) {
                        String fileString = files2[i];
                        if(UIUtil.isNotNullAndNotEmpty(fileString) && fileString.endsWith(".pdf")){
                            fileListStrings.addElement(fileString);
                            drwPDF = true;
                        }else if(fileString.endsWith(".btw")){
                            fileListStrings.addElement(fileString);
                        }
                    }
                }
                if(files!=null && files.length>0 && !files[0].trim().equals("")){
                    checkoutFiles(context, documentId, files, dir,"generic");
                }else if(files2!=null && files2.length>0 && !files2[0].trim().equals("") && drwPDF){
                    checkoutFiles(context, documentId, files2, dir,"drw");
                }
            } else {
                String formats = getDocumentFilesFormat(context,documentId);
                String[] formatArray = getFiles(formats);
                for (int i = 0; i < formatArray.length; i++) {
                    String format = formatArray[i];
                    String[] files = getFiles(getDocumentFilesFormatFile(context, documentId,format));
                    for (int j = 0; j < files.length; j++) {
                        String file = files[j];
                        if(UIUtil.isNotNullAndNotEmpty(file)) {
                            fileListStrings.addElement(file);
                        }
                    }
                    if(files!=null && files.length>0 && !files[0].trim().equals("")) {
                        checkoutFiles(context, documentId, files, dir, format);
                    }
                }
            }
            String[] taskIds = (String[]) fileListStrings.toArray(new String[]{});
            if (taskIds == null || taskIds.length == 0) {
                return null;
            }else{
                return taskIds;
            }
        } catch (FrameworkException ex) {
            throw new FrameworkException(ex);
        }
    }
//    public static String[] checkoutVersionedDocumentOrderFiles(Context context, String documentId, String dir) throws Exception {
//
//        try {
//            System.out.println("documentId=" + documentId);
//            System.out.println(DomainObject.newInstance(context, documentId).getTypeName());
//            DomainObject domainObject = DomainObject.newInstance(context, documentId);
//            System.out.println("isKindOf=" + domainObject.isKindOf(context, "ACAD Drawing"));
//
//            if (domainObject.isKindOf(context, "ACAD Drawing")) {
//                String[] files = getFiles(getDocumentFilesInfo(context, documentId));
//
//                StringList fileListStrings = new StringList();
//                for (int i = 0; i < files.length; i++) {
//                    String fileString = files[i];
//                    if (fileString.endsWith(".pdf")) {
//                        fileListStrings.addElement(fileString);
//                    } else {
//                        continue;
//                    }
//                }
//                String[] taskIds = (String[]) fileListStrings.toArray(new String[]{});
//                if (taskIds == null || taskIds.length == 0) {
//                    return null;
//                }
//                checkoutFiles(context, documentId, taskIds, dir);
//                return taskIds;
//            } else {
//                format = "generic";
//                String[] files = getFiles(getDocumentFilesInfo(context, documentId));
//                if (files != null && files.length > 0) {
//                    checkoutFiles(context, documentId, files, dir);
//                }
//
//                return files;
//            }
//        } catch (FrameworkException ex) {
//            throw new FrameworkException(ex);
//        }
//    }
    public static String[] checkoutVersionedDocumentFiles(Context context, String documentId, String dir) throws Exception {
        try {
            String foramts = getDocumentFilesFormat(context,documentId);
            String[] formatArray = getFiles(foramts);
            StringList fileListStrings = new StringList();
            for (int i = 0; i < formatArray.length; i++) {
                String format = formatArray[i];
                String[] files = getFiles(getDocumentFilesFormatFile(context, documentId,format));
                for (int j = 0; j < files.length; j++) {
                    String file = files[j];
                    fileListStrings.addElement(file);
                }
                checkoutFiles(context, documentId, files, dir,format);
            }
            if (fileListStrings == null || fileListStrings.size() == 0) {
                return null;
            }

            return (String[]) fileListStrings.toArray(new String[]{});

        } catch (FrameworkException ex) {
            throw new FrameworkException(ex);
        }
    }
}
