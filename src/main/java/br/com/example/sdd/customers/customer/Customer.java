package br.com.example.sdd.customers.customer;

import br.com.example.sdd.customers.customer.validation.ValidDocument;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @ValidDocument
    @Column(nullable = false, unique = true, length = 14)
    private String document;

    @NotBlank
    @Column(nullable = false, length = 15)
    private String phone;

    @NotBlank
    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String street;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String neighborhood;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Size(max = 2)
    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
