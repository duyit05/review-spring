package com.test.devteria.devteria.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
     @Size(min = 3 , message = "USER_INVALID")
     String username;

     @Size(min = 8 , message = "PASSWORD_INVALID")
     String password;
     String firstName;
     String lastName;
     LocalDate dob;



}
