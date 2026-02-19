package com.winestoreapp.wineryadminui.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UiAuthFilterIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private SessionTokenStorage storage;

    private UiAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new UiAuthFilter();
        filter.setStorage(storage);
        filter.setPaths(List.of("/actuator", "/login", "/static", "/css", "/js", "/favicon.ico"));

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(filter)
                .build();
    }

    @Test
    void unauthorizedAccess_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/ui/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void managerAccessingOrders_ShouldPass() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(storage.get(session)).thenReturn("valid-token");
        when(storage.getRoles(session)).thenReturn(List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/ui/orders").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void managerAccessingUsers_ShouldBeForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(storage.get(session)).thenReturn("valid-token");
        when(storage.getRoles(session)).thenReturn(List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/ui/users").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccessingUsers_ShouldPass() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(storage.get(session)).thenReturn("valid-token");
        when(storage.getRoles(session)).thenReturn(List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/ui/users").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void publicStaticResource_ShouldAlwaysPass() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestController {
        @GetMapping("/ui/orders")
        public String orders() {
            return "ok";
        }

        @GetMapping("/ui/users")
        public String users() {
            return "ok";
        }

        @GetMapping("/css/style.css")
        public String css() {
            return "ok";
        }
    }
}
