package com.tipcrm.web.publicapi;

import com.tipcrm.bo.MenuPermissionBo;
import com.tipcrm.constant.Constants;
import com.tipcrm.service.PermissionService;
import com.tipcrm.service.WebContext;
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

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/public/api")
@Api
@RequiresAuthentication
public class PermissionApi {

    @Autowired
    private WebContext webContext;

    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "permission/me", method = RequestMethod.GET)
    public JsonEntity<List<MenuPermissionBo>> myPermissions() {
        Integer userId = webContext.getCurrentUserId();
        return ResponseHelper.createInstance(permissionService.getMenuPermissionsByUserId(userId));
    }

    @RequestMapping(value = "permission/role/{roleId}", method = RequestMethod.GET)
    public JsonEntity<List<MenuPermissionBo>> getRolePermissions(@PathVariable(value = "roleId") Integer roleId) {
        return ResponseHelper.createInstance(permissionService.getPermissionsByRoleId(roleId));
    }

    @RequestMapping(value = "permission/role/{roleId}", method = RequestMethod.PUT)
    @RequiresPermissions(Constants.Permission.ROLE_UPDATE)
    public JsonEntity<String> updateRolePermissions(@PathVariable(value = "roleId") Integer roleId,
                                                    @RequestBody Set<Integer> permissionIds) {
        permissionService.updateRolePermissions(roleId, permissionIds);
        return ResponseHelper.createInstance(Constants.RequestResult.SUCCESS);
    }

    @RequestMapping(value = "permission/menu/{menuId}", method = RequestMethod.GET)
    public JsonEntity<List<String>> getMyPermissionByMenuId(@PathVariable Integer menuId) {
        return ResponseHelper.createInstance(permissionService.getMyPermission(menuId));
    }

}
