package com.test.devteria.devteria.controller;

import com.nimbusds.jose.JOSEException;
import com.test.devteria.devteria.request.AuthenticationRequest;
import com.test.devteria.devteria.request.IntrospectRequest;
import com.test.devteria.devteria.respone.ApiRespone;
import com.test.devteria.devteria.respone.AuthenticationResponse;
import com.test.devteria.devteria.respone.IntrospectResponse;
import com.test.devteria.devteria.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/log-in")
    ApiRespone<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authentication(request);
        return ApiRespone.<AuthenticationResponse>builder()
                .code(1000)
                .result(result)
                .build();
    }

    @PostMapping("/instrospect")
    ApiRespone<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiRespone.<IntrospectResponse>builder()
                .code(1000)
                .result(result)
                .build();
    }
}
