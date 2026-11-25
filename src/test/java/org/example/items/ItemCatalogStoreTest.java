package org.example.items;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:item-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
class ItemCatalogStoreTest {
    @Autowired ItemCatalogStore store;
    @Autowired ItemRepository repository;

    @Test
    void repeatImportKeepsPrimaryKeyAndImageAndUpdatesName() {
        var uri = URI.create("https://farmrpg.com/img/items/one.png");
        var first = store.update(List.of(new ItemPageParser.ParsedItem(123, "Old", uri)));
        assertEquals(1, first.added());
        var id = repository.findByItemId(123).orElseThrow().getId();
        store.saveImage(123, "123.png");
        var second = store.update(List.of(new ItemPageParser.ParsedItem(123, "New", uri)));
        assertEquals(0, second.added());
        assertEquals(1, second.updated());
        assertEquals(1, repository.count());
        var saved = repository.findByItemId(123).orElseThrow();
        assertEquals(id, saved.getId());
        assertEquals("New", saved.getItemName());
        assertEquals("123.png", saved.getImgSrc());
    }
}
