package com.nagarro.eclaims.rbac.service;

import com.nagarro.eclaims.rbac.entity.Permission;
import com.nagarro.eclaims.rbac.entity.Role;
import com.nagarro.eclaims.rbac.repository.PermissionRepository;
import com.nagarro.eclaims.rbac.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RbacService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Get all permissions for a user based on their roles
     */
    public Set<String> getPermissionsForRoles(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> permissions = new HashSet<>();
        for (String roleCode : roleCodes) {
            roleRepository.findByCodeAndActiveTrue(roleCode)
                    .ifPresent(role -> {
                        role.getPermissions()
                                .stream()
                                .filter(p -> p.getActive() != null && p.getActive())
                                .map(Permission::getCode)
                                .forEach(permissions::add);
                    });
        }
        return permissions;
    }

    /**
     * Check if a role exists and is active
     */
    public boolean roleExists(String code) {
        return roleRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    /**
     * Check if a permission exists and is active
     */
    public boolean permissionExists(String code) {
        return permissionRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    /**
     * Get all active roles
     */
    public List<Role> listActiveRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> r.getActive() != null && r.getActive())
                .collect(Collectors.toList());
    }

    /**
     * Get all active permissions
     */
    public List<Permission> listActivePermissions() {
        return permissionRepository.findAll().stream()
                .filter(p -> p.getActive() != null && p.getActive())
                .collect(Collectors.toList());
    }

    /**
     * Get a role by code
     */
    public Optional<Role> getRoleByCode(String code) {
        return roleRepository.findByCodeAndActiveTrue(code);
    }

    /**
     * Get permissions for a specific role
     */
    public Set<String> getPermissionsForRole(String roleCode) {
        return roleRepository.findByCodeAndActiveTrue(roleCode)
                .map(role -> role.getPermissions()
                        .stream()
                        .filter(p -> p.getActive() != null && p.getActive())
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .orElseGet(Collections::emptySet);
    }
}

