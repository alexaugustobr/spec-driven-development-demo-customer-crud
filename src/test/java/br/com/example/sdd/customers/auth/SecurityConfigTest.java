package br.com.example.sdd.customers.auth;

import br.com.example.sdd.customers.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void unauthenticatedAccessToProtectedRouteRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageIsPublicAndReturnsOk() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedAdminCanAccessProtectedRoute() throws Exception {
        mockMvc.perform(get("/customers").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(302));
    }

    @Test
    void authenticatedAttendantCanAccessProtectedRoute() throws Exception {
        mockMvc.perform(get("/customers").with(user("attendant@example.com").roles("ATTENDANT")))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(302));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
