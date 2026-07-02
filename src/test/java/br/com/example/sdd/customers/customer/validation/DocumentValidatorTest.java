package br.com.example.sdd.customers.customer.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentValidatorTest {

    private final DocumentValidator validator = new DocumentValidator();

    @Test
    void validCpfPassesValidation() {
        assertTrue(validator.isValid("529.982.247-25", null));
    }

    @Test
    void validCpfUnmaskedPassesValidation() {
        assertTrue(validator.isValid("52998224725", null));
    }

    @Test
    void validCnpjPassesValidation() {
        assertTrue(validator.isValid("11.222.333/0001-81", null));
    }

    @Test
    void invalidCpfWrongCheckDigitFails() {
        String validCpf = "52998224725";
        String lastDigit = validCpf.substring(10, 11);
        String alteredDigit = lastDigit.equals("9") ? "0" : "9";
        String alteredCpf = validCpf.substring(0, 10) + alteredDigit;

        assertFalse(validator.isValid(alteredCpf, null));
    }

    @Test
    void invalidCnpjWrongCheckDigitFails() {
        String validCnpj = "11222333000181";
        String lastDigit = validCnpj.substring(13, 14);
        String alteredDigit = lastDigit.equals("9") ? "0" : "9";
        String alteredCnpj = validCnpj.substring(0, 13) + alteredDigit;

        assertFalse(validator.isValid(alteredCnpj, null));
    }

    @Test
    void allSameDigitCpfFails() {
        assertFalse(validator.isValid("111.111.111-11", null));
    }

    @Test
    void allSameDigitCnpjFails() {
        assertFalse(validator.isValid("11.111.111/1111-11", null));
    }

    @Test
    void tooShortDocumentFails() {
        assertFalse(validator.isValid("1234567890", null));
    }

    @Test
    void thirteenDigitStringFails() {
        assertFalse(validator.isValid("1234567890123", null));
    }

    @Test
    void nullInputReturnsTrue() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void emptyStringReturnsTrue() {
        assertTrue(validator.isValid("", null));
    }
}
