package com.test.devteria.devteria.service;

import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.exception.AppException;
import com.test.devteria.devteria.repository.UserRepository;
import com.test.devteria.devteria.request.UserCreationRequest;
import com.test.devteria.devteria.respone.UserRespone;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    // INPUT OF TESTCASE
    private UserCreationRequest request;
    // OUTPUT OF TESTCASE
    private UserRespone respone;
    private LocalDate dob;

    private User user;

    // THIS ANNOTATION WILL START BEFOR @TEST
    @BeforeEach
    void initData() {
        dob = LocalDate.of(2000, 5, 5);
        request = UserCreationRequest.builder()
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .password("12345678")
                .dob(dob)
                .build();

        respone = UserRespone.builder()
                .id("7eab3d21055c")
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        user = User.builder()
                .id("7eab3d21055c")
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVE
        Mockito.when(userRepository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(false);
        Mockito.when(userRepository.save(ArgumentMatchers.any())).thenReturn(user);
        // WHEN
        userService.createUser(request);
        // THEN
        Assertions.assertThat(respone.getId()).isEqualTo("7eab3d21055c");
        Assertions.assertThat(respone.getUsername()).isEqualTo("john");
    }

    @Test
    void createUser_userExisted_fail (){
        // GIVEN
        Mockito.when(userRepository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(true);

        // WHEN
        AppException exception =  assertThrows(AppException.class , () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }
}
