package longi.mybatis.mapper;

import java.util.List;

import longi.mybatis.bean.UserInfo;


public interface UserInfoMapper {
    UserInfo getUserInfo(String userName);
    List<UserInfo> getAllUserInfoList();
}
