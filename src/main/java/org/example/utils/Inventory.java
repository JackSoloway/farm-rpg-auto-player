package org.example.utils;

import org.example.gameHtmlParser.GameHtmlParser;
import org.example.gameHttp.GameHttpClientGet;
import org.example.gameHttp.GameCookie;
import org.example.stringParser.StringTemplate;

import java.io.IOException;

public class Inventory {
    private static final String inventorySizeTemplate = "https://farmrpg.com/inventory.php";

    public int getInventorySize(GameCookie gameCookie) throws IOException, InterruptedException {
        var client = new GameHttpClientGet(java.net.URI.create(inventorySizeTemplate), gameCookie);
        var response = client.send();
        var htmlParser = new GameHtmlParser<>(Integer.class);
        var parseResult = htmlParser.tryGetValue(response, new StringTemplate("you cannot have more than <strong>***</strong> of any"), "[0-9.,]+");
        return parseResult.success() ? parseResult.value() : 0;
    }
}
