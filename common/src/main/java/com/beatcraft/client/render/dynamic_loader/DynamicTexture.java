package com.beatcraft.client.render.dynamic_loader;

import com.beatcraft.Beatcraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/// MAKE SURE TO CALL DynamicTexture#unloadTexture() WHEN DONE USING THE TEXTURE
public class DynamicTexture {
    private static final HashMap<ResourceLocation, DynamicTexture> loadedTextures = new HashMap<>();
    private final ResourceLocation textureID;
    private final int width;
    private final int height;
    private final String path;
    private NativeImage img;
    private net.minecraft.client.renderer.texture.DynamicTexture tex;

    private String filterString(String in) {
        return in.replaceAll("[^a-z0-9/._-]", "_").replaceAll("(?i)\\.jpe?g$", ".png");
    }

    public DynamicTexture(String path) throws IOException {
        this.path = path.replaceAll("(?i)\\.jpe?g$", ".png");
        img = NativeImage.read(new FileInputStream(this.path.replace("\\", "/")));
        tex = new net.minecraft.client.renderer.texture.DynamicTexture(img);

        width = img.getWidth();
        height = img.getHeight();

        textureID = Beatcraft.id("dynamic/" + filterString(this.path.toLowerCase()));

        unloadTextureFromId(textureID);

        Minecraft.getInstance().getTextureManager().register(textureID, (AbstractTexture) tex);

        loadedTextures.put(textureID, this);
    }

    public void reload() {
        Minecraft.getInstance().getTextureManager().release(textureID);
        try {
            img = NativeImage.read(new FileInputStream(this.path.replace("\\", "/")));
            tex = new net.minecraft.client.renderer.texture.DynamicTexture(img);
        } catch (IOException e) {
            Beatcraft.LOGGER.error("Failed to reload texture '{}'", this.path, e);
        }
        Minecraft.getInstance().getTextureManager().register(textureID, (AbstractTexture) tex);
        if (!loadedTextures.containsKey(textureID)) {
            loadedTextures.put(textureID, this);
        }
    }

    public boolean isLoaded() {
        return loadedTextures.containsKey(textureID);
    }

    public DynamicTexture getFromId(ResourceLocation id) {
        return loadedTextures.get(id);
    }

    public ResourceLocation id() {
        return textureID;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public static void unloadAllTextures() {
        //for (ResourceLocation id : loadedTextures.keySet()) {
        //    MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
        //}
        loadedTextures.clear();
    }

    public void unloadTexture() {
        if (!isLoaded()) return;
        Minecraft.getInstance().getTextureManager().release(this.textureID);
        loadedTextures.remove(this.textureID);
    }

    public static void unloadTextureFromId(ResourceLocation id) {
        if (loadedTextures.containsKey(id)) {
            Minecraft.getInstance().getTextureManager().release(id);
            loadedTextures.remove(id);
        }
    }

}