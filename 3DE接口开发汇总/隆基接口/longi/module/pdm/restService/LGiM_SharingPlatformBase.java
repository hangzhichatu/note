package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.RestService;
import com.dassault_systemes.smaslm.matrix.common.json.JSONArray;
import com.dassault_systemes.smaslm.matrix.common.json.JSONObject;
import com.matrixone.apps.domain.util.ContextUtil;
import longi.common.bean.bps.LoginUserInfo;
import longi.common.interceptors.LoginInfoManager;
import longi.common.service.LIMSDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName LGiM_SharingPlatformBase
 * @Description
 * @Author Admin
 * @Date 2022/12/9
 **/
@Path("/setUserActions")
public class LGiM_SharingPlatformBase extends RestService {

    private final static Logger logger = LoggerFactory.getLogger("module_interface");

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response setUserActions(@Context HttpServletRequest request, @RequestBody String routeActionContext)
            throws Exception {
        System.out.println(">>>=======" + routeActionContext);
        logger.info("接收到OA创建用户接口请求，请求报文：" + routeActionContext);
        matrix.db.Context context = ContextUtil.getAnonymousContext();
        JSONObject jsonObj = new JSONObject(routeActionContext);
        String strProcessInstID = jsonObj.getString("ProcessInstID");
        JSONArray userArray = jsonObj.getJSONArray("User");
        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
        LoginInfoManager loginInfoManager = new LoginInfoManager();
        Connection conn = loginInfoManager.getConnection();
        LIMSDBService dbService = new LIMSDBService();
        dbService.getConnection();
        try {
            conn.setAutoCommit(false);
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            for (int i = 0; i < userArray.length(); i++) {
                JSONObject jsonObjUser = userArray.getJSONObject(i);
                String strPermission = jsonObjUser.getString("Permission");
                // 若OA流程勾选不同意开通则跳过 1|同意 2|不同意
                if ("2".equals(strPermission)) {
                    continue;
                }
                String strEmplId = jsonObjUser.getString("LoginName");
                String strUserName = jsonObjUser.getString("UserName");
                // 查询是否存在
                // String queryStr = "SELECT NAME FROM PLM_SHARING_USER WHERE OPR_ID = ?";
                String queryStr = "SELECT NAME FROM PLM_SHARING_USER WHERE EMPL_ID = ?";
                stmt = conn.prepareStatement(queryStr);
                stmt.setString(1, strEmplId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    resultSet.close();
                    stmt.close();
                    continue;
                }
                resultSet.close();
                stmt.close();
                // 新增人员
                queryStr = "SELECT P.NAME,P.OPRID,P.EMPLID,D.GPSLONGDESCR,P.POSITIONNBRDESCR,P.DEPTDESCR FROM LIMS.T_LIMS_HR_PERSON P LEFT JOIN LIMS.T_LIMS_HR_DEPT D ON P.DEPTID = D.DEPTID WHERE P.EMPLID ='"
                        + strEmplId + "'";
                List<LoginUserInfo> infoList = LIMSDBService.doQueryUser(queryStr);

                if (infoList.size() > 0) {
                    for (int j = 0; j < infoList.size(); j++) {
                        LoginUserInfo userInfo = infoList.get(j);
                        Date nowDate = new Date(new java.util.Date().getTime());
                        String insertStr = "INSERT INTO PLM_SHARING_USER(OPR_ID,NAME,EMPL_ID,POSITION_NBR_DESCR,DEPT_DESCR,GPS_LONG_DESCR,CREATE_DATE) VALUES (?,?,?,?,?,?,?)";
                        stmt = conn.prepareStatement(insertStr);
                        stmt.setString(1, userInfo.getOprId());
                        stmt.setString(2, strUserName);
                        stmt.setString(3, userInfo.getEmplId());
                        stmt.setString(4, userInfo.getPositionNbrDescr());
                        stmt.setString(5, userInfo.getDeptDescr());
                        stmt.setString(6, userInfo.getGpsLongDescr());
                        stmt.setDate(7, nowDate);
                        stmt.addBatch();
                        stmt.executeBatch();
                        stmt.close();
                        // 增加权限
                        insertStr = "INSERT INTO PLM_SHARING_USER_ACTION(OPR_ID,ACTION_ID) VALUES (?,?)";
                        stmt = conn.prepareStatement(insertStr);
                        stmt.setString(1, userInfo.getOprId());
                        stmt.setString(2, "LGiM_ShareAdminProductBOMViewCMD");
                        stmt.addBatch();
                        stmt.executeBatch();
						stmt.close();
                        stmt = conn.prepareStatement(insertStr);
                        stmt.setString(1, userInfo.getOprId());
                        stmt.setString(2, "LGiM_ShareAdminBOMCompareCMD");
                        stmt.addBatch();
                        stmt.executeBatch();
                        stmt.close();
						stmt = conn.prepareStatement(insertStr);
                        stmt.setString(1, userInfo.getOprId());
                        stmt.setString(2, "LGiM_ShareAdminCodeSystemView");
                        stmt.addBatch();
                        stmt.executeBatch();
                        stmt.close();
						stmt = conn.prepareStatement(insertStr);
                        stmt.setString(1, userInfo.getOprId());
                        stmt.setString(2, "LGiM_ShareAdminPackingUtility");
                        stmt.addBatch();
                        stmt.executeBatch();
                        stmt.close();
                    }
                }
            }
            dbService.releaseConn();
            conn.commit();
            jObjBuilder.add("returnState", "0");
            jObjBuilder.add("message", "操作成功");

        } catch (SQLException throwables) {
            conn.rollback();
            jObjBuilder.add("returnState", "1");
            jObjBuilder.add("message", "OA流程:" + strProcessInstID + "创建用户发生异常,PDM系统内部错误,错误日志:" + throwables.toString());
            logger.error("接口处理异常：" + throwables.toString() + "" + "返回给OA数据：" + jObjBuilder.build().toString());

            return Response.status(500).entity(jObjBuilder.build().toString()).build();
        } finally {
            conn.close();
        }
        logger.info("返回给OA接口数据：" + jObjBuilder.toString());
        return Response.status(200).entity(jObjBuilder.build().toString()).build();
    }


//    public static void main(String[] args) throws MatrixException {
//        String routeActionContext = "";
//        JSONObject jsonObj = new JSONObject();
//        jsonObj.put("ProcessInstID","1");
//        JSONArray userArray = new JSONArray();
//        JSONObject jsonObj11 = new JSONObject();
//
//        jsonObj11.put("UserName","aaa");
//        jsonObj11.put("LoginName","bbb");
//        userArray.put(jsonObj11);
//
//        JSONObject jsonObj222 = new JSONObject();
//
//        jsonObj222.put("UserName","aaa");
//        jsonObj222.put("LoginName","bbb");
//        userArray.put(jsonObj222);
//
//        jsonObj.put("User",userArray);
//        System.out.println("jsonObj---------------------->"+jsonObj);
//
//    }
//
//
//
//    public void setUserActions( String routeActionContext)
//            throws Exception {
//        System.out.println(">>>======="+routeActionContext);
//        logger.info("接收到QA创建用户接口请求，请求报文：" + routeActionContext);
//        matrix.db.Context context = ContextUtil.getAnonymousContext();
//        JSONObject jsonObj = new JSONObject(routeActionContext);
//        String strProcessInstID=jsonObj.getString("ProcessInstID");
//
//        JSONArray userArray = jsonObj.getJSONArray("User");
//
//        JsonObjectBuilder jObjBuilder = Json.createObjectBuilder();
//
//        LoginInfoManager loginInfoManager =  new LoginInfoManager();
//
//        Connection  conn = loginInfoManager.getConnection();
//
//        try {
//
//            conn.setAutoCommit(false);
//
//
////            StringList loginUserList = FrameworkUtil.split(strUserNames, "|");
//            PreparedStatement stmt = null;
//            for (int i =0 ; i<userArray.length();i++) {
//                JSONObject jsonObjUser = userArray.getJSONObject(i);
//                String strUserNames = jsonObjUser.getString("UserName");
//                String strEmplId = jsonObjUser.getString("LoginName");
//                //新增人员
//                String insertStr = "INSERT INTO PLM_SHARING_USER(OPR_ID,NAME) VALUES (?,?)";
//                stmt = conn.prepareStatement(insertStr);
//                stmt.setString(1, strEmplId);
//                stmt.setString(2, strUserNames);
//                stmt.addBatch();
//                stmt.executeBatch();
//                stmt.close();
//                //增加权限
//                String sqlInsert = "INSERT INTO PLM_SHARING_USER_ACTION(OPR_ID,ACTION_ID) VALUES (?,?)";
//                stmt = conn.prepareStatement(sqlInsert);
//
//
//                stmt.setString(1, strEmplId);
//                stmt.setString(2, "LGiM_ShareAdminProductBOMViewCMD");
//                stmt.addBatch();
//                stmt.executeBatch();
//                stmt.close();
//            }
//            conn.commit();
//            jObjBuilder.add("returnState", "0");
//            jObjBuilder.add("message", "操作成功");
//
//        } catch (SQLException throwables) {
//            conn.rollback();
//            jObjBuilder.add("returnState", "1");
//            jObjBuilder.add("message", "OA流程:"+strProcessInstID+"创建用户发生异常,PDM系统内部错误,错误日志:" + throwables.toString());
//            logger.error("接口处理异常：" + throwables.toString() + "" + "返回给FMS数据：" + jObjBuilder.build().toString());
//
//        }finally {
//
//            conn.close();
//
//        }
//        logger.info("返回给OA接口数据：" + jObjBuilder.toString());
//    }

}