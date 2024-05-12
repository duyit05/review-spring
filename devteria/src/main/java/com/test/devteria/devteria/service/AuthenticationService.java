package com.test.devteria.devteria.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.exception.AppException;
import com.test.devteria.devteria.exception.ErrorCode;
import com.test.devteria.devteria.repository.UserRepository;
import com.test.devteria.devteria.request.AuthenticationRequest;
import com.test.devteria.devteria.request.IntrospectRequest;
import com.test.devteria.devteria.respone.AuthenticationResponse;
import com.test.devteria.devteria.respone.IntrospectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    @NonFinal
    @Value("${spring.datasource.jwt.privateKey}")
    protected String SIGNER_KEY;

    UserRepository userRepository;

    public AuthenticationResponse authentication(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generalToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();

    }

    // VERIFIER TOKEN
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);

            // Kiểm tra hết hạn và sự xác thực của JWT
            boolean isValid = verified && expityTime.after(new Date());
            return IntrospectResponse.builder().valid(isValid).build();
        } catch (JOSEException | ParseException e) {
            // Xử lý ngoại lệ JOSEException hoặc ParseException ở đây
            // Ví dụ: Log lỗi, thông báo cho người dùng, hoặc xử lý ngoại lệ theo cách khác
            e.printStackTrace(); // In stack trace của ngoại lệ
            return IntrospectResponse.builder().valid(false) // Trả về false vì không thể xác minh token
                    .build();
        }
    }

    public String generalToken(User user) {
        //  CREATE HEADER
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // CREATE PAYLOAD
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer("duymonkey.com").issueTime(new Date()).expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())).claim("scope", buildScope(user)).build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // REQUEST 2 PARAMETEER HEADER AND PAYLOAD
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }
}
