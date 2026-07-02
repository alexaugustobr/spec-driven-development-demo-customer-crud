package br.com.example.sdd.customers.customer;

import br.com.example.sdd.customers.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CustomerControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        customerRepository.deleteAllInBatch();
    }

    @Test
    void unauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedAdminSeesListView() throws Exception {
        mockMvc.perform(get("/customers").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/list"));
    }

    @Test
    void authenticatedAttendantSeesListView() throws Exception {
        mockMvc.perform(get("/customers").with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/list"));
    }

    @Test
    void defaultPageLoads20RecordsMax() throws Exception {
        seed(25);

        mockMvc.perform(get("/customers").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("content", hasSize(lessThanOrEqualTo(20)))))
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(25L))));
    }

    @Test
    void searchByPartialNameFiltersResults() throws Exception {
        seedCustomer("Joao Silva", "joao@example.com", "11111111111");
        seedCustomer("Jose Santos", "jose@example.com", "22222222222");
        seedCustomer("Maria Souza", "maria@example.com", "33333333333");

        mockMvc.perform(get("/customers")
                        .param("q", "jo")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(2L))))
                .andExpect(content().string(containsString("Joao")))
                .andExpect(content().string(containsString("Jose")))
                .andExpect(content().string(not(containsString("Maria"))));
    }

    @Test
    void searchByCpfReturnsExactMatch() throws Exception {
        seedCustomer("Joao", "joao@example.com", "12345678901");

        mockMvc.perform(get("/customers")
                        .param("q", "12345678901")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchByCnpjReturnsExactMatch() throws Exception {
        seedCustomer("Empresa", "empresa@example.com", "12345678000190");

        mockMvc.perform(get("/customers")
                        .param("q", "12345678000190")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchByEmailFiltersResults() throws Exception {
        seedCustomer("Joao", "joao@example.com", "11111111111");
        seedCustomer("Maria", "maria@test.com", "22222222222");

        mockMvc.perform(get("/customers")
                        .param("q", "joao@example.com")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchWithNoMatchReturnsEmptyPage() throws Exception {
        seedCustomer("Joao", "joao@example.com", "11111111111");

        mockMvc.perform(get("/customers")
                        .param("q", "XXXXXXXXXXX")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(0L))));
    }

    @Test
    void sortByNameAscReturnsAlphabeticOrder() throws Exception {
        seedCustomer("Zara", "zara@example.com", "11111111111");
        seedCustomer("Ana", "ana@example.com", "22222222222");

        mockMvc.perform(get("/customers")
                        .param("sort", "name")
                        .param("dir", "asc")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("content", contains(
                                hasProperty("name", is("Ana")),
                                hasProperty("name", is("Zara"))
                        ))));
    }

    @Test
    void sortByCreatedAtDescIsDefault() throws Exception {
        seedCustomerWithDate("Older", "older@example.com", "11111111111",
                OffsetDateTime.now().minusDays(1));
        seedCustomerWithDate("Newer", "newer@example.com", "22222222222",
                OffsetDateTime.now());

        mockMvc.perform(get("/customers")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", is("createdAt")))
                .andExpect(model().attribute("dir", is("desc")));
    }

    @Test
    void pageParamLoadsCorrectPage() throws Exception {
        seed(25);

        mockMvc.perform(get("/customers")
                        .param("page", "1")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("number", is(1))));
    }

    private void seed(int count) {
        for (int i = 1; i <= count; i++) {
            seedCustomer("Customer " + i, "customer" + i + "@example.com",
                    String.format("%011d", i));
        }
    }

    private void seedCustomer(String name, String email, String document) {
        Customer customer = Customer.builder()
                .name(name)
                .email(email)
                .document(document)
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua Exemplo")
                .neighborhood("Centro")
                .city("Sao Paulo")
                .state("SP")
                .build();
        customerRepository.save(customer);
    }

    private void seedCustomerWithDate(String name, String email, String document,
                                       OffsetDateTime createdAt) {
        Customer customer = Customer.builder()
                .name(name)
                .email(email)
                .document(document)
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua Exemplo")
                .neighborhood("Centro")
                .city("Sao Paulo")
                .state("SP")
                .createdAt(createdAt)
                .build();
        customerRepository.save(customer);
    }
}
