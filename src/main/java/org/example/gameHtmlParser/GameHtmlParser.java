package org.example.gameHtmlParser;

import java.util.Objects;
import org.example.gameHttp.GameHttpResult;
import org.example.stringParser.ParseResult;
import org.example.stringParser.StringParser;
import org.example.stringParser.StringTemplate;

/** Parses response bodies independently of HTTP status codes. */
public final class GameHtmlParser<T> {
    private final StringParser<T> stringParser;

    public GameHtmlParser(Class<T> targetType) {
        this.stringParser = new StringParser<>(targetType);
    }

    public ParseResult<T> tryGetValue(GameHttpResult html, StringTemplate template, String valueRegex) {
        Objects.requireNonNull(html, "html");
        return stringParser.tryGetValue(html.getBody(), template, valueRegex);
    }
}
