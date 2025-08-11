package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

/**
 * @ClassName LGiM_SharingPlatform
 * @Description
 * @Author Admin
 * @Date 2022/12/8
 **/
@ApplicationPath("/pdmservice/sp")
public class LGiM_SharingPlatform extends ModelerBase {

    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                LGiM_SharingPlatformBase.class
        };
    }
}



