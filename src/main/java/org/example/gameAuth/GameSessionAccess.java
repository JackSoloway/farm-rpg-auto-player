package org.example.gameAuth;

import org.example.gameHttp.GameCookie;
import org.example.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

@Service
public class GameSessionAccess {
    private final UserRepository users;
    private final GameSessionVerifier verifier;

    public GameSessionAccess(UserRepository users, GameSessionVerifier verifier) {
        this.users = users;
        this.verifier = verifier;
    }

    public record Status(boolean authenticated, String gameUsername) {}

    public Status status(String username) {
        var session = verified(username);
        return new Status(session != null, session == null ? null : session.getGameUsername());
    }

    public GameCookie requireCookie(String username) {
        var session = verified(username);
        if (session == null) throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "Log in to Farm RPG first");
        return session.getCookie();
    }

    private GameSession verified(String username) {
        var user = users.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var session = user.getGameSession();
        if (session == null) return null;
        try {
            return verifier.valid(session.getCookie()) ? session : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Game session check interrupted");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to verify game session");
        }
    }
}
