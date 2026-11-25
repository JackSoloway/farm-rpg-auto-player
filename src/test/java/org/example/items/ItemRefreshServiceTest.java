package org.example.items;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemRefreshServiceTest {
    private static final org.example.gameHttp.GameCookie COOKIE = new org.example.gameHttp.GameCookie("session=test");
    private static final String HTML = """
            <a class="close-panel" href="item.php?id=1"><img class="itemimgsm" alt="One" src="/img/items/one.png"></a>
            <a class="close-panel" href="item.php?id=2"><img class="itemimgsm" alt="Two" src="/img/items/two.png"></a>
            """;

    @Test
    void restartWaitsForCurrentFileAndCommitsItBeforeReloading() throws Exception {
        var source = mock(ItemCatalogSource.class);
        var store = mock(ItemCatalogStore.class);
        var repository = mock(ItemRepository.class);
        var images = mock(ItemImageStorage.class);
        var first = new Item(1, "One");
        var second = new Item(2, "Two");
        when(repository.findByItemId(1)).thenReturn(Optional.of(first));
        when(repository.findByItemId(2)).thenReturn(Optional.of(second));
        when(source.load(COOKIE)).thenReturn(HTML);
        when(store.update(anyList())).thenReturn(new ItemCatalogStore.Counts(2, 2, 0));
        when(images.exists(any())).thenAnswer(call -> call.getArgument(0) != null);
        var started = new CountDownLatch(1);
        var finish = new CountDownLatch(1);
        when(images.download(argThat(item -> item != null && item.itemId() == 1))).thenAnswer(call -> {
            started.countDown();
            assertTrue(finish.await(5, TimeUnit.SECONDS));
            return "1.png";
        });
        when(images.download(argThat(item -> item != null && item.itemId() == 2))).thenReturn("2.png");
        doAnswer(call -> { first.setImgSrc("1.png"); return null; }).when(store).saveImage(1, "1.png");
        var service = new ItemRefreshService(source, new ItemPageParser(), store, repository, images);
        try (var requests = Executors.newSingleThreadExecutor()) {
            try {
                service.refresh(COOKIE);
                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertEquals(new ItemRefreshService.ButtonState("UNLOCKED", 10), service.state());
                var restart = requests.submit(() -> service.refresh(COOKIE));
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (!service.state().status().equals("LOCKED") && System.nanoTime() < deadline) Thread.sleep(5);
                assertEquals(new ItemRefreshService.ButtonState("LOCKED", 2), service.state());
                assertFalse(restart.isDone());
                assertThrows(ResponseStatusException.class, () -> service.refresh(COOKIE));
                verify(source, times(1)).load(COOKIE);
                finish.countDown();
                restart.get(5, TimeUnit.SECONDS);
                verify(store, timeout(5000)).saveImage(2, "2.png");
                var order = inOrder(store, source);
                order.verify(source).load(COOKIE);
                order.verify(store).update(anyList());
                order.verify(store).saveImage(1, "1.png");
                order.verify(source).load(COOKIE);
                verify(images, times(1)).download(argThat(item -> item != null && item.itemId() == 1));
                verify(images, times(1)).download(argThat(item -> item != null && item.itemId() == 2));
            } finally {
                finish.countDown();
                service.close();
            }
        }
    }

    @Test
    void imageFailureDoesNotPreventFollowingImagesFromBeingSaved() throws Exception {
        var source = mock(ItemCatalogSource.class);
        var store = mock(ItemCatalogStore.class);
        var repository = mock(ItemRepository.class);
        var images = mock(ItemImageStorage.class);
        when(source.load(COOKIE)).thenReturn(HTML);
        when(repository.findByItemId(anyLong())).thenAnswer(call ->
                Optional.of(new Item(call.getArgument(0), "Item")));
        when(images.download(argThat(item -> item != null && item.itemId() == 1)))
                .thenThrow(new IOException("Download failed"));
        when(images.download(argThat(item -> item != null && item.itemId() == 2))).thenReturn("2.png");
        var service = new ItemRefreshService(source, new ItemPageParser(), store, repository, images);
        try {
            service.refresh(COOKIE);
            verify(store, timeout(5000)).saveImage(2, "2.png");
            verify(store, never()).saveImage(eq(1L), anyString());
            assertEquals("UNLOCKED", service.state().status());
        } finally {
            service.close();
        }
    }

    @Test
    void sourceFailureUnlocksButtonAndDoesNotChangeDatabase() throws Exception {
        var source = mock(ItemCatalogSource.class);
        var store = mock(ItemCatalogStore.class);
        when(source.load(COOKIE)).thenThrow(new IOException("offline"));
        var service = new ItemRefreshService(source, new ItemPageParser(), store,
                mock(ItemRepository.class), mock(ItemImageStorage.class));
        try {
            assertThrows(ResponseStatusException.class, () -> service.refresh(COOKIE));
            assertEquals("UNLOCKED", service.state().status());
            verifyNoInteractions(store);
        } finally {
            service.close();
        }
    }
}


