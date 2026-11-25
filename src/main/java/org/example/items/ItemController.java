package org.example.items;

import org.example.gameAuth.GameSessionAccess;
import java.security.Principal;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.TimeUnit;

@RestController
public class ItemController {
    private final ItemRefreshService service;
    private final ItemRepository repository;
    private final ItemImageStorage images;
    private final GameSessionAccess sessions;

    public ItemController(ItemRefreshService service, ItemRepository repository, ItemImageStorage images,
                          GameSessionAccess sessions) {
        this.service = service;
        this.repository = repository;
        this.images = images;
        this.sessions = sessions;
    }

    @GetMapping("/api/items/refresh/status")
    public ResponseEntity<ItemRefreshService.ButtonState> status(Principal principal) {
        sessions.requireCookie(principal.getName());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.state());
    }

    @PostMapping("/api/items/refresh")
    public ItemCatalogStore.Counts refresh(Principal principal) {
        return service.refresh(sessions.requireCookie(principal.getName()));
    }

    @GetMapping("/api/items/{itemId}/icon")
    public ResponseEntity<Resource> icon(@PathVariable long itemId) {
        var item = repository.findByItemId(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!images.exists(item.getImgSrc())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var file = new FileSystemResource(images.resolve(item.getImgSrc()));
        var type = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(file);
    }
}
