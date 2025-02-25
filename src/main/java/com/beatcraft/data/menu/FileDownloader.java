package com.beatcraft.data.menu;

import com.beatcraft.BeatCraft;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

public class FileDownloader {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void downloadCoverImage(String url, String outputFileName, Runnable after) {
        CompletableFuture.runAsync(() -> {
            _downloadCoverImage(url, outputFileName);
        }).thenRun(after);
    }
    private static void _downloadCoverImage(String url, String outputFileName) {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200 || response.body() == null) {
                BeatCraft.LOGGER.error("Failed to download file from '{}'", url);
                throw new IOException("Failed to download file from: " + url);
            }

            File tempFile = new File(outputFileName + ".tmp");
            Files.write(tempFile.toPath(), response.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            File finalPngFile = new File(outputFileName);
            convertAndResize(tempFile, finalPngFile);

            boolean deleted = tempFile.delete();
            if (!deleted) {
                BeatCraft.LOGGER.warn("Failed to delete temporary file '{}'", tempFile.getAbsolutePath());
            }

        } catch (IOException | InterruptedException e) {
            BeatCraft.LOGGER.error("Failed to download file '{}'", url, e);
            throw new RuntimeException("Failed to download image: " + url, e);
        }
    }

    private static void convertAndResize(File inputFile, File outputFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("Invalid image file: " + inputFile.getAbsolutePath());
        }

        BufferedImage resizedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, 100, 100, null);
        g2d.dispose();

        ImageIO.write(resizedImage, "png", outputFile);
    }


}
