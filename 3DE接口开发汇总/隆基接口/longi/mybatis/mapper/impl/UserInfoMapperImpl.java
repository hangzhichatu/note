package longi.mybatis.mapper.impl;

import longi.mybatis.bean.UserInfo;
import longi.mybatis.mapper.BaseMapper;
import longi.mybatis.mapper.UserInfoMapper;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

/**
 * @ClassName: UserInfoMapperImpl
 * @Description: 用户信息实现类
 * @author: Longi.suwei
 * @date: 2023年10月19日 下午7:22:51
 */
public class UserInfoMapperImpl extends BaseMapper implements UserInfoMapper {

	/**
	 * 配置文件路径
	 */
	String path = "mybatis-config.xml";

	/**
	 * @Title: getUserInfo
	 * @Description: 获取一个用户的基础信息
	 * @param userName
	 * @return
	 * @see longi.mybatis.mapper.UserInfoMapper#getUserInfo(java.lang.String)
	 */
	@Override
	public UserInfo getUserInfo(String userName) {
		// TODO Auto-generated method stub
		UserInfo user = null;
		SqlSession session = null;
		try {
			session = getSession(path);
			System.out.println("session");
			user = session.selectOne("com.longi.mapper.UserInfoMapper.getUserInfo");
			System.out.println("imp user=" + user.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return user;
	}

	/**
	* @Title: getAllUserInfoList
	* @Description: 获取所有的3A用户信息
	* @return
	* @see longi.mybatis.mapper.UserInfoMapper#getAllUserInfoList()
	*/
	@Override
	public List<UserInfo> getAllUserInfoList() {
		// TODO Auto-generated method stub
		List<UserInfo> userList = null;
		SqlSession session = null;
		try {
			session = getSession(path);
			System.out.println("session");
			userList = session.selectList("com.longi.mapper.UserInfoMapper.getAllUserInfoList");
			System.out.println("search userList =" + userList.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return userList;
	}

}