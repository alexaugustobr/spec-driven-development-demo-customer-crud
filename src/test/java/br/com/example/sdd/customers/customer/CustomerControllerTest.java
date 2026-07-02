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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CustomerControllerTest {

    private static final String VALID_CPF_1 = generateValidCpf(1);
    private static final String VALID_CPF_2 = generateValidCpf(2);

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
        seedCustomer("Joao Silva", "joao@example.com", VALID_CPF_1);
        seedCustomer("Jose Santos", "jose@example.com", VALID_CPF_2);
        seedCustomer("Maria Souza", "maria@example.com", generateValidCpf(3));

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
        seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers")
                        .param("q", VALID_CPF_1)
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchByCnpjReturnsExactMatch() throws Exception {
        seedCustomer("Empresa", "empresa@example.com", "11222333000181");

        mockMvc.perform(get("/customers")
                        .param("q", "11222333000181")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchByEmailFiltersResults() throws Exception {
        seedCustomer("Joao", "joao@example.com", VALID_CPF_1);
        seedCustomer("Maria", "maria@test.com", VALID_CPF_2);

        mockMvc.perform(get("/customers")
                        .param("q", "joao@example.com")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(1L))));
    }

    @Test
    void searchWithNoMatchReturnsEmptyPage() throws Exception {
        seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers")
                        .param("q", "XXXXXXXXXXX")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("page",
                        hasProperty("totalElements", is(0L))));
    }

    @Test
    void sortByNameAscReturnsAlphabeticOrder() throws Exception {
        seedCustomer("Zara", "zara@example.com", VALID_CPF_1);
        seedCustomer("Ana", "ana@example.com", VALID_CPF_2);

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
        seedCustomerWithDate("Older", "older@example.com", VALID_CPF_1,
                OffsetDateTime.now().minusDays(1));
        seedCustomerWithDate("Newer", "newer@example.com", VALID_CPF_2,
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

    // ---- F04: Access Control ----

    @Test
    void attendantCannotAccessNewCustomerForm() throws Exception {
        mockMvc.perform(get("/customers/new")
                        .with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessNewCustomerForm() throws Exception {
        mockMvc.perform(get("/customers/new")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    void attendantCannotSubmitNewCustomer() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("attendant@example.com").roles("ATTENDANT"))
                        .param("name", "Test")
                        .param("email", "test@example.com")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Teste")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isForbidden());
    }

    @Test
    void attendantCannotAccessEditForm() throws Exception {
        Customer saved = seedCustomer("Test", "test@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers/" + saved.getId() + "/edit")
                        .with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void attendantCannotUpdateCustomer() throws Exception {
        Customer saved = seedCustomer("Test", "test@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers/" + saved.getId())
                        .with(csrf())
                        .with(user("attendant@example.com").roles("ATTENDANT"))
                        .param("name", "Updated")
                        .param("email", "updated@example.com")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Teste")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isForbidden());
    }

    @Test
    void attendantCannotDeleteCustomer() throws Exception {
        Customer saved = seedCustomer("Test", "test@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers/" + saved.getId() + "/delete")
                        .with(csrf())
                        .with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(status().isForbidden());
    }

    // ---- F04: CRUD Happy Paths ----

    @Test
    void adminCreatesValidCustomerRedirectsToList() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Joao Silva")
                        .param("email", "joao@example.com")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("successMessage"));

        List<Customer> all = customerRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("joao@example.com", all.get(0).getEmail());
        assertEquals(VALID_CPF_1, all.get(0).getDocument());
    }

    @Test
    void adminViewsCustomerDetailPage() throws Exception {
        Customer saved = seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers/" + saved.getId())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/view"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void attendantViewsCustomerDetailWithoutAdminFlag() throws Exception {
        Customer saved = seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers/" + saved.getId())
                        .with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/view"))
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    void adminAccessesEditForm() throws Exception {
        Customer saved = seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(get("/customers/" + saved.getId() + "/edit")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    void adminUpdatesCustomerRedirectsToDetail() throws Exception {
        Customer saved = seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers/" + saved.getId())
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Joao Atualizado")
                        .param("email", "joao@example.com")
                        .param("document", VALID_CPF_2)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + saved.getId()))
                .andExpect(flash().attributeExists("successMessage"));

        Customer updated = customerRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Joao Atualizado", updated.getName());
    }

    @Test
    void adminDeletesCustomerRedirectsToList() throws Exception {
        Customer saved = seedCustomer("Joao", "joao@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers/" + saved.getId() + "/delete")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("successMessage"));

        assertFalse(customerRepository.existsById(saved.getId()));
    }

    @Test
    void viewNonExistentCustomerReturns404() throws Exception {
        mockMvc.perform(get("/customers/99999")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void editNonExistentCustomerReturns404() throws Exception {
        mockMvc.perform(get("/customers/99999/edit")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    // ---- F04: Validation Errors ----

    @Test
    void createWithInvalidCpfIsRejected() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Joao")
                        .param("email", "joao@example.com")
                        .param("document", "111.111.111-11")
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasErrors("customer"))
                .andExpect(model().attributeHasFieldErrors("customer", "document"));
    }

    @Test
    void createWithInvalidCnpjIsRejected() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Empresa")
                        .param("email", "empresa@example.com")
                        .param("document", "11.111.111/1111-11")
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasErrors("customer"));
    }

    @Test
    void createWithBlankNameIsRejected() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "")
                        .param("email", "joao@example.com")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasFieldErrors("customer", "name"));
    }

    @Test
    void createWithInvalidEmailIsRejected() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "Joao")
                        .param("email", "notanemail")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasFieldErrors("customer", "email"));
    }

    @Test
    void createWithDuplicateEmailIsRejected() throws Exception {
        seedCustomer("Existing", "duplicate@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "New")
                        .param("email", "duplicate@example.com")
                        .param("document", VALID_CPF_2)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasErrors("customer"))
                .andExpect(content().string(containsString("E-mail j\u00E1 cadastrado")));
    }

    @Test
    void createWithDuplicateDocumentIsRejected() throws Exception {
        seedCustomer("Existing", "existing@example.com", VALID_CPF_1);

        mockMvc.perform(post("/customers")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN"))
                        .param("name", "New")
                        .param("email", "new@example.com")
                        .param("document", VALID_CPF_1)
                        .param("phone", "11988887777")
                        .param("zipCode", "01310200")
                        .param("street", "Rua Exemplo")
                        .param("neighborhood", "Centro")
                        .param("city", "Sao Paulo")
                        .param("state", "SP"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeHasErrors("customer"))
                .andExpect(content().string(containsString("CPF ou CNPJ j\u00E1 cadastrado")));
    }

    private void seed(int count) {
        for (int i = 1; i <= count; i++) {
            String cpf = generateValidCpf(i);
            seedCustomer("Customer " + i, "customer" + i + "@example.com", cpf);
        }
    }

    private Customer seedCustomer(String name, String email, String document) {
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
        return customerRepository.save(customer);
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

    private static String generateValidCpf(int seed) {
        String base = String.format("%09d", (500000000 + seed) % 1000000000);
        int[] digits = new int[11];
        for (int i = 0; i < 9; i++) {
            digits[i] = base.charAt(i) - '0';
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += digits[i] * (10 - i);
        }
        digits[9] = 11 - (sum % 11);
        if (digits[9] >= 10) digits[9] = 0;
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * (11 - i);
        }
        digits[10] = 11 - (sum % 11);
        if (digits[10] >= 10) digits[10] = 0;
        StringBuilder sb = new StringBuilder();
        for (int d : digits) sb.append(d);
        return sb.toString();
    }
}