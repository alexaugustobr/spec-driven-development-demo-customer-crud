# Implementation Plan: F02 - Customer Listing and Search

## Prerequisites

- F01 (Authentication System) implemented and tests passing
- Docker running (Testcontainers requires Docker daemon for integration tests)
- `V1__create_users.sql` migration verified to run clean on startup

## Phase 1: Data Layer

**1. Customer Entity** — Create `Customer.java` in the `br.com.example.sdd.customers.customer` package. Apply Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, `@Builder`, and JPA annotations (`@Entity`, `@Table(name = "customers")`) matching the V2 migration. Include all fields: `id`, `name`, `email`, `document`, `phone`, `zip_code`, `street`, `neighborhood`, `city`, `state`, `createdAt` with `OffsetDateTime`.

**2. Flyway Migration** — Create `V2__create_customers.sql` defining the `customers` table with all columns, `NOT NULL` constraints, `TIMESTAMPTZ DEFAULT NOW()` for `created_at`, and `UNIQUE` constraints on `email` and `document`. Verify the migration applies cleanly on next application startup.

**3. Customer Repository** — Create `CustomerRepository` extending `JpaRepository<Customer, Long>`. Declare `findByDocument(String document, Pageable pageable)`, `findByNameContainingIgnoreCase(String name, Pageable pageable)`, and `findByEmailContainingIgnoreCase(String email, Pageable pageable)` as Spring Data derived query methods.

**4. Repository Tests** — Write `CustomerRepositoryTest` with `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)`. Cover `findAll(Pageable)` for page size and total count, each finder method for correctness and case-insensitivity, and sort ordering by name (ASC) and `createdAt` (DESC). Each test method seeds its own customers via the repository.

## Phase 2: Controller

**5. Customer Controller** — Create `CustomerController` with `@GetMapping("/customers")`. Accept `q` (String), `sort` (String), `dir` (String), and `page` (int, default 0) as request params. Validate `sort` against an allowlist `{name, createdAt}`, defaulting to `createdAt`; validate `dir` to `{asc, desc}`, defaulting to `desc`. Auto-detect search mode from `q` (blank → `findAll`; 11 or 14 all-digit chars → `findByDocument`; contains `@` → `findByEmailContainingIgnoreCase`; else → `findByNameContainingIgnoreCase`). Build `PageRequest.of(page, 20, Sort.by(direction, sortField))`. Add `page`, `q`, `sort`, `dir` to the model and return `"customers/list"`.

**6. Controller Tests (access and routing)** — Write `CustomerControllerTest` covering unauthenticated redirect to `/login`, authenticated access for both ADMIN and ATTENDANT roles, view name assertion (`customers/list`), and the default no-params list load.

**7. Controller Tests (search, sort, pagination)** — Extend `CustomerControllerTest` with seed-data scenarios: partial name match with mixed case, CPF exact match (11 digits), CNPJ exact match (14 digits), email partial match, no-match empty page, name sort ascending, default `createdAt` desc sort, and second-page navigation (`?page=1` with 25 seeded records).

## Phase 3: UI

**8. List Template** — Create `templates/customers/list.html`. Include a `<form method="get" action="/customers">` search bar with a single `<input name="q">` preserving the current value via `th:value`. Add a `<table>` with `<th>` anchor links for Name and Date of Registration: each link sets `sort` to its field, toggles `dir` to the opposite of the current direction when the column is already sorted, and resets `page=0` while preserving `q`. Render `<tbody>` rows from `th:each="${page.content}"`. Match the visual style of `login.html` (inline CSS, system font stack, card/table border layout).

**9. Empty State** — Inside `list.html`, add a `<div th:if="${page.totalElements == 0}">` section displaying a "Nenhum cliente encontrado" message, shown only when the result page is empty. The section replaces the table body or sits below the table header.

**10. Pagination Controls** — At the bottom of `list.html`, render a navigation row using `page.totalPages`, `page.number`, `page.hasPrevious`, and `page.hasNext`. Each page-number link and the Previous/Next anchors must preserve `q`, `sort`, and `dir` as query params while updating only `page`. Show page numbers as a compact range (e.g., current ± 2 pages) when `totalPages` is large.
