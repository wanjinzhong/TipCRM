package com.tipcrm.web.publicapi;
import java.util.List;
import java.util.Set;

import com.tipcrm.bo.RoleBo;
import com.tipcrm.bo.SaveRoleBo;
import com.tipcrm.constant.Constants;
import com.tipcrm.service.RoleService;
import com.tipcrm.web.util.JsonEntity;
import com.tipcrm.web.util.ResponseHelper;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/public/api")
@Api
@RequiresAuthentication
public class RoleApi {

    @Autowired
    private RoleService roleService;

    @RequestMapping(value = "roles", method = RequestMethod.GET)
    public JsonEntity<List<RoleBo>> getRoles() {
        return ResponseHelper.createInstance(roleService.getAllRoles());
    }

    @RequestMapping(value = "role", method = RequestMethod.POST)
    public JsonEntity<Integer> createNewRole(@RequestBody SaveRoleBo saveRoleBo) {
        return ResponseHelper.createInstance(roleService.saveRole(saveRoleBo));
    }

    @RequestMapping(value = "role", method = RequestMethod.PUT)
    public JsonEntity<String> updateRole(@RequestBody SaveRoleBo saveRoleBo) {
        roleService.updateRole(saveRoleBo);
        return ResponseHelper.createInstance(Constants.RequestResult.SUCCESS);
    }

    @RequestMapping(value = "/role/{roleId}", method = RequestMethod.DELETE)
    @RequiresPermissions(value = Constants.Permission.ROLE_DELETE)
    public JsonEntity<String> deleteRole(@PathVariable(value = "roleId") Integer roleId) {
        roleService.deleteRole(roleId);
        return ResponseHelper.createInstance(Constants.RequestResult.SUCCESS);
    }

    @RequestMapping(value = "/role/user/{userId}", method = RequestMethod.PUT)
    public JsonEntity<String> assignRoleToUser(@PathVariable(value = "userId") Integer userId,
                                               @RequestBody Set<Integer> roleIds) {
        roleService.assignRoleToUser(userId, roleIds);
        return ResponseHelper.createInstance(Constants.RequestResult.SUCCESS);
    }
}
