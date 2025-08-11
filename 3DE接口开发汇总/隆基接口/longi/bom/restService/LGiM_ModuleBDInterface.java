package longi.bom.restService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

// 配置路径
@ApplicationPath("/pdmservice/module/bd")

// 继承3de类
public class LGiM_ModuleBDInterface extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                // 引用实现类
                LGiM_ModuleBDInterfaceBase.class
        };
    }
}
