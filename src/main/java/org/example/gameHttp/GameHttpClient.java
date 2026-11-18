package org.example.gameHttp;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** A single-use game request. Transport resources are closed by send(). */
public abstract class GameHttpClient {
    private final HttpRequest request;
    private final AtomicBoolean sent = new AtomicBoolean();

    protected GameHttpClient(HttpRequest.Builder builder, GameCookie cookie) {
        Objects.requireNonNull(cookie, "cookie");
        var rawCookie = cookie.getRawValue();
        if (rawCookie != null && !rawCookie.isBlank()) {
            builder.header(GameCookie.header, rawCookie);
        }

        request = builder.build();
    }

    /**
     * Sends once, including when the attempt fails. HTTP errors are returned as results;
     * transport errors are thrown to the caller. Create a new instance to retry.
     */
    public final GameHttpResult send() throws IOException, InterruptedException {
        if (!sent.compareAndSet(false, true)) {
            throw new IllegalStateException("This game request has already been sent");
        }
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new GameHttpResult(response.statusCode(), response.body());
        }
    }
}
