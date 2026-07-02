package br.com.example.sdd.customers.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer create(Customer form) {
        stripMasks(form);

        if (customerRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("E-mail j\u00E1 cadastrado.");
        }
        if (customerRepository.existsByDocument(form.getDocument())) {
            throw new IllegalArgumentException("CPF ou CNPJ j\u00E1 cadastrado.");
        }

        return customerRepository.save(form);
    }

    public Customer update(Long id, Customer form) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        stripMasks(form);

        if (customerRepository.existsByEmailAndIdNot(form.getEmail(), id)) {
            throw new IllegalArgumentException("E-mail j\u00E1 cadastrado.");
        }
        if (customerRepository.existsByDocumentAndIdNot(form.getDocument(), id)) {
            throw new IllegalArgumentException("CPF ou CNPJ j\u00E1 cadastrado.");
        }

        existing.setName(form.getName());
        existing.setEmail(form.getEmail());
        existing.setDocument(form.getDocument());
        existing.setPhone(form.getPhone());
        existing.setZipCode(form.getZipCode());
        existing.setStreet(form.getStreet());
        existing.setNeighborhood(form.getNeighborhood());
        existing.setCity(form.getCity());
        existing.setState(form.getState());

        return customerRepository.save(existing);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        customerRepository.deleteById(id);
    }

    private void stripMasks(Customer form) {
        form.setDocument(form.getDocument().replaceAll("\\D", ""));
        form.setPhone(form.getPhone().replaceAll("\\D", ""));
        form.setZipCode(form.getZipCode().replaceAll("\\D", ""));
    }
}