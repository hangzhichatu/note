package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/pdmservice/PDM")
public class PartToPDMApplication extends ModelerBase {
    @Override
    public Class<?>[] getServices() {
        return new Class<?>[]{
                ManufacturerToPDMWS.class
        };
    }
}
