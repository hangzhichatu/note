package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

/**
 * @ClassName LGiM_Manufacturer
 * @Description
 * @Author Admin
 * @Date 2024/02/01
 **/
@ApplicationPath("/pdmservice/manufacturer")
public class LGiM_Manufacturer extends ModelerBase {

    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                LGiM_ManufacturerBase.class
        };
    }
}



