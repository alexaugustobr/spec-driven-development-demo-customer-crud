package br.com.example.sdd.customers.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DocumentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocument {

    String message() default "CPF ou CNPJ inv\u00E1lido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
