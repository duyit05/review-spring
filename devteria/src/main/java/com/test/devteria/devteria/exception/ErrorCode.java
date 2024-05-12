package com.test.devteria.devteria.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
    KEY_INVALID(1001 , "Uncategorized error"),
    USER_EXISTED(1002,  "User Existed"),
    USER_INVALID(1003 , "Username must be at least 3 characters"),
    PASSWORD_INVALID(1004 , "Password must be at least 8 characters"),
    USER_NOT_EXISTED(1005 , "Username not existed"),
    UNAUTHENTICATED(1006 , "Unauthenticated");

    private int code;
    private String message;

}
