package org.example.items;

import jakarta.annotation.PreDestroy;
import org.example.gameHttp.GameCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ItemRefreshService {
    private static final Logger log = LoggerFactory.getLogger(ItemRefreshService.class);
    private final ItemCatalogSource source;
    private final ItemPageParser parser;
    private final ItemCatalogStore store;
    private final ItemRepository repository;
    private final ItemImageStorage images;
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("item-icons").factory());
    private volatile DownloadJob job;

    private static class DownloadJob {
        volatile boolean stop;
        Future<?> future;
    }

    public record ButtonState(String status, int delay) {}

    public ItemRefreshService(ItemCatalogSource source, ItemPageParser parser, ItemCatalogStore store,
                              ItemRepository repository, ItemImageStorage images) {
        this.source = source;
        this.parser = parser;
        this.store = store;
        this.repository = repository;
        this.images = images;
    }

    public ButtonState state() {
        return refreshing.get() ? new ButtonState("LOCKED", 2) : new ButtonState("UNLOCKED", 10);
    }

    public ItemCatalogStore.Counts refresh(GameCookie cookie) {
        if (!refreshing.compareAndSet(false, true)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item database refresh is already running");
        }
        try {
            var previous = job;
            if (previous != null) {
                previous.stop = true;
                // Do not cancel/interrupt: the current file and its DB path must be committed first.
                previous.future.get();
            }
            var parsed = parser.parse(source.load(cookie));
            var counts = store.update(parsed);
            var next = new DownloadJob();
            next.future = executor.submit(() -> download(parsed, next));
            job = next;
            return counts;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Item refresh interrupted");
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Unable to load item catalog", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to load item catalog");
        } catch (ExecutionException e) {
            log.error("Previous image download failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Previous image download failed");
        } finally {
            refreshing.set(false);
        }
    }

    private void download(List<ItemPageParser.ParsedItem> parsed, DownloadJob current) {
        for (var entry : parsed) {
            if (current.stop) break;
            try {
                var item = repository.findByItemId(entry.itemId()).orElseThrow();
                if (images.exists(item.getImgSrc())) continue;
                String path = images.download(entry);
                store.saveImage(entry.itemId(), path);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Unable to save icon for item {}: {}", entry.itemId(), e.toString());
            }
        }
    }

    @PreDestroy
    public void close() {
        var current = job;
        if (current != null) current.stop = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(40, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
