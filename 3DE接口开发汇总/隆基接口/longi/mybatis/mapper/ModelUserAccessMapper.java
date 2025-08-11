package longi.mybatis.mapper;

import longi.mybatis.bean.ModelAccess;
import longi.mybatis.bean.ModelUserAccess;

import java.util.List;
import java.util.Map;

public interface ModelUserAccessMapper {
    int getAllCounts();
    List<ModelUserAccess> showAll(String userName);
    int addMeeting(ModelUserAccess userAccess);
    List<ModelAccess> showAllAccess();
    List<ModelUserAccess> getAllModelUserAccessWithModelId(String ModelId);
	int addAccessProduct(ModelAccess userAccess);
	int addUserAccess(ModelUserAccess userAccess);
	int removePrdUser(Map paramMap);
	
}
