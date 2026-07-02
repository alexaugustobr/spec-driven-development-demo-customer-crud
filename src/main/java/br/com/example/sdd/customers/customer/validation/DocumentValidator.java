package br.com.example.sdd.customers.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentValidator implements ConstraintValidator<ValidDocument, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String digits = value.replaceAll("\\D", "");

        if (digits.length() == 11) {
            return isValidCpf(digits);
        } else if (digits.length() == 14) {
            return isValidCnpj(digits);
        }

        return false;
    }

    private boolean isValidCpf(String cpf) {
        if (isAllSameDigit(cpf)) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstCheckDigit = 11 - (sum % 11);
        if (firstCheckDigit >= 10) {
            firstCheckDigit = 0;
        }
        if (firstCheckDigit != (cpf.charAt(9) - '0')) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondCheckDigit = 11 - (sum % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }
        return secondCheckDigit == (cpf.charAt(10) - '0');
    }

    private boolean isValidCnpj(String cnpj) {
        if (isAllSameDigit(cnpj)) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * weights1[i];
        }
        int firstCheckDigit = 11 - (sum % 11);
        if (firstCheckDigit >= 10) {
            firstCheckDigit = 0;
        }
        if (firstCheckDigit != (cnpj.charAt(12) - '0')) {
            return false;
        }

        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * weights2[i];
        }
        int secondCheckDigit = 11 - (sum % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }
        return secondCheckDigit == (cnpj.charAt(13) - '0');
    }

    private boolean isAllSameDigit(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }
}
