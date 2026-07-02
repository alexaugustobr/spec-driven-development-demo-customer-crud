package br.com.example.sdd.customers.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByDocument(String document, Pageable pageable);

    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Customer> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDocumentAndIdNot(String document, Long id);
}
