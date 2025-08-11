package longi.module.pdm.restService;


import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/pdmservice/dis")
public class LGiM_DISRouteFeedBack extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[] {
        	LGiM_DISRouteFeedBackBase.class
        };
    }
}
