package longi.common.service;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import longi.common.bean.bps.DpInfo;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Created by lg-xiehao3
 * @Date 2024/2/4 11:16
 */
public class IDepartFromPisService {
    /**
     * 查询部门及相关负责人
     * @param DpInfo
     * @return
     */
    public static DpInfo selectDepartMenment(DpInfo DpInfo) {
        List<DpInfo> departList = new ArrayList<DpInfo>();
        if (Objects.isNull(DpInfo.getId())) {
            String sql = "SELECT mu.real_name, ma.* FROM `msp_organization` ma, `msp_user` mu WHERE ma.org_level = 'L0' AND ma.manager_id = mu.login_name";
            try {
                departList = (List<DpInfo>) PISDBService.doQueryDept(sql);
                if (departList != null && departList.size() == 1) {
                    DpInfo = departList.get(0);
                    DpInfo.setState("open");
                    selectDepartMenment(DpInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sql = "SELECT mu.real_name, ma.* FROM `msp_organization` ma, `msp_user` mu WHERE parent_org_code = '" + FrameworkUtil.split(DpInfo.getId(), "|").get(1) + "' AND ma.manager_id = mu.login_name AND ma.enable_flag = '1'";
            try {
                departList = (List<DpInfo>) PISDBService.doQueryDept(sql);
                if (Objects.nonNull(departList) && departList != null && departList.size() > 0) {
                    for (Iterator iterator = departList.iterator(); iterator.hasNext(); ) {
                        DpInfo subDpInfo = (DpInfo) iterator.next();
                        if (UIUtil.isNotNullAndNotEmpty(subDpInfo.getId())) {
                            selectDepartMenment(subDpInfo);
                        }
                    }
                    DpInfo.addChildren(departList);
                    if (DpInfo.getState().equals("")) {
                        DpInfo.setState("closed");
                    }
                } else {
                    DpInfo.setState("open");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DpInfo;
    }

    public static void updateBPSDataToRedis(String key, String json) {
        Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        // 连接测试环境
//        Jedis jedis = new Jedis("10.0.88.100");
//        jedis.auth("plmadm79");
        jedis.set(key, json);// 存储数据
    }

    public static void main(String[] args)  {
        try {

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = dateFormat.format(currentDate);
            System.out.println(currentTime + "-----开始同步PIS组织架构数据------");

            PISDBService service = new PISDBService();
            service.getConnection();
            DpInfo DpInfoAll = new DpInfo();

            DpInfoAll = selectDepartMenment(DpInfoAll);
            List<DpInfo> DpInfoAllList = new ArrayList<DpInfo>();
            DpInfoAllList.add(DpInfoAll);
            String json = LONGiModuleJacksonUtil.beanToJson(DpInfoAllList);

            updateBPSDataToRedis("plm_bps_bu_json", json);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                PISDBService.releaseConn();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
