package longi.bom.ModulePackingService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/PLMPacking/Review")
public class LGiM_ModulePackingService extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                LGiM_ModulePackingServiceBase.class
        };
    }
}
