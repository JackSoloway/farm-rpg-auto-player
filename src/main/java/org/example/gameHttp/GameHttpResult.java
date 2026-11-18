package org.example.gameHttp;

/** HTTP success means a 2xx status; game-specific response bodies need separate validation. */
public record GameHttpResult(int statusCode, String body) {
    public boolean success() {
        return statusCode >= 200 && statusCode < 300;
    }
}
