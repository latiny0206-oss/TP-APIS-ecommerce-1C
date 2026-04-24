package com.trekking.ecommerce;

import com.trekking.ecommerce.security.JwtUtil;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "TestSecretKeyForJWTValidationThatIsLongEnough256bits");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        Method init = JwtUtil.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(jwtUtil);
    }

    @Test
    void generateToken_retornaTokenNoVacio() {
        UserDetails user = buildUser("testuser");

        String token = jwtUtil.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_retornaNombreDeUsuarioCorrecto() {
        UserDetails user = buildUser("juan");
        String token = jwtUtil.generateToken(user);

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("juan");
    }

    @Test
    void isTokenValid_tokenDelMismoUsuario_retornaTrue() {
        UserDetails user = buildUser("maria");
        String token = jwtUtil.generateToken(user);

        assertThat(jwtUtil.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_tokenDeOtroUsuario_retornaFalse() {
        UserDetails owner = buildUser("owner");
        UserDetails other = buildUser("other");
        String token = jwtUtil.generateToken(owner);

        assertThat(jwtUtil.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_tokenExpirado_retornaFalse() throws Exception {
        JwtUtil expiredUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredUtil, "secret",
                "TestSecretKeyForJWTValidationThatIsLongEnough256bits");
        ReflectionTestUtils.setField(expiredUtil, "expiration", -1000L);
        Method init = JwtUtil.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(expiredUtil);

        UserDetails user = buildUser("expired");
        String token = expiredUtil.generateToken(user);

        assertThat(jwtUtil.isTokenValid(token, user)).isFalse();
    }

    @Test
    void isTokenValid_tokenMalformado_retornaFalse() {
        UserDetails user = buildUser("malformed");

        assertThat(jwtUtil.isTokenValid("esto-no-es-un-jwt", user)).isFalse();
    }

    @Test
    void isTokenValid_tokenConFirmaInvalida_retornaFalse() throws Exception {
        JwtUtil otherSigner = new JwtUtil();
        ReflectionTestUtils.setField(otherSigner, "secret",
                "AnotherSecretKeyForJWTValidationLongEnough256bits");
        ReflectionTestUtils.setField(otherSigner, "expiration", 86400000L);
        Method init = JwtUtil.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(otherSigner);

        UserDetails user = buildUser("signedByOther");
        String foreignToken = otherSigner.generateToken(user);

        assertThat(jwtUtil.isTokenValid(foreignToken, user)).isFalse();
    }

    @Test
    void isTokenValid_tokenNuloOVacio_retornaFalse() {
        UserDetails user = buildUser("blank");

        assertThat(jwtUtil.isTokenValid(null, user)).isFalse();
        assertThat(jwtUtil.isTokenValid("", user)).isFalse();
        assertThat(jwtUtil.isTokenValid("   ", user)).isFalse();
    }

    private UserDetails buildUser(String username) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities(List.of())
                .build();
    }
}
