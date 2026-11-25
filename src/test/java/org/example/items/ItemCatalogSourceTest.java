package org.example.items;

import org.example.gameHttp.GameHttpResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ItemCatalogSourceTest {
    private static final String ITEMS = """
            <a href="item.php?id=123" class="close-panel"><img src='/img/items/blackcloak.png'
                alt="Black Cloak" class='itemimgsm'></a><span style="display:none">anything</span>
            """;

    @Test
    void preservesAndParsesCatalogIn302Body() throws Exception {
        String body = ItemCatalogSource.bodyForParsing(new GameHttpResult(302, ITEMS));
        assertEquals(ITEMS, body);
        assertEquals(123, new ItemPageParser().parse(body).getFirst().itemId());
    }

    @Test
    void redirectWithoutItemsDoesNotPassCatalogValidation() throws Exception {
        for (String html : new String[]{"", "<html><form>Log in</form></html>"}) {
            String body = ItemCatalogSource.bodyForParsing(new GameHttpResult(302, html));
            assertThrows(IllegalArgumentException.class, () -> new ItemPageParser().parse(body));
        }
    }

    @Test
    void otherHttpErrorsAreNotAcceptedEvenWithItemsInBody() {
        for (int status : new int[]{301, 304, 404, 429, 500}) {
            assertThrows(IOException.class, () -> ItemCatalogSource.bodyForParsing(new GameHttpResult(status, ITEMS)));
        }
        for (int status : new int[]{401, 403}) {
            assertEquals(428, assertThrows(ResponseStatusException.class,
                    () -> ItemCatalogSource.bodyForParsing(new GameHttpResult(status, ITEMS))).getStatusCode().value());
        }
    }
}
