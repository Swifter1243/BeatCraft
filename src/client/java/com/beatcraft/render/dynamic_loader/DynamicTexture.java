package com.beatcraft.render.dynamic_loader;

import com.beatcraft.BeatCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/// MAKE SURE TO CALL DynamicTexture#unloadTexture() WHEN DONE USING THE TEXTURE
public class DynamicTexture {
    private static final ArrayList<Identifier> loadedTextures = new ArrayList<>();
    private final Identifier textureID;
    private final int width;
    private final int height;
    private final String path;
    private NativeImage img;
    private net.minecraft.client.texture.DynamicTexture tex;

    private String filterString(String in) {
        return in.replaceAll("[^a-z0-9/._-]", "_").replaceAll("(?i)\\.jpe?g$", ".png");
    }

    public DynamicTexture(String path) throws IOException {
        this.path = path.replaceAll("(?i)\\.jpe?g$", ".png");
        img = NativeImage.read(new FileInputStream(this.path));
        tex = new NativeImageBackedTexture(img);

        width = img.getWidth();
        height = img.getHeight();

        textureID = Identifier.of(BeatCraft.MOD_ID, "dynamic/" + filterString(this.path.toLowerCase()));

        unloadTextureFromId(textureID);

        MinecraftClient.getInstance().getTextureManager().registerTexture(textureID, (AbstractTexture) tex);

        loadedTextures.add(textureID);
    }

    public void reload() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureID);
        try {
            img = NativeImage.read(new FileInputStream(this.path));
            tex = new NativeImageBackedTexture(img);
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to reload texture '{}'", this.path, e);
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureID, (AbstractTexture) tex);
        if (!loadedTextures.contains(textureID)) {
            loadedTextures.add(textureID);
        }
    }

    public boolean isLoaded() {
        return loadedTextures.contains(textureID);
    }

    public Identifier id() {
        return textureID;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public static void unloadAllTextures() {
        for (Identifier id : loadedTextures) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
        }
        loadedTextures.clear();
    }

    public void unloadTexture() {
        if (!isLoaded()) return;
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.textureID);
        loadedTextures.remove(this.textureID);
    }

    public static void unloadTextureFromId(Identifier id) {
        if (loadedTextures.contains(id)) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
            loadedTextures.remove(id);
        }
    }

}
