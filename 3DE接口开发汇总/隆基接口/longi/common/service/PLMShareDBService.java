package longi.common.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import longi.mybatis.bean.ActionAccess;
import longi.mybatis.mapper.ActionAccessMapper;
import org.apache.ibatis.session.SqlSession;

import com.matrixone.apps.domain.util.MapList;

import longi.mybatis.bean.ModelAccess;
import longi.mybatis.bean.ModelUserAccess;
import longi.mybatis.bean.UserInfo;
import longi.mybatis.mapper.ModelUserAccessMapper;
import longi.mybatis.mapper.UserInfoMapper;
import longi.mybatis.util.MybatisUtils;

/**
 * @ClassName: PLMShareDBService
 * @Description: 共享平台数据库查询相关实现类，基于mybatis的数据库查询等
 * @author: Longi.suwei
 * @date: 2023年6月15日 下午4:58:28
 */
public class PLMShareDBService {
	static SqlSession sqlSession;

	public static void main(String[] args) {
		Map<String, Object> params = new HashMap<>();

		params.put("MODEL_ID", "30103.58115.59367.48971");
		List<String> names = new ArrayList<String>();
		names.add("lvjun");
		names.add("qiaoxj");
		params.put("USER_NAME", names);
		removePrdUser(params);
//		getAllUserInfoList();
//		UserInfo user = getUserInfo("suwei3");
//		System.out.println(getAllModelAccessList().toString());
//		List<ModelUserAccess> a = getAllModelUserWithModelId("30103.58115.59367.48971");
//		System.out.println(a);
//		ModelAccess access = new ModelAccess();
//		access.setMODEL_ID("1233");
//		access.setMODEL_NAME("LR5");
//		access.setMODEL_REVISION("G5");
//		access.setMODEL_SECRET("3A");
//		access.setMODEL_TYPE("");
//		access.setIS_ACTIVE("TRUE");
//		addAccessProduct(access);
//		System.out.println("user="+user.toString());
	}
	/**
	 * @Author: Longi.suwei
	 * @Title: getUserModelAccessStringList
	 * @Description: 获取单个用户所有有权限的3A模型权限
	 * @param: @param userName
	 * @param: @return
	 * @date: 2023年6月15日 下午5:03:48
	 */
	@SuppressWarnings("unchecked")
	public static MapList getUserModelAccessStringList(String userName) {
		MapList mapList = new MapList();
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			List<ModelUserAccess> lists = mapper.showAll(userName);
			for (Iterator<ModelUserAccess> iterator = lists.iterator(); iterator.hasNext();) {
				ModelUserAccess modelUserAccess = (ModelUserAccess) iterator.next();
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", modelUserAccess.getMODEL_ID());
				map.put("name", modelUserAccess.getMODEL_NAME());
				map.put("revision", modelUserAccess.getMODEL_REVISION());
				mapList.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			sqlSession.close();
		}
		return mapList;
	}


	/**
	 * @Author: Longi.suwei
	 * @Title: getUserModelAccessStringList
	 * @Description: 获取单个用户所有有权限的3A模型权限
	 * @param: @param userName
	 * @param: @return
	 * @date: 2023年6月15日 下午5:03:48
	 */
	@SuppressWarnings("unchecked")
	public static MapList getModelAccessList(String userName) {
		MapList mapList = new MapList();
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			List<ModelUserAccess> lists = mapper.showAll(userName);
			for (Iterator<ModelUserAccess> iterator = lists.iterator(); iterator.hasNext();) {
				ModelUserAccess modelUserAccess = (ModelUserAccess) iterator.next();
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", modelUserAccess.getMODEL_ID());
				map.put("name", modelUserAccess.getMODEL_NAME());
				map.put("revision", modelUserAccess.getMODEL_REVISION());
				mapList.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			sqlSession.close();
		}
		return mapList;
	}
	/**
	 * @Author: Longi.suwei
	 * @Title: getUserInfo
	 * @Description: 从产品BOM共享平台数据库中获取单个用户的信息
	 * @param: @param userName
	 * @param: @return
	 * @date: 2023年10月19日 下午8:05:12
	 */
	public static UserInfo getUserInfo(String userName) {
		UserInfo userInfo = null;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			UserInfoMapper mapper = sqlSession.getMapper(UserInfoMapper.class);
			userInfo = mapper.getUserInfo(userName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return userInfo;
	}


	/**
	 * @Author: Longi.suwei
	 * @Title: getAllUserInfoList
	 * @Description: 获取所有的用户信息表，用于3A权限设置
	 * @param: @return
	 * @date: 2023年11月18日 上午11:02:52
	 */
	public static List<UserInfo> getAllUserInfoList() {
		List<UserInfo> userInfoList = null;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			UserInfoMapper mapper = sqlSession.getMapper(UserInfoMapper.class);
			userInfoList = mapper.getAllUserInfoList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return userInfoList;
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: getAllUserInfoList
	 * @Description: 获取所有的用户信息表，用于3A权限设置
	 * @param: @return
	 * @date: 2023年11月18日 上午11:02:52
	 */
	public static List<ModelAccess> getAllModelAccessList() {
		List<ModelAccess> modelAccessList = null;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			modelAccessList = mapper.showAllAccess();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return modelAccessList;
	}



	/**
	 * @Author: Longi.suwei
	 * @Title: addAccessProduct
	 * @Description: 添加一个3A权限产品配置
	 * @param: @param userAccess
	 * @param: @return
	 * @date: 2023年11月22日 下午2:51:22
	 */
	public static int addAccessProduct(ModelAccess userAccess) {
		int status = 0;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			status = mapper.addAccessProduct(userAccess);
			sqlSession.commit();
		} catch (Exception e) {
			status = 1;
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return status;
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: addUserAccess
	 * @Description: 新建产品用户授权
	 * @param: @param userAccess
	 * @param: @return
	 * @date: 2023年12月5日 上午11:30:03
	 */
	public static int addUserAccess(ModelUserAccess userAccess) {
		int status = 0;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			status = mapper.addUserAccess(userAccess);
			sqlSession.commit();
		} catch (Exception e) {
			status = 1;
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return status;
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: addUserAccess
	 * @Description: 新建产品用户授权
	 * @param: @param userAccess
	 * @param: @return
	 * @date: 2023年12月5日 上午11:30:03
	 */
	public static List<ModelUserAccess> getAllModelUserWithModelId(String modelId) {
		List<ModelUserAccess> modelUserAccessList = null;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			modelUserAccessList = mapper.getAllModelUserAccessWithModelId(modelId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return modelUserAccessList;
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: getUserModelAccessStringList
	 * @Description: 获取单个用户所有有权限的3A模型权限
	 * @param: @param userName
	 * @param: @return
	 * @date: 2023年6月15日 下午5:03:48
	 */
	@SuppressWarnings("unchecked")
	public static MapList getProductModelAccessStringList(String userName) {
		MapList mapList = new MapList();
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			List<ModelUserAccess> lists = mapper.showAll(userName);
			for (Iterator<ModelUserAccess> iterator = lists.iterator(); iterator.hasNext();) {
				ModelUserAccess modelUserAccess = (ModelUserAccess) iterator.next();
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", modelUserAccess.getMODEL_ID());
				map.put("name", modelUserAccess.getMODEL_NAME());
				map.put("revision", modelUserAccess.getMODEL_REVISION());
				mapList.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			sqlSession.close();
		}
		return mapList;
	}
	/**
	 * @Author: Longi.suwei
	 * @Title: removePrdUser
	 * @Description: TODO
	 * @param: @param prdId
	 * @param: @param string
	 * @date: 2023年12月8日 下午3:55:37
	 */
	public static int removePrdUser(Map paramMap) {
		// TODO Auto-generated method stub
		int status = 0;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ModelUserAccessMapper mapper = sqlSession.getMapper(ModelUserAccessMapper.class);
			status = mapper.removePrdUser(paramMap);
			sqlSession.commit();
		} catch (Exception e) {
			status = 1;
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return status;
	}

	public static List<ActionAccess> getActionAccessList() {
		List<ActionAccess> actionAccessList = null;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ActionAccessMapper mapper = sqlSession.getMapper(ActionAccessMapper.class);
			actionAccessList = mapper.getActionAccessList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return actionAccessList;
	}

	public static int addActionAccess(ActionAccess actionAccess) {
		int status = 0;
		try {
			sqlSession = MybatisUtils.getSqlSession();
			ActionAccessMapper mapper = sqlSession.getMapper(ActionAccessMapper.class);
			status = mapper.addActionAccess(actionAccess);
			sqlSession.commit();
		} catch (Exception e) {
			status = 1;
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return status;
	}
}
