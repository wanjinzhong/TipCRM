package com.tipcrm.service;
import java.util.List;

import com.tipcrm.bo.CreateUserBo;
import com.tipcrm.bo.LoginBo;
import com.tipcrm.bo.QueryRequestBo;
import com.tipcrm.bo.QueryResultBo;
import com.tipcrm.bo.UserBasicBo;
import com.tipcrm.bo.UserBo;
import com.tipcrm.bo.UserExtBo;
import com.tipcrm.dao.entity.User;

public interface UserService {

    // String regist(RegistUserBo registUserBo);

    Integer saveUser(CreateUserBo createUserBo);

    void login(LoginBo loginBo);

    UserExtBo getUserByUserId(Integer userId);

    Boolean isUserExist(Integer userId);

    Boolean isGeneralManager(Integer userId);

    List<User> findGeneralManager();

    User findSystemUser();

    void logout();

    List<UserBasicBo> findByName(String userName, Boolean includeDismiss);

    QueryResultBo<UserBo> queryUser(QueryRequestBo queryRequestBo);
}
