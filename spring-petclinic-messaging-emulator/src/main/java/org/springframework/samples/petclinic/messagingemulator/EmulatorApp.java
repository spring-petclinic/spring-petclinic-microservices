package org.springframework.samples.petclinic.messagingemulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@SpringBootApplication
@EnableDiscoveryClient
public class EmulatorApp {
    public static void main(String[] args) {
		SpringApplication.run(EmulatorApp.class, args);
	}

	@Value("classpath:/static/index.html")
    private Resource indexHtml;

    /**
     * workaround solution for forwarding to index.html
     * @see <a href="https://github.com/spring-projects/spring-boot/issues/9785">#9785</a>
     */
    @Bean
    RouterFunction<?> routerFunction() {
        RouterFunction router = RouterFunctions.resources("/**", new ClassPathResource("static/"))
            .andRoute(RequestPredicates.GET("/"),
                request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
        return router;
    }
    
}
