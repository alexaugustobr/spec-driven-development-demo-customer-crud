package br.com.example.sdd.customers.customer;

import br.com.example.sdd.customers.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CustomerRepositoryTest {

    private static final String VALID_CPF_1 = generateValidCpf(1);
    private static final String VALID_CPF_2 = generateValidCpf(2);

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAllInBatch();
    }

    @Test
    void findAllPageableReturnsCorrectPageSize() {
        saveCustomers(25);

        Page<Customer> page = customerRepository.findAll(PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getTotalElements()).isEqualTo(25);
    }

    @Test
    void findByDocumentReturnsExactMatch() {
        Customer customer = buildCustomer("Joao", "joao@example.com", VALID_CPF_1, "Avenida Paulista", "Bela Vista");
        customerRepository.save(customer);

        Page<Customer> result = customerRepository.findByDocument(VALID_CPF_1, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDocument()).isEqualTo(VALID_CPF_1);
    }

    @Test
    void findByNameContainingIgnoreCaseIsPartialAndCaseInsensitive() {
        Customer joao = buildCustomer("Joao Silva", "joao@example.com", VALID_CPF_1, "Avenida Paulista", "Bela Vista");
        Customer jose = buildCustomer("Jose Santos", "jose@example.com", VALID_CPF_2, "Rua Augusta", "Consolacao");
        customerRepository.saveAll(List.of(joao, jose));

        Page<Customer> result = customerRepository.findByNameContainingIgnoreCase("jo", PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByEmailContainingIgnoreCaseIsPartial() {
        Customer customer = buildCustomer("Joao", "joao@example.com", VALID_CPF_1, "Avenida Paulista", "Bela Vista");
        customerRepository.save(customer);

        Page<Customer> result = customerRepository.findByEmailContainingIgnoreCase(
                "@example", PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).contains("@example");
    }

    @Test
    void findAllSortedByNameAscending() {
        Customer zara = buildCustomer("Zara", "zara@example.com", VALID_CPF_1, "Rua A", "Bela Vista");
        Customer ana = buildCustomer("Ana", "ana@example.com", VALID_CPF_2, "Rua B", "Consolacao");
        customerRepository.saveAll(List.of(zara, ana));

        Page<Customer> result = customerRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name")));

        assertThat(result.getContent().get(0).getName()).isEqualTo("Ana");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Zara");
    }

    @Test
    void findAllSortedByCreatedAtDescending() {
        Customer older = Customer.builder()
                .name("Older")
                .email("older@example.com")
                .document(VALID_CPF_1)
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua A")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .build();
        Customer newer = Customer.builder()
                .name("Newer")
                .email("newer@example.com")
                .document(VALID_CPF_2)
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua B")
                .neighborhood("Consolacao")
                .city("Sao Paulo")
                .state("SP")
                .createdAt(OffsetDateTime.now())
                .build();
        customerRepository.saveAll(List.of(older, newer));

        Page<Customer> result = customerRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(result.getContent().get(0).getName()).isEqualTo("Newer");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Older");
    }

    private Customer buildCustomer(String name, String email, String document,
                                    String street, String neighborhood) {
        return Customer.builder()
                .name(name)
                .email(email)
                .document(document)
                .phone("11988887777")
                .zipCode("01310200")
                .street(street)
                .neighborhood(neighborhood)
                .city("Sao Paulo")
                .state("SP")
                .build();
    }

    private void saveCustomers(int count) {
        for (int i = 1; i <= count; i++) {
            String cpf = generateValidCpf(i);
            Customer customer = Customer.builder()
                    .name("Customer " + i)
                    .email("customer" + i + "@example.com")
                    .document(cpf)
                    .phone("11988887777")
                    .zipCode("01310200")
                    .street("Rua " + i)
                    .neighborhood("Bairro")
                    .city("Sao Paulo")
                    .state("SP")
                    .build();
            customerRepository.save(customer);
        }
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