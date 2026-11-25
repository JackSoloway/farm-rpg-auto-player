package org.example.items;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.Locale;

@Component
public class ItemImageStorage {
    private final Path directory;

    public ItemImageStorage(@Value("${app.items.image-directory:data/item-icons}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public Path resolve(String path) {
        Path resolved = directory.resolve(path).normalize();
        if (!resolved.startsWith(directory) || resolved.equals(directory)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        return resolved;
    }

    public boolean exists(String path) {
        return path != null && Files.isRegularFile(resolve(path));
    }

    public String download(ItemPageParser.ParsedItem item) throws IOException, InterruptedException {
        String source = item.imageUri().getPath();
        String extension = source.substring(source.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!extension.matches("png|gif|jpg|jpeg|webp")) throw new IOException("Unsupported image format");
        String filename = item.itemId() + "." + extension;
        Files.createDirectories(directory);
        var request = HttpRequest.newBuilder(item.imageUri()).timeout(Duration.ofSeconds(30)).GET().build();
        byte[] bytes;
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new IOException("Image returned HTTP " + response.statusCode());
            bytes = response.body();
            if (bytes.length == 0 || !response.headers().firstValue("Content-Type").orElse("").startsWith("image/")) {
                throw new IOException("Response is not an image");
            }
        }
        // ImageIO does not support WebP by default. Validate the other supported formats before publishing.
        if (!extension.equals("webp") && ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
            throw new IOException("Invalid image data");
        }
        Path temporary = Files.createTempFile(directory, filename + ".", ".part");
        try {
            Files.write(temporary, bytes);
            try {
                Files.move(temporary, resolve(filename), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return filename;
    }
}
