package longi.module.pdm.restService;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/dataToPDM")
public class HistoryPartToPDMWS extends RestService {
    private static final XLogger LOGGER = XLoggerFactory.getXLogger(HistoryPartToPDMWS.class);

    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response excution(String type, String name,String owner, String param)  {
        LOGGER.info(param);
        Context context = null;
        try {
            context = ContextUtil.getAnonymousContext();
            ContextUtil.pushContext(context);
            Map map = new HashMap<>();
            map.put("type", type);
            map.put("name", name);
            map.put("owner", owner);
            map.put("param", param);
            String[] args = JPO.packArgs(map);
            String jpo = "LONGiModuleMaterialCreate";
            String method = "createMaterial";
            String result = JPO.invoke(context, jpo, null, method, args, String.class);
            LOGGER.exit();
            return Response.ok(result).cacheControl(CacheControl.valueOf("no-cache")).build();
        } catch (MatrixException e) {
            e.printStackTrace();
            LOGGER.catching(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                ContextUtil.popContext(context);
            } catch (FrameworkException e) {
            }
        }

    }


}
