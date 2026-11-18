package org.example.gameHttp;

import java.net.URI;
import java.net.http.HttpRequest;

public final class GameHttpClientGet extends GameHttpClient {
    public GameHttpClientGet(URI uri, GameCookie cookie) {
        super(HttpRequest.newBuilder(uri).GET(), cookie);
    }
}
