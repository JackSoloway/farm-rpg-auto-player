package org.example.utils;

import org.example.gameHtmlParser.GameHtmlParser;
import org.example.gameHttp.GameHttpClientGet;
import org.example.gameHttp.GameCookie;
import org.example.stringParser.StringTemplate;

import java.io.IOException;

public class Inventory {
    private static final String inventorySizeTemplate = "you cannot have more than <strong>***</strong> of any";

    public int getInventorySize(GameCookie gameCookie) throws IOException, InterruptedException {
        var client = new GameHttpClientGet(GamePages.INVENTORY_URI, gameCookie);
        var response = client.send();
        if (!response.success()) {
            throw new IOException("Failed to retrieve inventory: HTTP " + response.statusCode());
        }

        var htmlParser = new GameHtmlParser<>(Integer.class);
        var parseResult = htmlParser.tryGetValue(response, new StringTemplate(inventorySizeTemplate), "(?s:.*?)");
        if (!parseResult.found()) {
            throw new IllegalStateException("Inventory size was not found in the server response");
        }

        return parseResult.value();
    }
}
