package longi.bpm.BPMReturnProcessService;
import com.dassault_systemes.platform.restServices.ModelerBase;

import javax.ws.rs.ApplicationPath;
@ApplicationPath("/LGiMs/BPMToPLM")
public class LGiM_BPMReturnProcessService extends ModelerBase{
    public LGiM_BPMReturnProcessService() {
    }

    public Class<?>[] getServices() {
        return new Class[]{LGiM_BPMReturnProcessServiceBase.class};
    }
}
