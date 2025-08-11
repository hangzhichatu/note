package longi.common.restService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

// 配置路径
@ApplicationPath("/resources/dataExchange")

// 继承3de类
public class CHIDIDataExchangeService extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                // 引用实现类
        	CHIDIDataExchangeServiceBase.class
        };
    }
}
