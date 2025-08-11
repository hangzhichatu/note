package longi.module.pdm.impl;

import com.alibaba.fastjson.JSON;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;

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
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import longi.module.pdm.util.LONGiPersonUtil;
import matrix.db.Context;
import matrix.db.JPO;
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
 * @ClassName: InboxTaskChangeOwnerAction
 * @Description: TODO 流程委托的实现类
 * @author: Longi.suwei
 * @date: Jun 12, 2020 5:16:03 PM
 */
public class LONGiModuleOrderDeleteRouteAbandonAction implements LONGiModuleOASendToDoInterface {
    private final String sep = File.separator;
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleOrderDeleteRouteAbandonAction.class);

    /**
     * @param context
     * @param attrMap
     * @return
     * @Title: sendToDo
     * @Description:
     * @see LONGiModuleOASendToDoInterface#sendToDo(Context, Map)
     */
    @Override
    public int sendToDo(Context context, Map attrMap) throws FrameworkException {
        // TODO Auto-generated method stub

        logger.info("开始执行受控单删除操作：" + attrMap);
        //id to name
        String name = attrMap.get("objId").toString();
        //type to LONGiModuleRelBPMRouteId
        String type = attrMap.get("type").toString();

        int resultState = 0;
        try {
            //,"N20:16df2bd338e493d6be7c43e49509bfe4"
            if (name.startsWith("CON")) {
                KmReviewParamterForm form = new KmReviewParamterForm();
                LONGiModuleBPMRouteFlowParamBean routeFlowParam = new LONGiModuleBPMRouteFlowParamBean();
                routeFlowParam.setAuditNote("受控单+" + name + "删除，流程废弃");
                routeFlowParam.setOperationType("drafter_abandon");
                form.setFlowParam(LONGiModuleJacksonUtil.beanToJson(routeFlowParam));
                //bpm 流程id 不为空，执行更新操作
                String bpmRouteId = type;
                form.setFdId(bpmRouteId);
                String result = LONGiModuleBPMInteService.approveRoute(form);
                logger.info("受控单关联的BPM流程已处理，返回结果：" + result);
            } else {
                logger.info("废弃的受控单数据是文件对象或者未关联BPM流程，受控单名称：" + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("废弃受控单过程异常，异常信息：" + e.toString());
            resultState = 1;
            throw new FrameworkException(e.getMessage());
        }finally {
            return resultState;
        }
    }
}
