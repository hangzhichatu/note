package longi.bpm.service;
import matrix.db.Context;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

public interface LGiM_IntergrationService {
    /**
     * @Title:getJPOServiceTest
     * @author Zheng Hang
     * @Description: 测试用服务接口
     * @data 2024/8/26
     * @param context
     * @param json
     * @return
     * @throws Exception
     */
    public String getJPOServiceTest(Context context,String json)throws Exception;
}
