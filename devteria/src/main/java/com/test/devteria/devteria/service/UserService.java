package com.test.devteria.devteria.service;

import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.enums.Role;
import com.test.devteria.devteria.exception.AppException;
import com.test.devteria.devteria.exception.ErrorCode;
import com.test.devteria.devteria.mapper.UserMapper;
import com.test.devteria.devteria.repository.UserRepository;
import com.test.devteria.devteria.request.UserCreationRequest;
import com.test.devteria.devteria.request.UserUpdateRequest;
import com.test.devteria.devteria.respone.UserRespone;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserRespone createUser(UserCreationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    // CHECK ROLE BEFORE AFTER CALL FUNCTION
    public List<UserRespone> getUsers() {
        log.info("Get all user");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    // CHECK USER LOGIN CORRECT WITH USERNAME LOGINING OR NOT IF CORRECT CAN CALL FUNCTION GET DETAIL INFORMATION
    // CALL FUNCTION BEFORE AFTER CHECK ROLE
    public UserRespone getUser(String id) {
        log.info("In method get user by id");
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserRespone updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public UserRespone getMyInfo (){
       String context =  SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(context).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
}
