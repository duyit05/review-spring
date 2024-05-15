package com.test.devteria.devteria.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.test.devteria.devteria.entity.InvalidatedToken;
import com.test.devteria.devteria.entity.User;
import com.test.devteria.devteria.exception.AppException;
import com.test.devteria.devteria.exception.ErrorCode;
import com.test.devteria.devteria.repository.InvalidatedTokenRepository;
import com.test.devteria.devteria.repository.UserRepository;
import com.test.devteria.devteria.request.AuthenticationRequest;
import com.test.devteria.devteria.request.IntrospectRequest;
import com.test.devteria.devteria.request.LogoutRequest;
import com.test.devteria.devteria.request.RefreshTokenRequest;
import com.test.devteria.devteria.respone.ApiRespone;
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

    InvalidatedTokenRepository invalidatedTokenRepository;

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
        // GET TOKEN FROM REQUEST
        var token = request.getToken();
        boolean invalid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            invalid = false;
        }
        return IntrospectResponse.builder().valid(invalid).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // VERIFY TOKEN FROM REQUEST
        SignedJWT signToken = verifyToken(request.getToken());

        // GET ID OF TOKEN
        String jwtIdToken = signToken.getJWTClaimsSet().getJWTID();

        // VERIFY TOKEN EXPIRATION TIME OR NOT
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        // ASSIGN INFOR TOKEN
        InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jwtIdToken).expiryTime(expiryTime).build();

        // SAVE TO DATABASE
        invalidatedTokenRepository.save(invalidatedToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {

        // VERIFY TOKEN FROM REQUEST
        SignedJWT signJWT = verifyToken(request.getToken());

        // GET ID OF TOKEN
        String jwtIdToken = signJWT.getJWTClaimsSet().getJWTID();

        // VERIFY EXPIRATION TIME
        Date expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        // ASSIGN INFOR TOKEN
        InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jwtIdToken).expiryTime(expiryTime).build();

        // SAVE TO DATABASE
        invalidatedTokenRepository.save(invalidatedToken);

        // VERIFY USERNAME
        String username = signJWT.getJWTClaimsSet().getSubject();

        // VERIFY USERNAME EXIST IN DATABASE OR NOT
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // GENERAL NEW TOKEN
        String token = generalToken(user);

        // RESPONSE
        return AuthenticationResponse.builder().token(token).authenticated(true).build();

    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        // VERIFY TOKEN, USING SIGNER_KEY TO CHECK TOKEN CAN  REPLACE OR NOT
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // DECRYPTION TOKEN
        SignedJWT signedJWT = SignedJWT.parse(token);

        // CHECK TOKEN EXPIRATION TIME
        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // VERIFY SIGNER_KEY NOT REPLACE
        boolean verified = signedJWT.verify(verifier);

        // CHECK TOKEN VALID OR NOT AND TOKEN EXPITY TIME OR NOT
        if (!(verified && expityTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        // CHECK TOKEN LOGOUT OR NOT
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public String generalToken(User user) {
        //  CREATE HEADER FOR TOKEN WITH ALGORITM HS 512 TO SIGN
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // CREATE CLAIMS FOR TOKEN
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer("duymonkey.com").issueTime(new Date()).expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())).claim("scope", buildScope(user)).jwtID(UUID.randomUUID().toString()).build();

        // CREATE PAYLOAD CONTAIN INFOR TOKEN AND AFTER PARSE CLAIMS TO JSON AND PUT INTO PAYLOAD
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // CREATE AND SIGN TOKEN
        // EXPLAIN : JWSOBJECT IS OBJECT REPRESENT FOR JWT, CREATED TO HEADER AND PAYLOAD
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            // SIGN TOKEN WITH PRIVATE KEY
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

            // CONVERT TOKEN TO STRING
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        // USING StringJoiner TO CONNECT STRING TOGETHER
        // HERE ROLES AND PERMISSIONS OF USER WITH SPACE AS SEPARATORS
        StringJoiner stringJoiner = new StringJoiner(" ");

        // GET ROLES AND PERMISSIONS AND BUILD LIMIT CHAIN
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            // FOOP EACH ROLE
            user.getRoles().forEach(role -> {

                // IF USER HAVE ROLE ADD EACH ROLE TO StringJoiner WITH PREFIX ROLE_
                stringJoiner.add("ROLE_" + role.getName());

                // FOR EACH PERMISSION
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    // IF ROLE HAVE PERMISSIONS, ADD PERMISSION TO StringJoiner
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }
        // RETURN ROLES AND PERMISSION CONNECT TOGETHER
        return stringJoiner.toString();
    }
}
