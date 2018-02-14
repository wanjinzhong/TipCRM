package com.tipcrm.service.impl;

import java.util.HashSet;
import java.util.Set;

import com.tipcrm.dao.entity.Role;
import com.tipcrm.dao.entity.RolePermission;
import com.tipcrm.dao.entity.User;
import com.tipcrm.dao.repository.UserRepository;
import com.tipcrm.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public Set<String> getPermissionValueListByUserId(Integer userId) {
        Set<String> permissions = new HashSet<String>();
        User user = userRepository.findOne(userId);
        for (Role role : user.getRoles()) {
            for (RolePermission rolePermission : role.getRolePermissions()) {
                permissions.add(rolePermission.getPermission().getValue());
            }
        }
        return permissions;
    }
}
