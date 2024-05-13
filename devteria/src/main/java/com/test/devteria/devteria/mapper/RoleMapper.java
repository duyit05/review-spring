package com.test.devteria.devteria.mapper;


import com.test.devteria.devteria.entity.Permission;
import com.test.devteria.devteria.entity.Role;
import com.test.devteria.devteria.request.PermissionRequest;
import com.test.devteria.devteria.request.RoleRequest;
import com.test.devteria.devteria.respone.PermissionRespone;
import com.test.devteria.devteria.respone.RoleRespone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions" , ignore = true)
    Role toRole (RoleRequest request);
    RoleRespone toRoleResponse(Role role);

}
