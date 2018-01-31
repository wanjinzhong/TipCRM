package com.tipcrm.web.publicapi;

import com.tipcrm.service.UserService;
import com.tipcrm.web.start.ShiroProps;
import com.tipcrm.web.util.JsonEntity;
import com.tipcrm.web.util.ResponseHelper;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/public/api")
public class LoginApi {

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(LoginApi.class);

    @Autowired
    private ShiroProps shiroProps;

    @RequestMapping(value = "login", method = {RequestMethod.POST})
    public JsonEntity<String> login(String loginKey, String password) {
        try {
            userService.login(loginKey, password);
            return ResponseHelper.createInstance(shiroProps.getSuccessUrl());
        } catch (UnknownAccountException e) {
            return new JsonEntity<>(500, "账号不存在");
        } catch (IncorrectCredentialsException e) {
            return new JsonEntity<>(500, "密码不正确");
        } catch (Exception e) {
            return new JsonEntity<>(500, "登陆失败");
        }
    }

    @RequestMapping(value = "regist", method = {RequestMethod.POST})
    public JsonEntity<String> regist(String email, String password, String username, Boolean isManager) {
        try {
            String name = userService.regist(email, password, username, isManager);
            return ResponseHelper.createInstance(name);
        } catch (Exception e) {
            return new JsonEntity<>(500, e.getMessage());
        }
    }
}
