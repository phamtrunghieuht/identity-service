package com.pthieu.identity_service.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pthieu.identity_service.dto.request.AuthenticationRequest;
import com.pthieu.identity_service.dto.request.IntrospectRequest;
import com.pthieu.identity_service.dto.response.AuthenticationResponse;
import com.pthieu.identity_service.dto.response.IntrospectResponse;
import com.pthieu.identity_service.entity.User;
import com.pthieu.identity_service.exception.AppException;
import com.pthieu.identity_service.exception.ErrorCode;
import com.pthieu.identity_service.repository.UserRepository;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean isAuthenticate = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isAuthenticate) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);
        logger.info("User {} authenticated successfully", user.getUsername());
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("identity-service")
                .issueTime(new Date())
                .expirationTime(new Date(
                    Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        // Create JWS object with the header and payload
        JWSObject jwsObject = new JWSObject(
            jwsHeader,
            payload
        );

        
            try {
                jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            } catch (JOSEException e) {
                logger.error("Error signing JWT", e);
                throw new RuntimeException(e);
            }
            return jwsObject.serialize();            
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        // Validate the token and return the introspection response
        // This is a placeholder implementation; actual validation logic should be added
        String token = request.getToken();
        if (token == null || token.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        JWSVerifier verifier;
        boolean verified = false;
        Date expiryTime = null;
        try {
            verifier = new MACVerifier(SIGNER_KEY.getBytes());

            SignedJWT signedJWT = SignedJWT.parse(token);

            expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            verified = signedJWT.verify(verifier);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JOSEException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Here you would typically decode and validate the JWT token
        // For simplicity, we assume the token is valid and return a dummy response
        return IntrospectResponse.builder()
                .valid(verified && expiryTime != null && expiryTime.after(new Date()))
                .build();
    }

    // private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
    //     JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

    //     SignedJWT signedJWT = SignedJWT.parse(token);

    //     Date expiryTime = (isRefresh)
    //             ? new Date(signedJWT
    //                     .getJWTClaimsSet()
    //                     .getIssueTime()
    //                     .toInstant()
    //                     .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
    //                     .toEpochMilli())
    //             : signedJWT.getJWTClaimsSet().getExpirationTime();

    //     var verified = signedJWT.verify(verifier);

    //     if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

    //     if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
    //         throw new AppException(ErrorCode.UNAUTHENTICATED);

    //     return signedJWT;
    // }

    String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        
        // verify if user has roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> stringJoiner.add(role));
        } else {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }
        return stringJoiner.toString();
    }
}
