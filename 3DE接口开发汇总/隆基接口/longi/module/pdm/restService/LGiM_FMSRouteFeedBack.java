package longi.module.pdm.restService;


import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/pdmservice/fms")
public class LGiM_FMSRouteFeedBack extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[] {
        	LGiM_FMSRouteFeedBackBase.class
        };
    }
}
