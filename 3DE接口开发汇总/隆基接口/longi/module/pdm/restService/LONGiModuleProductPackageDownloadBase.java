package longi.module.pdm.restService;

import com.alibaba.fastjson.JSONObject;
import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;

import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.util.LONGiModuleEnoviaFileUtil;
import longi.module.pdm.util.LONGiModuleZipCompressUtil;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


@Path("/packageFile")

public class LONGiModuleProductPackageDownloadBase extends RestService {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleProductPackageDownloadBase.class);
    private final String sep = File.separator;
    private String userPath = "";
    private String sourceFile = "";


    /**
     * Gets the connected user.
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response packageFiles(@Context HttpServletRequest request, @QueryParam("product") String product)
            throws Exception {
        logger.info("开始打包产品文件，产品id：" + product);
        declareServiceName("/pdm_nonAuthService/product/packageFile");
        boolean WSRequireSC = true;

        WSRequireSC = true;
        String securityContext = request.getHeader("SecurityContext");

        
        JSONObject jObjAll = new JSONObject();
        matrix.db.Context context = getAuthenticatedContext(request, WSRequireSC);
        long tempPath = new Date().getTime();
        userPath = LONGiModuleConfig.PRODUCT_DOWNLOAD_FILE_PATH + context.getUser() + "_" + tempPath + sep;
        sourceFile = userPath + "sourceFile" + sep;
        DomainObject productObj = DomainObject.newInstance(context, product);
        downloadPackageFile(context, productObj, sourceFile);

        String zipName = productObj.getInfo(context, "name") + "_" + productObj.getAttributeValue(context, "Title") + "_" + productObj.getInfo(context, "revision") + ".zip";

        String zipPath = userPath + "zipFile" + sep;
        File zipFile = new File(zipPath);
        if(!zipFile.exists()){
            zipFile.mkdir();
        }
        String zipFullName = zipPath + zipName;
        logger.info("打包的zip文件文件：" + zipFullName);
        System.out.println("sourceFile="+sourceFile);
        System.out.println("zipFullName="+zipFullName);
        LONGiModuleZipCompressUtil.toZip(sourceFile,zipFullName,true);
//        LONGiModuleZipCompressUtil zipCom = new LONGiModuleZipCompressUtil(zipFullName, sourceFile);
//        zipCom.zip();
        jObjAll.put("zipName", zipName);
        jObjAll.put("zipFullName", zipFullName);
        return Response.ok(jObjAll.toString()).cacheControl(CacheControl.valueOf("no-cache")).build();
    }

    private void downloadPackageFile(matrix.db.Context context, DomainObject productObj, String filePath) throws Exception {
        StringList busList = new StringList(DomainConstants.SELECT_ID);
        StringList relList = new StringList(DomainRelationship.SELECT_ID);
        busList.addElement(DomainConstants.SELECT_ID);
        int filter = 1;
        MapList rtnMaps = productObj.getRelatedObjects(context, DomainRelationship.RELATIONSHIP_VAULTED_DOCUMENTS, "Document,Specification,ACAD Drawing",
                busList, relList, false, true, (short) filter, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
//        MapList rtnMaps1 = productObj.getRelatedObjects(context, "Vaulted Objects",
//                "ACAD Drawing",
//                busList, relList, false, true, (short) filter, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
        MapList rtnMapsFolders = productObj.getRelatedObjects(context,
                "Data Vaults,Sub Vaults",
                "Workspace Vault",
                busList,
                null,
                false,
                true,
                (short) 1,
                null,
                null,
                0);

        String fileDirPath = "";
        

        String folderName = productObj.getInfo(context, "name");
        
        filePath = filePath + sep + folderName;
        File fileDir = new File(fileDirPath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        if (!rtnMaps.isEmpty()) {
            //文件下载
            for (Iterator iterator = rtnMaps.iterator(); iterator.hasNext(); ) {
                Map docMap = (Map) iterator.next();
                String docId = docMap.get("id").toString();
                LONGiModuleEnoviaFileUtil.checkoutVersionedDocumentOrderFiles(context, docId, filePath);
            }
        }
        if (!rtnMapsFolders.isEmpty()) {
            for (Iterator iterator = rtnMapsFolders.iterator(); iterator.hasNext(); ) {
                Map subFolder = (Map) iterator.next();
                String subId = subFolder.get("id").toString();
                DomainObject subObj = DomainObject.newInstance(context, subId);
                downloadPackageFile(context, subObj,filePath);
            }
        }
        
    }


}
