package org.example.gameAuth;

import org.example.gameHttp.GameCookie;
import org.example.gameHttp.GameHttpClientGet;
import org.example.gameHtmlParser.GameHtmlParser;
import org.example.stringParser.StringTemplate;
import org.example.utils.GamePageURI;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;

@Component
public class GameSessionVerifier {
    public boolean valid(GameCookie cookie) throws IOException, InterruptedException {
        if (cookie == null || cookie.getRawValue() == null || cookie.getRawValue().isBlank()) return false;
        var response = new GameHttpClientGet(GamePageURI.INVENTORY_URI, cookie, Duration.ofSeconds(20)).send();
        if (response.statusCode() >= 300 && response.statusCode() < 400
                || response.statusCode() == 401 || response.statusCode() == 403) return false;
        if (!response.success()) throw new IOException("Inventory returned HTTP " + response.statusCode());
        return new GameHtmlParser<>(Integer.class).tryGetValue(response,
                new StringTemplate("you cannot have more than <strong>***</strong> of any"), "(?s:.*?)").found();
    }
}
