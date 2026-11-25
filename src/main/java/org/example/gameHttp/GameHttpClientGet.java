package org.example.gameHttp;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

public final class GameHttpClientGet extends GameHttpClient {
    public GameHttpClientGet(URI uri, GameCookie cookie) {
        super(HttpRequest.newBuilder(uri).GET(), cookie);
    }

    public GameHttpClientGet(URI uri, GameCookie cookie, Duration timeout) {
        super(HttpRequest.newBuilder(uri).timeout(timeout).GET(), cookie);
    }
}
