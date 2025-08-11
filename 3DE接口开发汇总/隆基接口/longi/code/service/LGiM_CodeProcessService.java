package longi.code.service;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/PLM/Code/Review")
public class LGiM_CodeProcessService extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                LGiM_CodeProcessServiceBase.class
        };
    }
}
