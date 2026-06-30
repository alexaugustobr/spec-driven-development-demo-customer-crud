# Implementation Plan: F04 - Customer Management (CRUD)

## Prerequisites

- F01 (Authentication System) implemented and tests passing
- F02 (Customer Listing and Search) implemented — `Customer` entity, `CustomerRepository`, `CustomerController`, `V2__create_customers.sql`, and `list.html` must exist
- F03 (Address Integration) implemented — `cep.js` must exist at `src/main/resources/static/js/cep.js`
- Docker running (Testcontainers requires Docker daemon for integration tests)

## Phase 1: Validation Layer

**1. @ValidDocument Annotation** — Create `ValidDocument.java` in `br.com.example.sdd.customers.customer.validation`. Define it as a `@Constraint(validatedBy = DocumentValidator.class)` annotation targeting `ElementType.FIELD` and `ElementType.PARAMETER`, with default message `"CPF ou CNPJ inválido"` and the standard `groups()` and `payload()` attributes required by Jakarta Validation.

**2. DocumentValidator** — Create `DocumentValidator.java` in the same `validation` package implementing `ConstraintValidator<ValidDocument, String>`. Return `true` for null or blank input (defer to `@NotBlank`). Strip all non-digits with `replaceAll("\\D", "")`. Branch on the resulting length: 11 → run the standard two-check-digit CPF algorithm (reject all-same-digit sequences); 14 → run the standard two-check-digit CNPJ algorithm (reject all-same-digit sequences); any other length → return `false`.

**3. DocumentValidatorTest** — Create `DocumentValidatorTest.java` in `src/test/.../customer/validation` using `@ExtendWith(MockitoExtension.class)` (no Spring context needed). Instantiate `DocumentValidator` directly and call `isValid(value, null)`. Cover all 11 test functions specified in the spec's Testing Strategy section, including masked input, null, empty, all-same-digit CPF and CNPJ, wrong check digits, and wrong lengths.

## Phase 2: Service Layer

**4. Customer Entity Validation Annotations** — Modify `Customer.java` to add Jakarta Validation annotations on all fields: `@NotBlank` on `name`, `email`, `document`, `phone`, `zipCode`, `street`, `neighborhood`, `city`, `state`; `@Email` on `email`; `@Size(max=255)` on `name`, `email`, `street`, `neighborhood`, `city`; `@Size(max=2)` on `state`; `@ValidDocument` on `document`. These enable `@Valid` binding in the controller.

**5. CustomerRepository Derived Methods** — Modify `CustomerRepository` to add four new derived query methods needed by `CustomerService`: `existsByEmail(String email)`, `existsByDocument(String document)`, `existsByEmailAndIdNot(String email, Long id)`, and `existsByDocumentAndIdNot(String document, Long id)`. Spring Data JPA generates these from the method names — no `@Query` annotation required.

**6. CustomerService** — Create `CustomerService.java` in `br.com.example.sdd.customers.customer` annotated `@Service`. Inject `CustomerRepository`. Implement three public methods: `create(Customer form)` strips masks from `document`, `phone`, `zipCode` via `replaceAll("\\D", "")`, checks for duplicate email and document, then calls `customerRepository.save(form)`; `update(Long id, Customer form)` loads the existing customer (throws `ResponseStatusException(NOT_FOUND)` if absent), strips masks, checks duplicates excluding own ID, merges all mutable fields, saves; `delete(Long id)` loads customer by ID (throws 404 if absent) then calls `customerRepository.deleteById(id)`.

## Phase 3: Controller + Security

**7. SecurityConfig ADMIN-Only URL Rules** — Modify `SecurityConfig.java` to add five `requestMatchers` before the existing `anyRequest().authenticated()` rule: `GET /customers/new`, `POST /customers`, `GET /customers/*/edit`, `POST /customers/*`, and `POST /customers/*/delete` — all requiring `hasRole("ADMIN")`. Spring Security's default access-denied handling returns 403, which Spring Boot auto-serves via `templates/error/403.html`.

**8. CustomerController CRUD Routes** — Modify `CustomerController.java` to inject `CustomerService` and add six handler methods for the routes in the spec. Each GET handler that looks up by ID throws `ResponseStatusException(NOT_FOUND)` when the customer is absent. Each POST mutation handler binds `@Valid @ModelAttribute Customer customer, BindingResult result, RedirectAttributes redirectAttributes, Authentication authentication` — returns the form view if `result.hasErrors()`, otherwise delegates to the service and sets `redirectAttributes.addFlashAttribute("successMessage", "...")` before redirecting. Add `isAdmin` (computed from `Authentication.getAuthorities()`) to the model on every customer endpoint including `list()`.

**9. CustomerControllerTest F04 Additions** — Extend the existing `CustomerControllerTest.java` with the 20 new test methods specified in the spec's Testing Strategy section: 6 access-control tests, 8 CRUD happy-path tests, and 6 validation-error tests. Use `@BeforeEach` to seed and clean the database. Form submission tests use `MockMvcRequestBuilders.post(...)` with `.param(...)` chaining and `.with(csrf())`. Access-control tests use `.with(user("...").roles("..."))`.

## Phase 4: Templates

**10. form.html** — Create `src/main/resources/templates/customers/form.html`. Use `th:action` computed from `customer.id` (null → `POST /customers`; present → `POST /customers/{id}`). Add input fields for all nine mandatory fields, assigning the F03-required HTML IDs (`zipCode`, `street`, `neighborhood`, `city`, `state`) to address inputs. Render `th:errors` below each field and a flash `successMessage` toast at the top. For Admin edit mode (`customer.id != null and isAdmin`), include a Delete button that opens an inline HTML/CSS modal overlay — the modal has Cancel (hides modal via vanilla JS) and Confirm (submits a hidden CSRF-bearing form to `POST /customers/{id}/delete`). Add `<script src="/js/cep.js">` before `</body>`.

**11. view.html** — Create `src/main/resources/templates/customers/view.html`. Display all customer fields as read-only text. Show an "Editar" link to `/customers/{id}/edit` only when `isAdmin`. Include a "Voltar para lista" link → `/customers` and a flash `successMessage` display at the top. Apply inline CSS consistent with `login.html` and `list.html`.

**12. 403.html and list.html Updates** — Create `src/main/resources/templates/error/403.html` with an "Acesso Negado" heading, a brief explanation, and a "Voltar para lista" link → `/customers`. Then modify `list.html`: add a flash notification `<div th:if="${successMessage}">` at the top; add a "Novo Cliente" `<a>` button (`th:if="${isAdmin}"`) linking to `/customers/new`; link each table row to `/customers/{id}`.
