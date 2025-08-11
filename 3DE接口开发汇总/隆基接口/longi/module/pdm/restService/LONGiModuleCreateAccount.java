package longi.module.pdm.restService;

import javax.ws.rs.ApplicationPath;

import com.dassault_systemes.platform.restServices.ModelerBase;


@ApplicationPath("/pdmservice/account")
public class LONGiModuleCreateAccount extends ModelerBase {

	@Override
	public Class<?>[] getServices() {
		return new Class<?>[] {
			LONGiModuleCreateAccountBase.class
			};
	}

}
