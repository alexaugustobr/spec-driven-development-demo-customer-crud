package br.com.example.sdd.customers.cep;

import br.com.example.sdd.customers.TestcontainersConfiguration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CepControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CepService cepService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void validUnmaskedCepReturnsMappedAddressJson() throws Exception {
        when(cepService.lookup("01310200"))
                .thenReturn(Optional.of(new CepAddress("Av. Paulista", "Bela Vista", "São Paulo", "SP")));

        mockMvc.perform(get("/api/cep/01310200")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Av. Paulista"))
                .andExpect(jsonPath("$.neighborhood").value("Bela Vista"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.state").value("SP"));
    }

    @Test
    void validMaskedCepIsNormalizedAndReturns200() throws Exception {
        when(cepService.lookup(anyString()))
                .thenReturn(Optional.of(new CepAddress("Rua Augusta", "Consolação", "São Paulo", "SP")));

        mockMvc.perform(get("/api/cep/01310-200")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(cepService).lookup("01310200");
    }

    @Test
    void tooShortCepReturns400() throws Exception {
        mockMvc.perform(get("/api/cep/1234")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericCepReturns400() throws Exception {
        mockMvc.perform(get("/api/cep/ABCDEFGH")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nineDigitCepReturns400() throws Exception {
        mockMvc.perform(get("/api/cep/123456789")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cepNotFoundReturns404() throws Exception {
        when(cepService.lookup("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cep/99999999")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedCepRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/cep/01310200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}