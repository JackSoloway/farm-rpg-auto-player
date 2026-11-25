package org.example.items;

import org.example.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:item-api-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
class ItemControllerTest {
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy security;
    @Autowired JwtService jwt;
    @Autowired UserDetailsService users;
    @MockitoBean ItemRefreshService service;
    @MockitoBean org.example.gameAuth.GameSessionAccess sessions;

    @Test
    void adminCanReadStateAndRefreshWithBearerButPlayerCannot() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
        String admin = "Bearer " + jwt.createToken(users.loadUserByUsername("admin"));
        String player = "Bearer " + jwt.createToken(users.loadUserByUsername("player"));
        when(service.state()).thenReturn(new ItemRefreshService.ButtonState("LOCKED", 2));
        var cookie = new org.example.gameHttp.GameCookie("session=admin-game");
        when(sessions.requireCookie("admin")).thenReturn(cookie);
        when(service.refresh(cookie)).thenReturn(new ItemCatalogStore.Counts(10, 1, 2));
        mvc.perform(get("/api/items/refresh/status").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("LOCKED"))
                .andExpect(jsonPath("$.delay").value(2));
        mvc.perform(post("/api/items/refresh").servletPath("/api/items/refresh").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.found").value(10));
        mvc.perform(post("/api/items/refresh").servletPath("/api/items/refresh").header("Authorization", player))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/items/refresh/status").header("Authorization", player))
                .andExpect(status().isForbidden());
        verify(service, times(1)).refresh(cookie);
    }

    @Test
    void missingGameSessionPreventsRefreshEvenForAdmin() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
        String admin = "Bearer " + jwt.createToken(users.loadUserByUsername("admin"));
        when(sessions.requireCookie("admin")).thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.PRECONDITION_REQUIRED));
        mvc.perform(post("/api/items/refresh").servletPath("/api/items/refresh").header("Authorization", admin))
                .andExpect(status().is(428));
        verifyNoInteractions(service);
    }
}
