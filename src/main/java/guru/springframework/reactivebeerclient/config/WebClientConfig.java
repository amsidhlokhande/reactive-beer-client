package guru.springframework.reactivebeerclient.config;

import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import static guru.springframework.reactivebeerclient.config.WebClientConfigProperties.BASE_URL;
import static io.netty.handler.logging.LogLevel.DEBUG;
import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient getWebClient() {
        return builder().baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap("reactor.netty.client.HttpClient", DEBUG, TEXTUAL)))
                .build();
    }
}
