package longi.mybatis.mapper.impl;

import java.util.List;
import java.util.Map;

import longi.mybatis.bean.ModelAccess;
import longi.mybatis.bean.ModelUserAccess;
import longi.mybatis.mapper.BaseMapper;
import longi.mybatis.mapper.ModelUserAccessMapper;
import org.apache.ibatis.session.SqlSession;



/** 
* @ClassName: ModelUserAccessMapperImpl
* @Description: 用户权限信息实现类
* @author: Longi.suwei
* @date: 2023年10月19日 下午7:23:10
*/
public class ModelUserAccessMapperImpl extends BaseMapper implements ModelUserAccessMapper {

	/**
	 * 配置文件路径
	 */
	String path = "mybatis-config.xml";
	
	

	/**
	* @Title: getAllCounts
	* @Description: 获取所有权限的数量
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#getAllCounts()
	*/
	@Override
	public int getAllCounts() {
		// TODO Auto-generated method stub
		int result = 0;
		SqlSession session = null;
		try {
			session = getSession(path);
			result = session.selectOne("com.longi.mapper.ModelUserAccessMapper.getAllCounts");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return result;
	}

	

	/**
	* @Title: showAll
	* @Description: 获取单个用户的信息
	* @param userName
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#showAll(java.lang.String)
	*/
	@Override
	public List<ModelUserAccess> showAll(String userName) {
		// TODO Auto-generated method stub
		List<ModelUserAccess> meetingList = null;
		SqlSession session = null;
		try {
			session = getSession(path);
			meetingList = session.selectList("com.longi.mapper.ModelUserAccessMapper.showAll");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return meetingList;
	}

	

	/**
	* @Title: addMeeting
	* @Description: 增加一个用户权限数据
	* @param userAccess
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#addMeeting(longi.mybatis.bean.ModelUserAccess)
	*/
	@Override
	public int addMeeting(ModelUserAccess userAccess) {
		// TODO Auto-generated method stub
		int result = 0;
		SqlSession session = null;
		try {
			session = getSession(path);
			result = session.insert("com.longi.mapper.ModelUserAccessMapper.addMeeting");
			session.commit();
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return result;
	}



	/**
	* @Title: showAllModelAccess
	* @Description: 获取所有的模型权限清单
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#showAllModelAccess()
	*/
	@Override
	public List<ModelAccess> showAllAccess() {
		// TODO Auto-generated method stub
		List<ModelAccess> accessList = null;
		SqlSession session = null;
		try {
			session = getSession(path);
			accessList = session.selectList("com.longi.mapper.ModelUserAccessMapper.showAllAccess");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			session.close();
		}
		return accessList;
	}
	
	@Override
	public int addAccessProduct(ModelAccess userAccess) {
		// TODO Auto-generated method stub
		int result = 0;
		SqlSession session = null;
		try {
			session = getSession(path);
			result = session.insert("com.longi.mapper.ModelUserAccessMapper.addAccessProduct");
			session.commit();
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return result;
	}



	@Override
	public int addUserAccess(ModelUserAccess userAccess) {
		// TODO Auto-generated method stub
		int result = 0;
		SqlSession session = null;
		try {
			session = getSession(path);
			result = session.insert("com.longi.mapper.ModelUserAccessMapper.addUserAccess");
			session.commit();
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return result;
	}
	
	/**
	* @Title: showAllModelAccess
	* @Description: 获取所有的模型权限清单
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#showAllModelAccess()
	*/
	@Override
	public List<ModelUserAccess> getAllModelUserAccessWithModelId(String ModelId) {
		// TODO Auto-generated method stub
		List<ModelUserAccess> accessList = null;
		SqlSession session = null;
		try {
			session = getSession(path);
			accessList = session.selectList("com.longi.mapper.ModelUserAccessMapper.showAllAccess");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			session.close();
		}
		return accessList;
	}



	/**
	* @Title: removePrdUser
	* @Description: 删除产品用户权限
	* @param prdId
	* @param string
	* @return
	* @see longi.mybatis.mapper.ModelUserAccessMapper#removePrdUser(java.lang.String, java.lang.String)
	*/
	@Override
	public int removePrdUser(Map paramMap) {
		// TODO Auto-generated method stub
		int result = 0;
		SqlSession session = null;
		try {
			session = getSession(path);
			result = session.delete("com.longi.mapper.ModelUserAccessMapper.removePrdUser");
			session.commit();
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		}finally{
			session.close();
		}
		
		return result;
	}

}