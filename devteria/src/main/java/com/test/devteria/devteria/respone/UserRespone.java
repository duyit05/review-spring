package com.test.devteria.devteria.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRespone {
     String id;
     String username;
     String firstName;
     String lastName;
     LocalDate dob;
     Set<RoleRespone> roles;

}
