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
        Customer customer = Customer.builder()
                .name("Joao")
                .email("joao@example.com")
                .document("12345678901")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Avenida Paulista")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .build();
        customerRepository.save(customer);

        Page<Customer> result = customerRepository.findByDocument("12345678901", PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDocument()).isEqualTo("12345678901");
    }

    @Test
    void findByNameContainingIgnoreCaseIsPartialAndCaseInsensitive() {
        Customer joao = Customer.builder()
                .name("Joao Silva")
                .email("joao@example.com")
                .document("11111111111")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Avenida Paulista")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .build();
        Customer jose = Customer.builder()
                .name("Jose Santos")
                .email("jose@example.com")
                .document("22222222222")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua Augusta")
                .neighborhood("Consolacao")
                .city("Sao Paulo")
                .state("SP")
                .build();
        customerRepository.saveAll(List.of(joao, jose));

        Page<Customer> result = customerRepository.findByNameContainingIgnoreCase("jo", PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByEmailContainingIgnoreCaseIsPartial() {
        Customer customer = Customer.builder()
                .name("Joao")
                .email("joao@example.com")
                .document("11111111111")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Avenida Paulista")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .build();
        customerRepository.save(customer);

        Page<Customer> result = customerRepository.findByEmailContainingIgnoreCase(
                "@example", PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).contains("@example");
    }

    @Test
    void findAllSortedByNameAscending() {
        Customer zara = Customer.builder()
                .name("Zara")
                .email("zara@example.com")
                .document("11111111111")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua A")
                .neighborhood("Bela Vista")
                .city("Sao Paulo")
                .state("SP")
                .build();
        Customer ana = Customer.builder()
                .name("Ana")
                .email("ana@example.com")
                .document("22222222222")
                .phone("11988887777")
                .zipCode("01310200")
                .street("Rua B")
                .neighborhood("Consolacao")
                .city("Sao Paulo")
                .state("SP")
                .build();
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
                .document("11111111111")
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
                .document("22222222222")
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

    private void saveCustomers(int count) {
        for (int i = 1; i <= count; i++) {
            Customer customer = Customer.builder()
                    .name("Customer " + i)
                    .email("customer" + i + "@example.com")
                    .document(String.format("%011d", i))
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
}
