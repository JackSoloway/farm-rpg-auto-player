package org.example.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPageParserTest {
    private final ItemPageParser parser = new ItemPageParser();

    @Test
    void parsesAttributeVariantsEntitiesAndDuplicates() {
        String html = """
                <a href="item.php?id=123" class="close-panel"><img src='/img/items/blackcloak.png'
                  alt="Black &amp; White" class='itemimgsm'></a><span style="display:none">anything</span>
                <a class='close-panel' href='item.php?id=123'><img class='itemimgsm' alt='Black &amp; White'
                  src='/img/items/blackcloak.png'></a>
                """;
        var items = parser.parse(html);
        assertEquals(1, items.size());
        assertEquals(123, items.getFirst().itemId());
        assertEquals("Black & White", items.getFirst().itemName());
        assertEquals("https://farmrpg.com/img/items/blackcloak.png", items.getFirst().imageUri().toString());
    }

    @Test
    void rejectsEmptyLoginPageAndPageWithoutValidItems() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("<html>Please log in</html>"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                <a class="close-panel" href="item.php?id=1"><img class="itemimgsm" alt="Test"
                  src="https://example.com/image.png"></a>
                """));
    }

    @Test
    void acceptsImageOutsideItemsDirectory() {
        var items = parser.parse(entry("871", "Captured Ghost", "/img/ghost.png"));
        assertEquals(871, items.getFirst().itemId());
        assertEquals("https://farmrpg.com/img/ghost.png", items.getFirst().imageUri().toString());
    }

    @Test
    void skipsInvalidEntriesAndConflictingDuplicatesWithoutLosingValidItems() {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ItemPageParser.class);
        var appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            var items = parser.parse(
                    entry("871", "Captured Ghost", "/img/ghost.png")
                    + entry("bad", "Bad ID", "/img/example.png")
                    + entry("999999999999999999999999", "Overflow", "/img/example.png")
                    + entry("5", "Broken URI", "/img/bad image.png")
                    + entry("6", "", "/img/example.png")
                    + entry("7", "Missing image", "")
                    + entry("8", "Foreign host", "https://example.com/icon.png")
                    + entry("871", "Conflicting name", "/img/ghost.png")
                    + entry("123", "Last valid item", "/img/items/blackcloak.png"));
            assertEquals(java.util.List.of(871L, 123L), items.stream().map(ItemPageParser.ParsedItem::itemId).toList());
            assertEquals("Captured Ghost", items.getFirst().itemName());
            assertEquals(7, appender.list.size());
            String message = appender.list.getFirst().getFormattedMessage();
            assertTrue(message.contains("itemId=bad"));
            assertTrue(message.contains("itemName=Bad ID"));
            assertTrue(message.contains("imgSrc=/img/example.png"));
            assertTrue(message.contains("reason=Invalid item link"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private static String entry(String id, String name, String src) {
        return "<a class='close-panel' href='item.php?id=" + id
                + "'><img class='itemimgsm' alt='" + name + "' src='" + src + "'></a>";
    }
}
