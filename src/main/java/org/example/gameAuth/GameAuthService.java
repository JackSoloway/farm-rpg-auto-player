package org.example.gameAuth;

import org.example.gameHttp.GameCookie;
import org.example.user.UserRepository;
import org.springframework.web.server.ResponseStatusException;

import org.example.auth.LoginRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class GameAuthService {
    private static final String _usernameKey = "username";
    private static final String _passwordKey = "password";
    private static final URI _baseUri = URI.create("https://farmrpg.com/");
    private static final URI _loginUri = URI.create("https://farmrpg.com/worker.php?go=login");
    private static final String _cookiePairFormat = "%s=%s";
    private static final String _cookieSeparator = "; ";
    private static final String _successLoginBody = "success";

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository users;

    public GameAuthService(GameSessionRepository gameSessionRepository, UserRepository users) {
        this.gameSessionRepository = gameSessionRepository;
        this.users = users;
    }

    @Transactional
    public GameCookie login(LoginRequest loginRequest, String appUsername) {
        var cookieManager = new CookieManager();

        try (var client = HttpClient.newBuilder().cookieHandler(cookieManager).build()) {

            var initialCookieRequest = browserRequest(_baseUri)
                    .headers(BrowserHeaders._page)
                    .GET()
                    .build();

            client.send(initialCookieRequest, HttpResponse.BodyHandlers.ofString());

            var request = browserRequest(_loginUri)
                    .headers(BrowserHeaders._ajax)
                    .POST(HttpRequest.BodyPublishers.ofString(getRequestBody(loginRequest)))
                    .build();

            var loginResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (!loginResponse.body().equals(_successLoginBody))
                throw new GameAuthFailedException(HttpStatus.UNAUTHORIZED);

            var gameCookie = new GameCookie(extractCookies(cookieManager));
            var session = saveOrUpdateSession(loginRequest.username(), gameCookie);
            var user = users.findByUsername(appUsername).orElseThrow();
            user.setGameSession(session);
            users.save(user);
            return gameCookie;
        } catch (GameAuthFailedException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Game login interrupted");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to log in to game", e);
        }
    }

    private static HttpRequest.@NonNull Builder browserRequest(@NonNull URI uri) {
        return HttpRequest.newBuilder(uri).headers(BrowserHeaders._common);
    }

    private @NonNull String extractCookies(@NonNull CookieManager cookieManager) {
        return cookieManager
                .getCookieStore()
                .get(_baseUri)
                .stream()
                .map(cookie -> String.format(_cookiePairFormat, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(_cookieSeparator));
    }

    private @NonNull String getRequestBody(@NonNull LoginRequest loginRequest) {
        return String.format("%s=%s&%s=%s",
                _usernameKey, encode(loginRequest.username()),
                _passwordKey, encode(loginRequest.password()));
    }

    private static @NonNull String encode(@NonNull String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private GameSession saveOrUpdateSession(String username, GameCookie cookie) {
        var session = gameSessionRepository.findByGameUsername(username)
                .orElseGet(() -> new GameSession(username, cookie));
        session.setCookie(cookie);
        return gameSessionRepository.save(session);
    }
}
