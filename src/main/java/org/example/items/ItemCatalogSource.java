package org.example.items;

import org.example.gameHttp.GameCookie;
import org.example.gameHttp.GameHttpClientGet;
import org.example.gameHttp.GameHttpResult;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Component
public class ItemCatalogSource {
    public String load(GameCookie cookie) throws IOException, InterruptedException {
        var response = new GameHttpClientGet(
                URI.create("https://farmrpg.com/wiki.php?page=Everything+2"),
                cookie, Duration.ofSeconds(45)).send();
        return bodyForParsing(response);
    }

    static String bodyForParsing(GameHttpResult response) throws IOException {
        if (response.statusCode() == HttpStatus.UNAUTHORIZED.value() || response.statusCode() == HttpStatus.FORBIDDEN.value()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_REQUIRED, "Game session is no longer valid; log in again");
        }
        // This wiki endpoint can include the complete item list in a 302 response.
        // Preserve that body instead of following Location; ItemPageParser validates it before any DB writes.
        if (!response.success() && response.statusCode() != HttpStatus.FOUND.value()) {
            throw new IOException("Wiki returned HTTP " + response.statusCode());
        }
        return response.body();
    }
}
