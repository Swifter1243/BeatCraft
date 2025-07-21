package com.beatcraft;

import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.lightshow.environment.kaleidoscope.RingSpike;
import com.beatcraft.menu.SongDownloaderMenu;
import com.beatcraft.menu.SongList;
import com.beatcraft.render.BeatCraftRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.gl.GlUtil;
import com.beatcraft.render.instancing.InstancedMesh;
import com.beatcraft.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.render.menu.SongSelectMenuPanel;
import com.beatcraft.render.mesh.MeshLoader;
import com.beatcraft.replay.ReplayHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

public class BeatCraftAssetReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier ID = BeatCraft.id("asset_reloader");

    @Override
    public Identifier getFabricId() {
        return ID;
    }



    @Override
    public void reload(ResourceManager manager) {


        // Unload
        InstancedMesh.cleanupAll();
        LightMesh.cleanupAll();
        //DynamicTexture.unloadAllTextures();
        GlUtil.clear();

        // Load
        Bloomfog.initShaders();
        BeatmapAudioPlayer.init();

        var window = MinecraftClient.getInstance().getWindow();
        var w = Math.max(1, window.getWidth());
        var h = Math.max(1, window.getHeight());

        if (BeatCraftRenderer.bloomfog == null) BeatCraftRenderer.init();
        BeatCraftRenderer.bloomfog.resize(w, h, true);

        BeatCraftClient.songs.loadSongs();
        ReplayHandler.loadReplays();
        HUDRenderer.initSongSelectMenuPanel();

        MeshLoader.COLOR_NOTE_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/color_note.json"), MeshLoader.NOTE_TEXTURE, "instanced/color_note", 1f);
        MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/color_note_chain_head.json"), MeshLoader.NOTE_TEXTURE, "instanced/color_note", 1f);
        MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/color_note_chain_link.json"), MeshLoader.NOTE_TEXTURE, "instanced/color_note", 1f);
        MeshLoader.BOMB_NOTE_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/bomb_note.json"), MeshLoader.NOTE_TEXTURE, "instanced/bomb_note", 1f);
        MeshLoader.NOTE_ARROW_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/note_arrow.json"), MeshLoader.ARROW_TEXTURE, "instanced/arrow", 1f);
        MeshLoader.NOTE_DOT_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/note_dot.json"), MeshLoader.ARROW_TEXTURE, "instanced/arrow", 1f);
        MeshLoader.CHAIN_DOT_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/item/chain_note_dot.json"), MeshLoader.ARROW_TEXTURE, "instanced/arrow", 1f);

        MeshLoader.MIRROR_COLOR_NOTE_INSTANCED_MESH = MeshLoader.COLOR_NOTE_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_BOMB_NOTE_INSTANCED_MESH = MeshLoader.BOMB_NOTE_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_CHAIN_HEAD_NOTE_INSTANCED_MESH = MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_CHAIN_LINK_NOTE_INSTANCED_MESH = MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_NOTE_ARROW_INSTANCED_MESH = MeshLoader.NOTE_ARROW_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_NOTE_DOT_INSTANCED_MESH = MeshLoader.NOTE_DOT_INSTANCED_MESH.copy();
        MeshLoader.MIRROR_CHAIN_DOT_INSTANCED_MESH = MeshLoader.CHAIN_DOT_INSTANCED_MESH.copy();

        MeshLoader.SMOKE_INSTANCED_MESH = MeshLoader.loadInstancedMesh(BeatCraft.id("models/gameplay/smoke.json"), MeshLoader.SMOKE_TEXTURE, "instanced/smoke", 6f);

        try {
            MeshLoader.KALEIDOSCOPE_SPIKE = LightMesh.load("kaleidoscope_spike", BeatCraft.id("meshes/environment/kaleidoscope/spikes.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SongSelectMenuPanel.refreshList = true;


        if (BeatmapAudioPlayer.currentFile != null && BeatmapPlayer.currentInfo != null) {
            var b = BeatmapPlayer.getCurrentBeat();
            BeatmapAudioPlayer.playAudioFromFile(BeatmapAudioPlayer.currentFile);
            BeatmapAudioPlayer.goToBeat(b);
            if (BeatmapPlayer.isPlaying()) { // re-sync song
                BeatmapPlayer.play();
            } else {
                BeatmapPlayer.pause();
            }
        }

        LightMesh.initialized = false;

        RenderSystem.recordRenderCall(() -> {
            LightMesh.buildMeshes();
            RingSpike.reload();
        });


    }
}
