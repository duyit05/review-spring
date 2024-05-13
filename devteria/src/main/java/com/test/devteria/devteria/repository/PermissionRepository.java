package com.test.devteria.devteria.repository;

import com.test.devteria.devteria.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission , String> {
}
