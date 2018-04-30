package com.tipcrm.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tipcrm.bo.RoleBasicBo;
import com.tipcrm.bo.RoleBo;
import com.tipcrm.bo.SaveRoleBo;
import com.tipcrm.cache.PermissionCache;
import com.tipcrm.cache.RoleCache;
import com.tipcrm.dao.entity.Permission;
import com.tipcrm.dao.entity.Role;
import com.tipcrm.dao.entity.RolePermission;
import com.tipcrm.dao.entity.User;
import com.tipcrm.dao.entity.UserRole;
import com.tipcrm.dao.repository.RolePermissionRepository;
import com.tipcrm.dao.repository.RoleRepository;
import com.tipcrm.dao.repository.UserRepository;
import com.tipcrm.dao.repository.UserRoleRepository;
import com.tipcrm.exception.BizException;
import com.tipcrm.service.RoleService;
import com.tipcrm.service.WebContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private WebContext webContext;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public List<Role> getRolesByUserId(Integer userId) {
        List<Role> roles = RoleCache.getRoles(userId);
        if (CollectionUtils.isEmpty(roles)) {
            roles = userRoleRepository.findRoleByUserId(userId);
            RoleCache.addOrUpdateRoles(userId, roles);
        }
        return roles;
    }

    @Override
    public List<RoleBasicBo> getMyRoles() {
        return RoleBasicBo.toRoleBasicBos(getRolesByUserId(webContext.getCurrentUserId()));
    }

    @Override
    public Role findById(Integer roleId) {
        List<Role> roles = RoleCache.getAllRole();
        for (Role role : roles) {
            if (role.getId().equals(roleId)) {
                return role;
            }
        }
        return null;
    }

    @Override
    public Map<Integer, Set<Permission>> getAllRolePermissionMap() {
        List<Role> roles = roleRepository.findAll();
        Map<Integer, Set<Permission>> rolePermissions = new HashMap<>();
        for (Role role : roles) {
            Set<Permission> permissions = new HashSet<>();
            List<RolePermission> rolePermissionList = role.getRolePermissions();
            if (!CollectionUtils.isEmpty(rolePermissionList)) {
                permissions.addAll(rolePermissionList.stream().map(rp -> rp.getPermission()).collect(Collectors.toList()));
            }
            rolePermissions.put(role.getId(), permissions);
        }
        return rolePermissions;
    }

    @Override
    public List<RoleBo> getAllRoles() {
        List<Role> roles = RoleCache.getAllRole();
        return RoleBo.toRoleBos(roles);
    }

    @Override
    public Integer saveRole(SaveRoleBo saveRoleBo) {
        validateSaveRoleBo(saveRoleBo, true);
        User user = webContext.getCurrentUser();
        Date date = new Date();
        Role role = new Role();
        role.setName(saveRoleBo.getName());
        role.setEntryTime(date);
        role.setEntryUser(user);
        role.setEditable(true);
        roleRepository.save(role);
        RoleCache.addRole(role);
        PermissionCache.addOrUpdatePermissions(role.getId(), new HashSet<>());
        return role.getId();
    }

    private void validateSaveRoleBo(SaveRoleBo saveRoleBo, Boolean isSaveMethod) {
        if (!isSaveMethod && saveRoleBo.getId() == null) {
            throw new BizException("没有指定需要修改角色ID");
        }
        if (!isSaveMethod) {
            Role role = RoleCache.getRoleById(saveRoleBo.getId());
            if (role == null) {
                throw new BizException("角色不存在");
            } else if (!role.getEditable()) {
                throw new BizException("角色不可编辑");
            }
        }
        if (StringUtils.isBlank(saveRoleBo.getName())) {
            throw new BizException("角色名不能为空");
        }
        Role role = RoleCache.getRoleByName(saveRoleBo.getName());
        if (role != null && (isSaveMethod || !isSaveMethod && !role.getId().equals(saveRoleBo.getId()))) {
            throw new BizException("角色名已存在");
        }
    }

    @Override
    public void updateRole(SaveRoleBo saveRoleBo) {
        validateSaveRoleBo(saveRoleBo, false);
        Role role = RoleCache.getRoleById(saveRoleBo.getId());
        User user = webContext.getCurrentUser();
        Date date = new Date();
        role.setName(saveRoleBo.getName());
        role.setUpdateTime(date);
        role.setUpdateUser(user);
        roleRepository.save(role);
        RoleCache.updateRole(role);
    }

    @Override
    public void deleteRole(Integer roleId) {
        Role role = RoleCache.getRoleById(roleId);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        if (!role.getEditable()) {
            throw new BizException("该角色不可删除");
        }
        rolePermissionRepository.deleteByRoleId(roleId);
        roleRepository.delete(roleId);
        userRoleRepository.deleteByRoleId(roleId);
        PermissionCache.popPermissions(roleId);
        RoleCache.deleteRole(roleId);
    }

    @Override
    public void assignRoleToUser(Integer userId, Set<Integer> roleIds) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        List<Role> roles = RoleCache.getRoles(userId);
        List<Integer> existIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<Integer> needRemove = new ArrayList<>();
        List<Integer> needAdd = new ArrayList<>();
        for (Integer id : existIds) {
            if (!roleIds.contains(id)) {
                needRemove.add(id);
            }
        }
        for (Integer roleId : roleIds) {
            if (!existIds.contains(roleId)) {
                needAdd.add(roleId);
            }
        }
        if (!CollectionUtils.isEmpty(needRemove)) {
            userRoleRepository.deleteByUserIdAndRoleId(userId, needRemove);
            RoleCache.popRoles(userId, needRemove);
        }
        if (!CollectionUtils.isEmpty(needAdd)) {
            List<Role> addRoles = RoleCache.getRoleById(needAdd);
            List<UserRole> userRoles = new ArrayList<>();
            for (Role role : addRoles) {
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRoles.add(userRole);
            }
            userRoleRepository.save(userRoles);
            RoleCache.pushRoles(userId, addRoles);
        }
    }
}
