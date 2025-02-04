package com.beatcraft.data.menu;

import com.beatcraft.BeatCraft;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FileDownloader {

    private static final OkHttpClient httpClient = new OkHttpClient();

    public static void downloadCoverImage(String url, String outputFileName, Runnable after) {
        CompletableFuture.runAsync(() -> {
            _downloadCoverImage(url, outputFileName);
        }).thenRun(after);
    }
    private static void _downloadCoverImage(String url, String outputFileName) {
        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                BeatCraft.LOGGER.error("Failed to download file from '{}'", url);
                throw new IOException("Failed to download file from: " + url);
            }

            File tempFile = new File(outputFileName + ".tmp");
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                outputStream.write(response.body().bytes());
            }

            File finalPngFile = new File(outputFileName);
            convertAndResize(tempFile, finalPngFile);

            var ignored = tempFile.delete();

        } catch (IOException e) {
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
