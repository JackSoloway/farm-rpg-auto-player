package org.example.gameAuth;

import org.example.gameHttp.GameCookie;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "game_sessions")
public class GameSession {
    private static final int _gameUsernameLength = 64;
    private static final int _cookieLength = 2048;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(nullable = false, unique = true, length = _gameUsernameLength)
    private String gameUsername;

    @Convert(converter = GameCookieConverter.class)
    @Column(nullable = false, length = _cookieLength)
    private GameCookie cookie;

    protected GameSession() {}

    public GameSession(String gameUsername, GameCookie cookie) {
        this.gameUsername = gameUsername;
        this.cookie = cookie;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getGameUsername() {
        return gameUsername;
    }

    public GameCookie getCookie() {
        return cookie;
    }

    public void setCookie(GameCookie cookie) {
        this.cookie = cookie;
    }
}

