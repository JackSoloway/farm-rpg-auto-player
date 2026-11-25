package org.example.items;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ItemCatalogStore {
    private final ItemRepository repository;

    public ItemCatalogStore(ItemRepository repository) { this.repository = repository; }

    public record Counts(int found, int added, int updated) {}

    @Transactional
    public Counts update(List<ItemPageParser.ParsedItem> parsed) {
        var existing = repository.findAll().stream().collect(Collectors.toMap(Item::getItemId, Function.identity()));
        int added = 0;
        int updated = 0;
        for (var entry : parsed) {
            var item = existing.get(entry.itemId());
            if (item == null) {
                repository.save(new Item(entry.itemId(), entry.itemName()));
                added++;
            } else if (!item.getItemName().equals(entry.itemName())) {
                item.setItemName(entry.itemName());
                updated++;
            }
        }
        return new Counts(parsed.size(), added, updated);
    }

    @Transactional
    public void saveImage(long itemId, String path) {
        var item = repository.findByItemId(itemId).orElseThrow();
        item.setImgSrc(path);
    }
}
