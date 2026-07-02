package br.com.example.sdd.customers.cep;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CepController {

    private final CepService cepService;

    @GetMapping("/api/cep/{cep}")
    public ResponseEntity<CepAddress> lookup(@PathVariable String cep) {
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != 8) {
            return ResponseEntity.badRequest().build();
        }
        return cepService.lookup(digits)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}