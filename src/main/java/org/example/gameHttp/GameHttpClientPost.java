package org.example.gameHttp;

import java.net.URI;
import java.net.http.HttpRequest;

public final class GameHttpClientPost extends GameHttpClient {
    public GameHttpClientPost(URI uri, GameCookie cookie) {
        super(HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()), cookie);
    }

    public GameHttpClientPost(URI uri, GameCookie cookie, String body) {
        super(HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(body)), cookie);
    }
}
