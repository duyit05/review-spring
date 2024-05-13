package com.test.devteria.devteria.repository;

import com.test.devteria.devteria.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role , String> {
}
