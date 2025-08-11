package longi.mybatis.mapper;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * @Description //TODO 
 * 
 * @Param 
 * @return 
 * @Date 2023/6/15 15:23
 * @Author suwei3
 **/
public class BaseMapper {
/**
 * @Description //TODO 
 * 
 * @Param 
 * @param path
 * @return  * @return {@link SqlSession}
 * @Date 2023/6/15 15:23
 * @Author suwei3
 **/
    public SqlSession getSession(String path) {
        Reader reader = null;
        SqlSession session = null;
        try {
            reader = Resources.getResourceAsReader(path);
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            SqlSessionFactory factory = builder.build(reader);
            session = factory.openSession();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return session;
    }

}