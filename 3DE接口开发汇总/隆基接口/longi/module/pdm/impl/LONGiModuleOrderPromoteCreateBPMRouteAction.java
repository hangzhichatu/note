package longi.module.pdm.impl;

import cn.net.drm.edi.client.DrmAgent;
import cn.net.drm.edi.client.DrmAgentInf;
import com.alibaba.fastjson.JSON;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralUtil;

import longi.module.pdm.client.route.AttachmentForm;
import longi.module.pdm.client.route.KmReviewParamterForm;
import longi.module.pdm.constants.LONGiModuleBPMConfig;
import longi.module.pdm.constants.LONGiModuleConfig;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.model.LONGiModuleBPMRouteFlowParamBean;
import longi.module.pdm.model.LONGiModuleKmReviewParamterFormDocBean;
import longi.module.pdm.model.LONGiModuleKmReviewParamterFormDocInternalBean;
import longi.module.pdm.model.LONGiModuleTargetBean;
import longi.module.pdm.service.LONGiModuleBPMInteService;
import longi.module.pdm.util.LONGiModuleEnoviaFileUtil;
import longi.module.pdm.util.LONGiModuleFastJsonUtil;
import longi.module.pdm.util.LONGiModuleFileDRMUtil;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName: InboxTaskToDoAction
 * @Description: TODO 创建BPM流程
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:15:19 PM
 */
public class LONGiModuleOrderPromoteCreateBPMRouteAction implements LONGiModuleOASendToDoInterface {
    private final String sep = File.separator;
    private DrmAgentInf drmClient;
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleOrderPromoteCreateBPMRouteAction.class);

    @Override
    public int sendToDo(Context context, Map attrMap) throws Exception {
        // TODO Auto-generated method stub
        int success = 0;
        logger.info("开始同步BPM流程，输入参数 attrMap：" + attrMap);
        String id = attrMap.get("objId").toString();
        DomainObject order = null;
        Map attributeMap = new HashMap();
        String LONGiModuleProjectDocumentControlledType = "";
        //审批答复意见
        String description = "";
        try {
            order = DomainObject.newInstance(context, id);
            attributeMap = order.getAttributeMap(context, true);
            LONGiModuleProjectDocumentControlledType = attributeMap.get("LONGiModuleProjectDocumentControlledType").toString();
            description = order.getDescription(context);

            if (!(LONGiModuleProjectDocumentControlledType.equals("TempOutControlled") || LONGiModuleProjectDocumentControlledType.equals("FormalControlled"))) {
                logger.info("内部临时受控流程，跳过BPM流程创建");
                return 0;
            }
        } catch (MatrixException e) {
            logger.error("查询受控单程序异常：" + e.toString());
            success = 1;
            throw new MatrixException(e);
//            e.printStackTrace();
        }
        
        // 流程表单
        LONGiModuleKmReviewParamterFormDocInternalBean internalFormBean = new LONGiModuleKmReviewParamterFormDocInternalBean();
        LONGiModuleBPMInteService bpm = new LONGiModuleBPMInteService();
//        String id = service.addReview(bpm.createForm());
        KmReviewParamterForm form = new KmReviewParamterForm();
        // 文档模板id
        form.setFdTemplateId(LONGiModuleBPMConfig.FD_TEMPLATEID);
        // 文档标题
        String title = attributeMap.get("Title").toString();
        if(UIUtil.isNotNullAndNotEmpty(title)) {
        	title = title.replaceAll("(\\r\\n|\\n|\\n\\r|\\r)","");
        }
        form.setDocSubject(title);

        LONGiModuleTargetBean creatorBean = new LONGiModuleTargetBean();
        try {
            creatorBean.setLoginName(order.getOwner(context).getName());
//            creatorBean.setLoginName("jiaofk");

            // 流程发起人
            form.setDocCreator(LONGiModuleJacksonUtil.beanToJsonLongiOA(creatorBean));
            // 文档关键字
            form.setFdKeyword("[\"受控\",\"技术文件\",\"" + order.getName() + "\"]");
            System.out.println("受控单名称=" + order.getName());
            System.out.println("attributeMap=" + attributeMap);
            //        internalFormBean.setDept("隆基股份_组件事业部_产品研发中心_组件设计中心_研究四室");
            //LONGiModuleApplicationFormType Add | Revise
            internalFormBean.setFormType(attributeMap.get("LONGiModuleApplicationFormType").toString());
            //LONGiModuleNatureOfFile Temp Formal
            internalFormBean.setFormNature(attributeMap.get("LONGiModuleNatureOfFile").toString());
            internalFormBean.setTechFileType(attributeMap.get("LONGiModuleTechDocType").toString());

            //新建、修订 、废止目的和原因
            internalFormBean.setModifyType(attributeMap.get("LONGiModuleWillSolveProblem").toString());

            internalFormBean.setModifyReasonDesc(attributeMap.get("LONGiModuleControlledCause").toString());
            //LONGiModuleChangeBefore
            //LONGiModuleChangeAfter
            internalFormBean.setBeforeChangeDesc(attributeMap.get("LONGiModuleChangeBefore").toString());
            internalFormBean.setAfterChangeDesc(attributeMap.get("LONGiModuleChangeAfter").toString());
            internalFormBean.setRemarks("PDM系统与BPM集成生成流程");
        } catch (Exception e) {
            logger.error("构造internalFormBean程序异常：" + e.toString());
            success = 1;
            throw new Exception(e);
        }
        //没有属性
//        byte[] fileByte = file2bytes("D:\\suwei3\\Desktop\\TEMP\\test.pdf");
//        internalFormBean.setFiles(fileByte);
        //fd_336b39b04689cc  文件
        String relationshipPattern = "LONGiControlledOrderRelationship";
        String typePattern = "*";
        StringList objectSelects = new StringList();
        objectSelects.addElement("name");
        objectSelects.addElement("id");
        objectSelects.addElement("revision");
        objectSelects.addElement("owner");
        objectSelects.addElement("modified");
//        objectSelects.addElement("attribute[LONGiModuleProjectDocumentFormalControlledNumber]");
        StringList relationshipSelects = new StringList();
        relationshipSelects.addElement("attribute[LONGiModuleProjectDocumentTempInnerControlledNumber]");
        relationshipSelects.addElement("attribute[LONGiModuleProjectDocumentTempOutControlledNumber]");
        relationshipSelects.addElement("attribute[LONGiModuleProjectDocumentFormalControlledNumber]");
        boolean getTo = false;
        boolean getFrom = true;
        short recurseToLevel = 1;
        String objectWhere = "";
        String relationshipWhere = "";
        int limit = 0;

        try {

            System.out.println("加密开始初始化");
            drmClient = DrmAgent.getInstance();
            MapList fileList = order.getRelatedObjects(context, relationshipPattern, typePattern, objectSelects, relationshipSelects, getTo, getFrom, recurseToLevel, objectWhere, relationshipWhere, limit);
            logger.info("查询到关联的文档数据：" + fileList);
            long tempPath = new Date().getTime();
            String dir = LONGiModuleConfig.BPM_ROUTE_FILE_PATH + sep + context.getUser() + "_" + tempPath + sep + "original";
//            String dir = "D:\\suwei3\\Desktop\\TEMP\\admin_platform_1596521701342\\original";
            String decryptDir = LONGiModuleConfig.BPM_ROUTE_FILE_PATH + sep + context.getUser() + "_" + tempPath + sep + "decrypt";
//            String decryptDir = "D:\\suwei3\\Desktop\\TEMP\\admin_platform_1596521701342\\decrypt";
//            String dir = LONGiModuleConfig.BPM_ROUTE_FILE_PATH;
            StringList fileCodeList = new StringList();
            StringList fileRevList = new StringList();
            StringList fileOwnerList = new StringList();
            StringList fileEffectiveDateList = new StringList();
            StringList fileApproverList = new StringList();
            LONGiModuleKmReviewParamterFormDocBean formDocBean = new LONGiModuleKmReviewParamterFormDocBean(internalFormBean);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sysFt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//            List<AttachmentForm> attForms = createAllAtts();
            int indexTag = 1;
            Map map = LONGiModuleFastJsonUtil.jsonToMap(LONGiModuleFastJsonUtil.beanToJson(formDocBean));
            ArrayList<AttachmentForm> attFormsList = new ArrayList<AttachmentForm>();
            String fd_n10 = attributeMap.get("LONGiModuleFutureFlow").toString();
            String fd_n20 = attributeMap.get("LONGiModuleModProcessInfo").toString();
            fd_n10 = LONGiPersonUtil.transferNametoCompanyId(context, fd_n10);
            fd_n20 = LONGiPersonUtil.transferNametoCompanyId(context, fd_n20);
            map.put("fd_n10", fd_n10);
            map.put("fd_n20", fd_n20);
//            if(ProgramCentralUtil.isNotNullString(description)){
//                map.put("fdUsageContent", description);
//            }

            //37544.43048.12584.59512
            //to[LONGiModuleProduct2ProductDevelopmentDetail].from.id

            map.put("fd_edit_link", LONGiModuleConfig.CAS_NAVI_URL + id);
            //产品id,快速方案或者全新品的id
            String productId = order.getInfo(context,"to[LONGiModuleProduct2ProductDevelopmentDetail].from.id");
            //关联的产品类型
            String productType = order.getInfo(context,"to[LONGiModuleProduct2ProductDevelopmentDetail].from.type");
            //不是快速方案，则使用受控单id作为下载id
            logger.info("当前受控单所属产品类型："+productType);
            //LONGiModuleNonStandardProduct
            logger.info("当前受控单所属产品类型："+productType);
            if(!productType.equals("LONGiModuleNonStandardProduct")){
                productId = id;
            }
            map.put("fd_download_link", LONGiModuleConfig.CAS_NAVI_URL + productId);

            //预填写的正式编码
            /*String fcNumber = attributeMap.get("LONGiModuleProjectDocumentFormalControlledNumber").toString();
            StringList fcNumberList = new StringList();
            int fcNumberSize = fcNumberList.size();
            if (ProgramCentralUtil.isNotNullString(fcNumber)) {
                fcNumberList = FrameworkUtil.split(fcNumber, ",");
            }
            System.out.println("fcNumberList=" + fcNumberList);*/

            //decryptFileStream(Client,file,outFile);

            //上传附件
            try {
                String[] referenceFiles = LONGiModuleEnoviaFileUtil.checkoutVersionedDocumentOrderFiles(context, id, dir);
                logger.info("当前输出文件清单" + id + "查询到的文件：" + Arrays.toString(referenceFiles));
                //参考文档对象
//                MapList refDocMapList = order.getRelatedObjects(context, "Reference Document", "*", objectSelects, relationshipSelects, getTo, getFrom, recurseToLevel, objectWhere, relationshipWhere, limit);
                StringList refDocIds = order.getInfoList(context,"from[Reference Document].to.id");
                //汇总所有参考文档文件名
                StringList refFileList = new StringList();

                //合并受控单附件的文档和参考文档内容
                if(Objects.isNull(referenceFiles) || referenceFiles==null || referenceFiles.length==0 || referenceFiles[0].equals("")){
                    logger.info("当前输出文件清单为空" + id);
                }else {
                    List list = new ArrayList(Arrays.asList(referenceFiles));
                    logger.info("输出文件清单" + id + "转List：" + list.toString());
                    refFileList.addAll(list);
                }

                

                for (Iterator<String> iterator = refDocIds.iterator(); iterator.hasNext(); ) {
                    String refId =  iterator.next();
                    String[] tempFiles = LONGiModuleEnoviaFileUtil.checkoutVersionedDocumentOrderFiles(context, refId, dir);
                    if(Objects.nonNull(tempFiles) && tempFiles.length>0){
                        refFileList.addAll(tempFiles);
                    }
                }
                //refDocIds.addAll(list);
                //logger.info("合并后的总附件文件" + id + "查询到的文件：" + refDocIds);


                if (Objects.nonNull(refFileList) && refFileList.size() > 0) {
                    for (String fileString : refFileList) {
                        if (ProgramCentralUtil.isNullString(fileString)) {
                            continue;
                        }
                        File originalFile = new File(dir + sep + fileString);
                        LONGiModuleFileDRMUtil.decryptInputStream(drmClient, originalFile, decryptDir, fileString);
                        logger.info("解密文件成功，解密目录：" + decryptDir + sep + fileString);
                        AttachmentForm tempForm = createAtt(decryptDir, fileString);
//                    AttachmentForm tempForm = createAtt(dir, fileString);
                        tempForm.setFdKey("fd_336b39b04689cc");
//                    tempForm.setFdKey("fd_336b39b04689cc");
                        attFormsList.add(tempForm);
                        //attForms[indexTag-1] = ;
//                    attForms.add(tempForm);

                        //关闭解密程序实例
                    /*
                    AttachmentForm tempForm = createAtt(dir, fileString);
//                    AttachmentForm tempForm = createAtt(dir, fileString);
                    tempForm.setFdKey("fd_attr"+fdLineNumber);
//                    tempForm.setFdKey("fd_336b39b04689cc");
                    attFormsList.add(tempForm);*/

                    }
                }


            } catch (Exception e) {
                // TODO: handle exception
                logger.error("参考文件下载和解密失败：" + e.toString());
                throw new Exception(e);
            }
            for (Iterator iterator = fileList.iterator(); iterator.hasNext(); ) {
                Map next = (Map) iterator.next();
                logger.info("当前文处理受控文档对象："+next);
                String documentId = next.get("id").toString();
                String revision = next.get("revision").toString();
                String owner = next.get("owner").toString();
                String modified = next.get("modified").toString();
                //String fcNumber = next.get("attribute[LONGiModuleProjectDocumentFormalControlledNumber]").toString();
                /*
                fileCodeList.addElement(fcNumber);
                fileRevList.addElement(revision);
                fileOwnerList.addElement(owner);
                try {
                    fileEffectiveDateList.addElement(ft.format(sysFt.parse(modified)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                fileApproverList.addElement(owner);
                 */
                String[] fileStrings = null;
//                String[] fileStrings = {"Test.docx"};
                try {
                    fileStrings = LONGiModuleEnoviaFileUtil.checkoutVersionedDocumentOrderFiles(context, documentId, dir);
                    logger.info("下载该文件文件：id="+documentId+"----文件："+fileStrings);
                } catch (Exception e) {
                    // TODO: handle exception
                    logger.error("受控文件下载失败:"+e.toString());
                    throw new Exception(e);
                }
                if(Objects.isNull(fileStrings) || fileStrings.length==0){
                    continue;
                }
                DomainObject documentObj = DomainObject.newInstance(context, documentId);
                BusinessObject bu = documentObj.getPreviousRevision(context);
                if (bu.exists(context)) {
                    DomainObject previousObj = DomainObject.newInstance(context, bu.getObjectId());
                    revision = previousObj.getInfo(context, "revision");
                } else {
                    revision = "";
                }
                //                String[] fileStrings = {"build.xml"};
                //fd_fileno  文件编号
                String userName = LONGiPersonUtil.getDocApprover(context, documentId);
                String fdLineNumber = Integer.toString(indexTag);
                if (indexTag > 9) {
                    fdLineNumber = "0" + fdLineNumber;
                }

                //


//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentTempOutControlledNumber].value =
//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentFormalControlledNumber].value =
//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentTempInnerControlledNumber].value = LR00002
//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentTempOutControlledNumber].value =
//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentFormalControlledNumber].value =
//                from[LONGiControlledOrderRelationship].attribute[LONGiModuleProjectDocumentTempInnerControlledNumber].value = LR00001


                //编码和文档数量不一致
                /*if (fcNumberSize < indexTag) {
                    map.put("fd_fileno" + fdLineNumber, fcNumberList.get(indexTag - 1));
                }*/
                //!(LONGiModuleProjectDocumentControlledType.equals("TempOutControlled") || LONGiModuleProjectDocumentControlledType.equals("FormalControlled"))
                if (LONGiModuleProjectDocumentControlledType.equals("TempOutControlled")) {
                    map.put("fd_fileno" + fdLineNumber, next.get("attribute[LONGiModuleProjectDocumentTempOutControlledNumber]").toString());
                } else if (LONGiModuleProjectDocumentControlledType.equals("FormalControlled")) {

                    map.put("fd_fileno" + fdLineNumber, next.get("attribute[LONGiModuleProjectDocumentFormalControlledNumber]").toString());
                } else {
                    map.put("fd_fileno" + fdLineNumber, "");
                }
                map.put("fd_versionno" + fdLineNumber, revision);
                map.put("fd_author" + fdLineNumber, LONGiPersonUtil.getChineseName(context, owner));
                map.put("fd_startdate" + fdLineNumber, ft.format(sysFt.parse(modified)));
                map.put("fd_approver" + fdLineNumber, userName);
                map.put("fd_isvalue" + fdLineNumber, "1");

                //fd_attr2 文件
                for (String fileString : fileStrings) {
                    //打开解密程序实例
                    if (ProgramCentralUtil.isNullString(fileString)) {
                        continue;
                    }
                    System.out.println("加密结束始化");
                    File originalFile = new File(dir + sep + fileString);
                    LONGiModuleFileDRMUtil.decryptInputStream(drmClient, originalFile, decryptDir, fileString);


                    AttachmentForm tempForm = createAtt(decryptDir, fileString);
//                    AttachmentForm tempForm = createAtt(dir, fileString);
                    tempForm.setFdKey("fd_attr" + fdLineNumber);
//                    tempForm.setFdKey("fd_336b39b04689cc");
                    attFormsList.add(tempForm);
                    //attForms[indexTag-1] = ;
//                    attForms.add(tempForm);

                    //关闭解密程序实例
                    /*
                    AttachmentForm tempForm = createAtt(dir, fileString);
//                    AttachmentForm tempForm = createAtt(dir, fileString);
                    tempForm.setFdKey("fd_attr"+fdLineNumber);
//                    tempForm.setFdKey("fd_336b39b04689cc");
                    attFormsList.add(tempForm);*/

                }
                indexTag++;
            }


            for (int i = indexTag; i < 11; i++) {
//                 = array[i];
                String fdLineNumber = Integer.toString(i);
                if (i > 9) {
                    fdLineNumber = "0" + fdLineNumber;
                }
                map.put("fd_fileno" + fdLineNumber, "");
                map.put("fd_versionno" + fdLineNumber, "");
                map.put("fd_author" + fdLineNumber, "");
                map.put("fd_startdate" + fdLineNumber, "");
                map.put("fd_approver" + fdLineNumber, "");
                map.put("fd_isvalue" + fdLineNumber, "2");
            }
            logger.info("报文属性值："+map);
//            form.getAttachmentForms().addAll(attForms);
            AttachmentForm[] attForms = (AttachmentForm[]) attFormsList.toArray(new AttachmentForm[attFormsList.size()]);
            form.setAttachmentForms(attForms);
            String formValues = JSON.toJSONString(map);
            form.setFormValues(formValues);
            //,"N20:16df2bd338e493d6be7c43e49509bfe4"
//            form.setFlowParam(flowParam);
            //bpm 流程id 不为空，执行更新操作
            String bpmRouteId = attributeMap.get("LONGiModuleRelBPMRouteId").toString();
            if (ProgramCentralUtil.isNullString(bpmRouteId)) {
                String returnId = LONGiModuleBPMInteService.createRoute(form);
                order.setAttributeValue(context, "LONGiModuleRelBPMRouteId", returnId);
                order.setAttributeValue(context, "LONGiModuleReviewInfo", LONGiModuleBPMConfig.BPM_URL + returnId);
            } else {
                //更新的form
//                LONGiModuleBPMRouteFlowParamBean routeFlowParam = new LONGiModuleBPMRouteFlowParamBean();
//                routeFlowParam.setAuditNote("受控单+" + order.getInfo(context,"name")+"已修订，请重新审批");
//                routeFlowParam.setOperationType("drafter_submit");
//                form.setFlowParam(LONGiModuleJacksonUtil.beanToJson(routeFlowParam));
                form.setFdId(bpmRouteId);
                String fdUpdateId = LONGiModuleBPMInteService.updateRoute(form);
                logger.info("更新受控单数据成功，返回BPM流程单fdId："+fdUpdateId);
                //提升的form
                KmReviewParamterForm approveForm = new KmReviewParamterForm();
                LONGiModuleBPMRouteFlowParamBean approveParam = new LONGiModuleBPMRouteFlowParamBean();
                approveParam.setAuditNote("受控单+" + order.getInfo(context, "name") + description);
                //drafter_submit
                approveParam.setOperationType("handler_pass");
                approveForm.setFlowParam(LONGiModuleJacksonUtil.beanToJson(approveParam));
                approveForm.setFdId(bpmRouteId);
                String fdApproveId = LONGiModuleBPMInteService.approveRoute(approveForm);
                logger.info("提升受控单数据成功，返回BPM流程单fdId："+fdApproveId);
            }

        } catch (Exception e) {
            logger.error("创建受控单，基础功能异常："+e.toString());
            e.printStackTrace();
            success = 1;
//            throw new FrameworkException(e.getMessage());
        } finally {
            drmClient.close();
            logger.info("成功关闭加密程序");
            return success;
        }
    }


//        map.put("fd_3897439d8696ac.fd_38974405db480c", fileAttr6);

    /*

            String formValues = JSON.toJSONString(map);

    //        String formValues = JSON.toJSONString(formDocBean);
            form.setFormValues(formValues);
            // 流程参数
            String flowParam = "{auditNode:\"请审核\", futureNodeId:\"N10\", changeNodeHandlers:[\"N10:16cba7cb29a453e30a1a09e4ef5919f6;15bc97653f219c7e864018644bfa498e\",\"N20:16cba7cb29a453e30a1a09e4ef5919f6\"]}";
            //,"N20:16df2bd338e493d6be7c43e49509bfe4"
            form.setFlowParam(flowParam);

            List<AttachmentForm> attForms = createAllAtts();
            form.getAttachmentForms().addAll(attForms);
            return 0;
        }

        /**
         * @Author: Longi.suwei
         * @Title: createAllAtts
         * @Description: TODO 创建附件列表
         * @param: @return
         * @param: @throws Exception
         * @date: Jun 29, 2020 5:49:38 PM
         */
    private List<AttachmentForm> createAllAtts() {

        List<AttachmentForm> attForms = new ArrayList<AttachmentForm>();

//        String fileName = "ucbug.com-NetDrive.zip";
//        AttachmentForm attForm01 = createAtt(fileName);
//        attForm01.setFdKey("fd_3897439d8696ac.0.fd_38974405db480c");
//        attForm01.setFdKey("fd_336b39b04689cc");
//        attForm01.setFdKey("fd_336b39b04689cc");
//        File file01 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
//        DataHandler dataHandler01 = new DataHandler(new FileDataSource(file01));
//        attForm01.setFdAttachment(dataHandler01);
//        fileName = "commons-collections-3.2.2.jar";
//        AttachmentForm attForm02 = createAtt(fileName);
//        attForm02.setFdKey("fd_336b39b04689cc");
//        File file02 = new File("D:\\suwei3\\Desktop\\TEMP\\" + fileName);
//        DataHandler dataHandler02 = new DataHandler(new FileDataSource(file02));
//        attForm02.setFdAttachment(dataHandler02);
//        attForms.add(attForm01);
//        attForms.add(attForm02);

        return attForms;
//        return null;
    }

    /**
     * @Author: Longi.suwei
     * @Title: createAtt
     * @Description: TODO 创建附件对象
     * @param: @param fileName
     * @param: @return
     * @param: @throws Exception
     * @date: Jun 29, 2020 5:49:45 PM
     */
    AttachmentForm createAtt(String dir, String fileName) throws Exception {
        AttachmentForm attForm = new AttachmentForm();
        attForm.setFdFileName(fileName);
        // 设置附件关键字，表单模式下为附件控件的id
//        File file = new File(dir + sep + fileName);
//        DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        try {

            byte[] data = file2bytes(dir + sep + fileName);
            attForm.setFdAttachment(data);
        } catch (Exception e) {
            logger.error("BPM上传文件设置失败："+fileName+sep+dir+"\n"+e.toString());
            throw new Exception(e);
        }
        return attForm;
    }

    /**
     * 将文件转换为字节编码
     */
    byte[] file2bytes(String fileName) throws Exception {
        InputStream in = new FileInputStream(fileName);
        byte[] data = new byte[in.available()];

        try {
            in.read(data);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }

        return data;
    }

}
