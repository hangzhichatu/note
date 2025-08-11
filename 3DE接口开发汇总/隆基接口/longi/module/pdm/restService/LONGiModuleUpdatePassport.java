package longi.module.pdm.restService;

import javax.ws.rs.ApplicationPath;

import com.dassault_systemes.platform.restServices.ModelerBase;


@ApplicationPath("/pdmservice/password")
public class LONGiModuleUpdatePassport extends ModelerBase {

	@Override
	public Class<?>[] getServices() {
		return new Class<?>[] {
			LONGiModuleUpdatePasswordBase.class
			};
	}

}
