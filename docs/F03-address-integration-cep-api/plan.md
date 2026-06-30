# Implementation Plan: F03 - Address Integration (CEP API)

## Prerequisites

- F01 (Authentication System) implemented and tests passing
- Docker running (Testcontainers requires Docker daemon for `CepControllerTest`)
- `V2__create_customers.sql` (F02) does not need to be migrated yet — F03 introduces no schema changes

## Phase 1: Backend

**1. CepAddress Record** — Create `CepAddress.java` in `br.com.example.sdd.customers.cep` as a Java record with four `String` fields: `street`, `neighborhood`, `city`, and `state`. This is the JSON response type returned by the endpoint and consumed by `cep.js`.

**2. CepService** — Create `CepService.java` in the `cep` package. Inject a `RestClient` bean configured with the ViaCEP base URL. Implement `Optional<CepAddress> lookup(String cep)` that calls ViaCEP's `/ws/{cep}/json/` path and maps `logradouro`, `bairro`, `localidade`, and `uf` to a `CepAddress`. Return `Optional.empty()` when ViaCEP responds with `"erro": true` or any HTTP error, letting the controller handle the 404 translation.

**3. CepController** — Create `CepController.java` with `@GetMapping("/api/cep/{cep}")`. Strip the hyphen from the path variable, validate that exactly 8 digits remain (returning `400` otherwise), delegate to `CepService.lookup()`, and return `200 + CepAddress` as JSON or `404` when the optional is empty. No `SecurityConfig` changes are required — the endpoint is covered by the existing `anyRequest().authenticated()` rule.

**4. CepService Tests** — Write `CepServiceTest.java` without a Spring context. Use `MockRestServiceServer.bindTo(restClient.builder())` to intercept outbound HTTP. Cover three scenarios: successful lookup with full field mapping, ViaCEP returning `{"erro":"true"}` producing `Optional.empty()`, and a ViaCEP HTTP 500 also producing `Optional.empty()` without throwing.

**5. CepController Tests** — Write `CepControllerTest.java` with `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)` + `@MockitoBean CepService` + MockMvc. Cover: valid unmasked CEP returns 200 with JSON body, masked CEP is normalized before reaching the service, too-short and non-numeric inputs return 400, nine-digit input returns 400, service returning empty produces 404, and unauthenticated request redirects to `/login`.

## Phase 2: Frontend

**6. cep.js Static File** — Create `src/main/resources/static/js/cep.js`. Attach an `input` listener to the element with `id="zipCode"`. Extract raw digits and apply the `NNNNN-NNN` mask when digit count reaches 5. When digit count reaches exactly 8, show a spinner on the address fields and call `fetch('/api/cep/' + digits)`. On a 200 response, parse the JSON and fill the elements with `id="street"`, `id="neighborhood"`, `id="city"`, and `id="state"`. On any non-200 response or network error, remove the spinner and leave the address fields editable. F04's template must assign the agreed IDs to its inputs and include this file via `<script src="/js/cep.js"></script>`.
