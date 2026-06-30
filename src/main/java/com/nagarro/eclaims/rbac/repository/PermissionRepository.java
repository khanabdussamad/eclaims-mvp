package com.nagarro.eclaims.rbac.repository;

import com.nagarro.eclaims.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);
    Optional<Permission> findByCodeAndActiveTrue(String code);
    List<Permission> findByCodeIn(Set<String> codes);
    List<Permission> findByCodeInAndActiveTrue(Set<String> codes);
}

