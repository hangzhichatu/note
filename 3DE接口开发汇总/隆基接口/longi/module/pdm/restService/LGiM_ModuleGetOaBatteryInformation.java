package longi.module.pdm.restService;

import javax.ws.rs.ApplicationPath;

import com.dassault_systemes.platform.restServices.ModelerBase;

// 配置路径
@ApplicationPath("/pdmservice/module/batterycode")

// 继承3de类
public class LGiM_ModuleGetOaBatteryInformation extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                // 引用实现类
                LGiM_ModuleGetOaBatteryInformationBase.class
        };
    }

}
