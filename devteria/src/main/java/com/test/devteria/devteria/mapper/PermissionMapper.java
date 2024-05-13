package com.test.devteria.devteria.mapper;


import com.test.devteria.devteria.entity.Permission;
import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.request.PermissionRequest;
import com.test.devteria.devteria.request.UserCreationRequest;
import com.test.devteria.devteria.request.UserUpdateRequest;
import com.test.devteria.devteria.respone.PermissionRespone;
import com.test.devteria.devteria.respone.UserRespone;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission (PermissionRequest request);
    PermissionRespone toPermissionRespone(Permission permission);

}
