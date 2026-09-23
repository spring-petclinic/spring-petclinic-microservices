package org.springframework.samples.petclinic.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private WebTestClient client;

    @Test
    void staticResourcesArePublic() {
        client.get()
            .uri("/")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void currentUserIsPublicAndAnonymousByDefault() {
        client.get()
            .uri("/api/user/me")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.authenticated").isEqualTo(false)
            .jsonPath("$.roles").isEmpty();
    }

    @Test
    void apiCallsOfAnonymousUsersAreRejected() {
        client.get()
            .uri("/api/customer/owners")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void browsersAreRedirectedToTheAuthorizationServer() {
        client.get()
            .uri("/api/customer/owners")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().valueEquals(HttpHeaders.LOCATION, "/oauth2/authorization/petclinic");
    }

    @Test
    void theCsrfTokenIsExposedToTheSinglePageApplication() {
        client.get()
            .uri("/api/user/me")
            .exchange()
            .expectStatus().isOk()
            .expectCookie().exists("XSRF-TOKEN");
    }

    @Test
    void stateChangingRequestsWithoutCsrfTokenAreRejected() {
        client.post()
            .uri("/logout")
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void stateChangingRequestsAreAcceptedWithTheCsrfCookie() {
        String csrfToken = client.get()
            .uri("/api/user/me")
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class)
            .getResponseCookies()
            .getFirst("XSRF-TOKEN")
            .getValue();

        client.post()
            .uri("/logout")
            .cookie("XSRF-TOKEN", csrfToken)
            .header("X-XSRF-TOKEN", csrfToken)
            .exchange()
            .expectStatus().isFound();
    }
}
