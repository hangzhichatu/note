package longi.module.pdm.dao;



import java.util.Map;

import matrix.db.Context;

public interface LONGiModuleOASendToDoInterface {
	int sendToDo(Context context, Map attrMap) throws Exception;
}
