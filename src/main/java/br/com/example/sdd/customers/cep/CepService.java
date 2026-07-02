package br.com.example.sdd.customers.cep;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class CepService {

    private final RestClient cepRestClient;

    public Optional<CepAddress> lookup(String cep) {
        try {
            Map<String, Object> response = cepRestClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && response.containsKey("erro")) {
                return Optional.empty();
            }

            if (response == null) {
                return Optional.empty();
            }

            return Optional.of(new CepAddress(
                    (String) response.get("logradouro"),
                    (String) response.get("bairro"),
                    (String) response.get("localidade"),
                    (String) response.get("uf")
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}