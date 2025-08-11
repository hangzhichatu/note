package longi.mybatis.mapper;

import longi.mybatis.bean.ActionAccess;

import java.util.List;

public interface ActionAccessMapper {
    List<ActionAccess> getActionAccessList();

    int addActionAccess(ActionAccess actionAccess);
}