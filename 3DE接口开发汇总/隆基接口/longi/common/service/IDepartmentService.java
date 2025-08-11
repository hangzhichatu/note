package longi.common.service;

import longi.common.bean.bps.DpInfo;
import longi.common.bean.bps.UserInfo;
import longi.module.pdm.util.LONGiModuleJacksonUtil;
import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IDepartmentService {
	private final static Logger logger = LoggerFactory.getLogger(IDepartmentService.class);

	static int ii = 0;
	/**
	* @Author: Longi.suwei
	* @Title: selectDpInfoTrees
	* @Description: 查询部门和人
	* @param: @param DpInfo
	* @param: @return
	* @date: 2022年5月9日 下午1:44:25
	*/
	public static DpInfo selectDpInfoTrees(DpInfo DpInfo) {
//        return DpInfoMapper.selectDpInfoTrees(DpInfo);
		// 查询逻辑
		// 空，则查询第一层

		List<DpInfo> departList = new ArrayList<DpInfo>();
		if (Objects.isNull(DpInfo.getId())) {
			String searchSql = "select * from LIMS.T_LIMS_HR_DEPT where LGIDEPTLEVEL='L0'";
			try {
				departList = (List<DpInfo>) LIMSDBService.doQueryDept(searchSql);
				if (departList != null && departList.size() == 1) {
					DpInfo = departList.get(0);
					selectDpInfoTrees(DpInfo);
				}
			} catch (SQLException e) {
				logger.error("sql执行结果输出：" + e.toString());
			}
		} else{

			//查询该部门下的人
			selectDpInfoPerson(DpInfo);
			// 查询该部门下的部门
			String searchSql = "select DEPTID,DESCR,LGIDEPTLEVEL from LIMS.T_LIMS_HR_DEPT where PARTDEPTIDCHN='" + DpInfo.getId() + "'  and LGICOSTCENTERID is not NULL";
			try {
				departList = (List<DpInfo>) LIMSDBService.doQueryDept(searchSql);
				if (Objects.nonNull(departList)) {
					for (Iterator iterator = departList.iterator(); iterator.hasNext();) {
						DpInfo subDpInfo = (DpInfo) iterator.next();
						if (UIUtil.isNotNullAndNotEmpty(subDpInfo.getId())) {
							selectDpInfoTrees(subDpInfo);
						}
					}
					DpInfo.addChildren(departList);
					DpInfo.setState("closed");
				}else {
					DpInfo.setState("open");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return DpInfo;
	}

	/**
	* @Author: Longi.suwei
	* @Title: selectJustDpInfoTrees
	* @Description: 仅查询部门
	* @param: @param DpInfo
	* @param: @return
	* @date: 2022年5月9日 下午1:44:55
	*/
	public static DpInfo selectJustDpInfoTrees(DpInfo DpInfo) {
//        return DpInfoMapper.selectDpInfoTrees(DpInfo);
		// 查询逻辑
		// 空，则查询第一层
		List<DpInfo> departList = new ArrayList<DpInfo>();
		if (Objects.isNull(DpInfo.getId())) {
			String searchSql = "select * from LIMS.T_LIMS_HR_DEPT where LGIDEPTLEVEL='L0'";
			try {
				departList = (List<DpInfo>) LIMSDBService.doQueryDept(searchSql);
				if (departList != null && departList.size() == 1) {
					DpInfo = departList.get(0);
					System.out.println(DpInfo.getText()+"设置open");
					DpInfo.setState("open");
					selectJustDpInfoTrees(DpInfo);
				}
			} catch (SQLException e) {
				logger.error("sql执行结果输出：" + e.toString());
			}
		} else{

			//查询该部门下的人
			//selectDpInfoPerson(DpInfo);
			// 查询该部门下的部门
			String searchSql = "select DEPTID,DESCR,LGIDEPTLEVEL,MANAGERNAME,MANAGERID from LIMS.T_LIMS_HR_DEPT where PARTDEPTIDCHN='" + FrameworkUtil.split(DpInfo.getId(), "|").get(1) + "' and LGICOSTCENTERID is not NULL";
			try {
				departList = (List<DpInfo>) LIMSDBService.doQueryDept(searchSql);
				if (Objects.nonNull(departList) && departList!=null && departList.size()>0) {
					for (Iterator iterator = departList.iterator(); iterator.hasNext();) {
						DpInfo subDpInfo = (DpInfo) iterator.next();
						if (UIUtil.isNotNullAndNotEmpty(subDpInfo.getId())) {
							

							selectJustDpInfoTrees(subDpInfo);
						}
					}
					DpInfo.addChildren(departList);
					if(DpInfo.getState().equals("")) {
						DpInfo.setState("closed");
					}
				}else {
					DpInfo.setState("open");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return DpInfo;
	}
	/**
	* @Author: Longi.suwei
	* @Title: selectDpInfoPerson
	* @Description: 暂时废弃
	* @param: @param DpInfo
	* @date: 2022年5月9日 下午4:57:46
	*/
	private static void selectDpInfoPerson(DpInfo DpInfo) {
		// TODO Auto-generated method stub
		String searchSql = "select EMPLID,NAME from(select a.EMPLID,a.NAME,row_number() over(partition by a.EMPLID order by EFFDT desc) sui from LIMS.T_LIMS_HR_PERSON a where a.DEPTID='" + DpInfo.getId() + "' and a.HRSTATUS='A') where sui=1";
		List<UserInfo> departList = new ArrayList<UserInfo>();
		try {
			departList = (List<UserInfo>) LIMSDBService.doQueryPerson(searchSql);
			if (Objects.nonNull(departList)) {
				/*
				for (Iterator iterator = departList.iterator(); iterator.hasNext();) {
					DpInfo subDpInfo = (DpInfo) iterator.next();
					
					if (UIUtil.isNotNullAndNotEmpty(subDpInfo.getId())) {
						try {
//							Thread.sleep(100);
							System.out.println(
									"查询人员：" + subDpInfo.getId() + ":" + subDpInfo.getDpInfodesc());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
					*/
//				DpInfo.setChildren(departList);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	* @Author: Longi.suwei
	* @Title: updateBPSDataToRedis
	* @Description: 更新数据至Redis
	* @param: @param key
	* @param: @param json
	* @date: 2022年5月9日 下午4:58:02
	*/
	public static void updateBPSDataToRedis(String key,String json) {
		Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
		jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
		// jedis.select(1);
		// System.out.println("Connection to server sucessfully");
		// 查看服务是否运行
		
		//System.out.println("json: " + json);
		jedis.set(key, json);// 存储数据
	}

	public static void main(String[] args) {
		try {

			LIMSDBService jdbcUtils = new LIMSDBService();
			jdbcUtils.getConnection();
			DpInfo DpInfoAll = new DpInfo();
			//查询所有
//			DpInfoAll = selectDpInfoTrees(DpInfoAll);
			
			//仅查部门
			DpInfoAll = selectJustDpInfoTrees(DpInfoAll);
			List<DpInfo> DpInfoAllList = new ArrayList<DpInfo>();
			DpInfoAllList.add(DpInfoAll);
			String json = LONGiModuleJacksonUtil.beanToJson(DpInfoAllList);

			updateBPSDataToRedis("plm_bps_bu_json",json);

//	    	System.out.println(LONGiModuleFastJsonUtil.beanToJson(selectDpInfoTrees(DpInfoAll)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				LIMSDBService.releaseConn();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
