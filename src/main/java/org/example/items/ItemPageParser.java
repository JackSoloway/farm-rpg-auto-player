package org.example.items;

import org.jsoup.Jsoup;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ItemPageParser {
    private static final Logger log = LoggerFactory.getLogger(ItemPageParser.class);
    private static final Pattern ITEM_LINK = Pattern.compile("^/?item\\.php\\?id=(\\d+)$");
    private static final URI BASE = URI.create("https://farmrpg.com");

    public record ParsedItem(long itemId, String itemName, URI imageUri) {}

    public List<ParsedItem> parse(String html) {
        var items = new LinkedHashMap<Long, ParsedItem>();
        for (var anchor : Jsoup.parse(html).select("a.close-panel[href]:has(img.itemimgsm)")) {
            String href = anchor.attr("href");
            var image = anchor.selectFirst("img.itemimgsm");
            String name = image.attr("alt").trim();
            String src = image.attr("src").trim();
            String rawId = href.contains("id=") ? href.substring(href.indexOf("id=") + 3) : "unknown";
            try {
                var match = ITEM_LINK.matcher(href);
                if (!match.matches()) {
                    throw new IllegalArgumentException("Invalid item link");
                }

                long id = Long.parseLong(match.group(1));
                var item = getItem(id, name, src);
                var previous = items.putIfAbsent(id, item);

                if (previous != null && !previous.equals(item)) {
                    throw new IllegalArgumentException("Conflicting duplicate; keeping the first valid entry");
                }
            } catch (IllegalArgumentException e) {
                log.warn("Skipping item: itemId={}, itemName={}, imgSrc={}, href={}, reason={}",
                        rawId, name, src, href, e.getMessage());
            }
        }
        if (items.isEmpty()) throw new IllegalArgumentException("No items found in the wiki page");
        return List.copyOf(items.values());
    }

    private static @NonNull ParsedItem getItem(long id, String name, String src) {
        if (id <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }

        if (name.isBlank() || name.length() > 512) {
            throw new IllegalArgumentException("Item name is empty or exceeds 512 characters");
        }

        if (src.isBlank()) {
            throw new IllegalArgumentException("Image path is empty");
        }

        URI uri = BASE.resolve(src).normalize();

        if (!"https".equals(uri.getScheme()) || !"farmrpg.com".equals(uri.getHost())
                || uri.getPort() != -1 || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Image must be hosted on https://farmrpg.com");
        }

        return new ParsedItem(id, name, uri);
    }
}
