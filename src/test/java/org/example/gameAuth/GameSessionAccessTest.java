package org.example.gameAuth;

import org.example.gameHttp.GameCookie;
import org.example.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:game-session-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
class GameSessionAccessTest {
    @Autowired GameSessionAccess access;
    @Autowired GameSessionRepository sessions;
    @Autowired UserRepository users;
    @MockitoBean GameSessionVerifier verifier;

    @Test
    void loadsOnlyLinkedCookieAndRequiresSuccessfulLiveVerification() throws Exception {
        var cookie = new GameCookie("session=stored-cookie");
        var saved = sessions.save(new GameSession("game-player", cookie));
        var admin = users.findByUsername("admin").orElseThrow();
        admin.setGameSession(saved);
        users.save(admin);
        when(verifier.valid(cookie)).thenReturn(true);
        assertTrue(access.status("admin").authenticated());
        assertEquals("game-player", access.status("admin").gameUsername());
        assertEquals(cookie, access.requireCookie("admin"));
        assertFalse(access.status("player").authenticated());
        assertEquals(428, assertThrows(ResponseStatusException.class, () -> access.requireCookie("player")).getStatusCode().value());
        when(verifier.valid(cookie)).thenReturn(false);
        assertFalse(access.status("admin").authenticated());
        assertEquals(428, assertThrows(ResponseStatusException.class, () -> access.requireCookie("admin")).getStatusCode().value());
        when(verifier.valid(cookie)).thenThrow(new IOException("offline"));
        assertEquals(502, assertThrows(ResponseStatusException.class, () -> access.requireCookie("admin")).getStatusCode().value());
        assertEquals(cookie, users.findByUsername("admin").orElseThrow().getGameSession().getCookie());
    }
}
