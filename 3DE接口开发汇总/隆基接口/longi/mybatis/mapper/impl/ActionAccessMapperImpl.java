package longi.mybatis.mapper.impl;

import longi.mybatis.bean.ActionAccess;
import longi.mybatis.mapper.ActionAccessMapper;
import longi.mybatis.mapper.BaseMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class ActionAccessMapperImpl extends BaseMapper implements ActionAccessMapper {

    /**
     * 配置文件路径
     */
    String path = "mybatis-config.xml";

    @Override
    public List<ActionAccess> getActionAccessList() {
        List<ActionAccess> actionAccessList = null;
        SqlSession session = null;
        try {
            session = getSession(path);
            actionAccessList = session.selectList("com.longi.mapper.ActionAccessMapper.getActionAccessList");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return actionAccessList;
    }

    @Override
    public int addActionAccess(ActionAccess actionAccess) {
        int iResult = 0;
        SqlSession session = null;
        try {
            session = getSession(path);
            iResult = session.insert("com.longi.mapper.ActionAccessMapper.addActionAccess");
            session.commit();
        } catch (Exception e) {
            session.rollback();
            e.printStackTrace();
        }finally{
            session.close();
        }
        return iResult;
    }
}
