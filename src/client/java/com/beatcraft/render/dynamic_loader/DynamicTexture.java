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

    public DynamicTexture(String path) throws IOException {
        NativeImage img = NativeImage.read(new FileInputStream(path));
        net.minecraft.client.texture.DynamicTexture tex = new NativeImageBackedTexture(img);

        width = img.getWidth();
        height = img.getHeight();

        textureID = Identifier.of(BeatCraft.MOD_ID, "dynamic/" + Path.of(path).getFileName().toString());

        MinecraftClient.getInstance().getTextureManager().registerTexture(textureID, (AbstractTexture) tex);

        loadedTextures.add(textureID);
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
    }

}
