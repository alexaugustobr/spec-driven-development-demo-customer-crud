# Implementation Plan: F01 - Authentication System

**Prerequisites:**
- Docker running locally (Postgres container via `compose.yaml`)
- `build.gradle`: add `implementation 'org.flywaydb:flyway-core'` and `runtimeOnly 'org.flywaydb:flyway-database-postgresql'`
- `application.yaml`: configure `spring.datasource`, `spring.jpa.open-in-view: false`, `spring.flyway.enabled: true`, and `server.servlet.session.timeout: 7200s`

---

### Stage 1: Data Foundation

**1. Flyway Migration** - Create the `V1__create_users.sql` migration file under `src/main/resources/db/migration/`. This defines the `users` table schema and runs automatically on application startup, establishing the database contract that all other auth components depend on. See spec Section 6 for the complete DDL.

**2. User Entity and Repository** - Create the `User` JPA entity and `UserRepository` interface in the `auth` package. The entity maps to the `users` table and declares the nested `Role` enum. See spec Section 4 for file paths and Section 6 for the data model.

---

### Stage 2: Security Core

**3. UserDetailsService** - Implement `UserDetailsServiceImpl` in the `auth` package to load a `User` by email from the repository and adapt it to Spring Security's `UserDetails` contract, mapping the `Role` enum to a `GrantedAuthority`. See spec Section 4 for responsibilities.

**4. Security Configuration** - Create `SecurityConfig` in the `auth` package to configure Spring Security's form login, success and failure redirect URLs, session timeout, CSRF, the `BCryptPasswordEncoder` bean, and the public/protected route rules. See spec Sections 4 and 5 for the full configuration scope.

**5. DataInitializer** - Implement the `ApplicationRunner` in the `auth` package that checks on startup whether the `users` table is empty and, if so, creates one `ADMIN` and one `ATTENDANT` user with bcrypt-encoded passwords. Log both emails and plain-text passwords at `INFO` level so developers can log in immediately. See spec Section 4 for behavior details.

---

### Stage 3: Presentation Layer

**6. Login Controller** - Create `LoginController` in the `auth` package with a single `GET /login` handler that returns the `auth/login` Thymeleaf view name. Spring Security's filter chain handles `POST /login` natively; no controller action is needed for it.

**7. Login Page** - Create `src/main/resources/templates/auth/login.html` with an email field, a password field with a client-side show/hide toggle, a submit button that shows a loading state on click, and a conditional error banner displayed when the `?error` query parameter is present. Use Thymeleaf's `th:action` so the CSRF token is injected automatically. See spec Section 5 for the complete field and error message requirements.
