package com.alumni.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class AppConfig {

    @Value("${phantombuster.timeout.seconds:60}")
    private int timeoutSeconds;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = timeoutSeconds * 1000;
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }
}
