package br.com.example.sdd.customers.cep;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CepServiceTest {

    @Test
    void lookupValidCepReturnsMappedAddress() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build()
                .expect(requestTo("https://viacep.com.br/ws/01310200/json/"))
                .andRespond(withSuccess("""
                        {"logradouro":"Av. Paulista","bairro":"Bela Vista","localidade":"São Paulo","uf":"SP"}""",
                        MediaType.APPLICATION_JSON));
        RestClient restClient = builder.baseUrl("https://viacep.com.br/ws").build();
        CepService cepService = new CepService(restClient);

        Optional<CepAddress> result = cepService.lookup("01310200");

        assertThat(result).isPresent();
        assertThat(result.get().street()).isEqualTo("Av. Paulista");
        assertThat(result.get().neighborhood()).isEqualTo("Bela Vista");
        assertThat(result.get().city()).isEqualTo("São Paulo");
        assertThat(result.get().state()).isEqualTo("SP");
    }

    @Test
    void lookupNotFoundCepReturnsEmpty() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build()
                .expect(requestTo("https://viacep.com.br/ws/00000000/json/"))
                .andRespond(withSuccess("""
                        {"erro":"true"}""",
                        MediaType.APPLICATION_JSON));
        RestClient restClient = builder.baseUrl("https://viacep.com.br/ws").build();
        CepService cepService = new CepService(restClient);

        Optional<CepAddress> result = cepService.lookup("00000000");

        assertThat(result).isEmpty();
    }

    @Test
    void lookupViaCepServerErrorReturnsEmpty() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build()
                .expect(requestTo("https://viacep.com.br/ws/01310200/json/"))
                .andRespond(withServerError());
        RestClient restClient = builder.baseUrl("https://viacep.com.br/ws").build();
        CepService cepService = new CepService(restClient);

        Optional<CepAddress> result = cepService.lookup("01310200");

        assertThat(result).isEmpty();
    }
}