package org.springframework.samples.petclinic.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = "petclinic.auth-server.issuer-uri=http://localhost:9000")
class AuthServerApplicationTests {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void contextLoads() {
    }

    @Test
    void exposesOpenIdProviderConfiguration() {
        assertThat(mvc.get().uri("/.well-known/openid-configuration").accept(MediaType.APPLICATION_JSON))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.issuer")
            .isEqualTo("http://localhost:9000");
    }

    @Test
    void exposesTheJsonWebKeySet() {
        assertThat(mvc.get().uri("/oauth2/jwks"))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.keys.length()")
            .isEqualTo(1);
    }

    @Test
    void requiresAuthenticationOnTheAuthorizationEndpoint() {
        assertThat(mvc.get()
            .uri("/oauth2/authorize?response_type=code&client_id=petclinic-gateway&scope=openid"
                + "&redirect_uri=http://localhost:8080/login/oauth2/code/petclinic"
                + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256")
            .accept(MediaType.TEXT_HTML))
            .hasStatus3xxRedirection()
            .hasRedirectedUrl("/login");
    }
}
