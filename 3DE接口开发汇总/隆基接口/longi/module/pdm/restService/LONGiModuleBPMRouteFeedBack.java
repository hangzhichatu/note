package longi.module.pdm.restService;


import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

//@ApplicationPath(ModelerBase.REST_BASE_PATH + "/LONGiModulePDM/restFul")
@ApplicationPath("/pdmservice/bpm")
public class LONGiModuleBPMRouteFeedBack extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[] {
                LONGiModuleBPMRouteFeedBackBase.class
        };
    }
}
