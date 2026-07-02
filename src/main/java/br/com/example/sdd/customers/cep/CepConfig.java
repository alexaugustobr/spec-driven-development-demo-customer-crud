package br.com.example.sdd.customers.cep;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CepConfig {

    @Bean
    public RestClient cepRestClient(@Value("${cep.api.base-url:https://viacep.com.br/ws}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}