package org.example.gameAuth;

import org.example.gameHttp.GameCookie;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GameCookieConverter implements AttributeConverter<GameCookie, String> {
    @Override
    public String convertToDatabaseColumn(GameCookie gameCookie) {
        return gameCookie.getRawValue();
    }

    @Override
    public GameCookie convertToEntityAttribute(String rawValue) {
        return new GameCookie(rawValue);
    }
}

