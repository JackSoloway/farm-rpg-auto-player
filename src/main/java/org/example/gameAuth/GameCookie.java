package org.example.gameAuth;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameCookie {
    public static final String header = "Cookie";

    private final String rawValue;
    private Map<String, String> values;

    public GameCookie(String rawValue) {
        this.rawValue = rawValue;
        init(rawValue);
    }

    private void init(String rawValue) {
        if (rawValue == null || rawValue.isEmpty())
            return;

        values = Arrays.stream(rawValue.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> parts[1].trim()
                ));
    }

    public String getRawValue() {
        return rawValue;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameCookie gameCookie && Objects.equals(rawValue, gameCookie.rawValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawValue);
    }
}
