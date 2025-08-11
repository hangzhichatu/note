package longi.bom.BOMCreateProcessService;
import com.dassault_systemes.platform.restServices.ModelerBase;


import javax.ws.rs.ApplicationPath;

/**
 * @author wwy
 * @Description:
 * @date 2023/1/5 14:40
 */
@ApplicationPath("/LGiM/PLM")
public class LGiM_BOMCreateProcessService extends ModelerBase {

    @Override
    public Class<?>[] getServices() {
        // TODO Auto-generated method stub
        return new Class<?>[]{
                LGiM_BOMCreateProcessServiceBase.class
        };
    }
}
