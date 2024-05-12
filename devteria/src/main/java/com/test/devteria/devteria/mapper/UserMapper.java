package com.test.devteria.devteria.mapper;

import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.request.UserCreationRequest;
import com.test.devteria.devteria.request.UserUpdateRequest;
import com.test.devteria.devteria.respone.UserRespone;
import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser (UserCreationRequest request);
    UserRespone toUserResponse (User user);
    void updateUser (@MappingTarget User user , UserUpdateRequest request);
}
