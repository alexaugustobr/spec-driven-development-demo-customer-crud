# Implementation Plan: F01. Authentication System

**Prerequisites:**
- Java 25 LTS installed (`java -version` confirms 25.x)
- Gradle 8.x installed or Gradle Wrapper will be used
- PostgreSQL running locally (default port 5432)
- Database `customers_db` created with a user that has DDL privileges
- Environment variables (or `application.properties` values) ready: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

---

### Stage 1: Project Scaffolding

**1. Gradle Build File and Dependencies** - Create `build.gradle` with the Spring Boot 3.5.x plugin and all required dependencies: Spring Web, Spring Security, Thymeleaf, `thymeleaf-extras-springsecurity6`, Spring Data JPA, PostgreSQL driver, and Lombok. Set the Java source compatibility to 25. Configure the `settings.gradle` with the project name `customers`.

**2. Application Entry Point and Configuration** - Create the `CustomersApplication.java` main class and `application.properties` with the PostgreSQL data source URL, JPA DDL auto mode, session timeout (2 hours), and SQL init mode set to `always`. See spec Section 4 for the full list of required properties.

---

### Stage 2: User Domain and Seed Data

**3. User Entity and Role Enum** - Create the `Role` enum with `ADMIN` and `ATTENDANT` values, then create the `User` JPA entity mapped to the `users` table. The entity must implement `UserDetails` so Spring Security can consume it directly. Refer to spec Section 6 for the full column list, types, and constraints.

**4. User Repository** - Create `UserRepository` as a `JpaRepository<User, Long>` with a single derived query method to look up a user by email. This is the only data access method required by the authentication flow.

**5. Seed Data File** - Create `data.sql` under `src/main/resources` with two INSERT statements for the default admin and attendant users. Generate BCrypt hashes at strength 10 for the seed passwords before committing the file. See spec Section 6 for the seed SQL template.

---

### Stage 3: Security Configuration

**6. UserDetailsService** - Create `UserDetailsServiceImpl` implementing Spring Security's `UserDetailsService`. It must delegate to `UserRepository.findByEmail`, throw `UsernameNotFoundException` when the user is absent, and return the `User` entity directly (which already implements `UserDetails`).

**7. Spring Security Config** - Create `SecurityConfig` as a `@Configuration` class that defines the `SecurityFilterChain` bean. Configure: public access for `/login` and static resources; authentication required for all other paths; form login with custom login page, success URL, and failure URL; logout redirecting to `/login?logout` with session invalidation; session fixation protection; and a 2-hour session timeout. See spec Sections 3 and 5 for behavioral details.

---

### Stage 4: Web Layer and Templates

**8. Controllers** - Create `AuthController` to handle `GET /login` (returning the login view, or redirecting to `/dashboard` if already authenticated) and `DashboardController` to handle `GET /dashboard` (returning the dashboard view). The security config enforces authentication; the controller only handles the view logic.

**9. Base Layout Template** - Create `src/main/resources/templates/layout/base.html` as the shared Thymeleaf layout with Bootstrap 5 loaded via CDN, a common `<head>` section, a navigation bar placeholder, and a `layout:fragment="content"` insertion point for child templates.

**10. Login Page Template** - Create `login.html` as a standalone page (not extending the base layout). Include a Bootstrap 5 centered card with the login form, conditional error and logout alert blocks driven by `th:if="${param.error}"` and `th:if="${param.logout}"`, a show/hide password toggle using inline vanilla JS, and a submit button that disables itself and shows a spinner on form submit. See spec Section 5 for the form field names required by Spring Security (`username`, `password`).

**11. Dashboard Stub Template** - Create `src/main/resources/templates/dashboard/index.html` extending the base layout. Display the authenticated user's email and role using Thymeleaf Security Extras (`sec:authentication`). This page is a placeholder that F02 will populate with the customer list.
