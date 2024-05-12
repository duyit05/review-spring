package com.test.devteria.devteria.exception;

import ch.qos.logback.core.spi.ErrorCodes;
import com.test.devteria.devteria.respone.ApiRespone;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    public final static ApiRespone respone = new ApiRespone();

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiRespone> handlingRuntimeException(RuntimeException exception) {

        respone.setCode(1001);
        respone.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(respone);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiRespone> handlingRuntimeException(AppException exception) {

        ErrorCode errorCode = exception.getErrorCode();
        respone.setCode(errorCode.getCode());
        respone.setMessage(exception.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(respone);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiRespone> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiRespone.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiRespone> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.KEY_INVALID;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {

        }
        respone.setCode(errorCode.getCode());
        respone.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(respone);
    }
}
