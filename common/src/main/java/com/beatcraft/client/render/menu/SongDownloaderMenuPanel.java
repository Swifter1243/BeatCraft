package com.beatcraft.client.render.menu;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.menu.SongDownloaderMenu;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.map.SongDownloader;
import com.beatcraft.common.data.map.song_preview.SongPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;

public class SongDownloaderMenuPanel extends MenuPanel<SongDownloaderMenu> {

    private class SongPreviewWidget extends Widget {

        private static final int LIST_MAX_WIDTH = 480;
        protected SongPreview preview;

        protected SongPreviewWidget(SongPreview preview, Vector3f position, Runnable previewDisplayer) {
            this.preview = preview;
            this.position = position;

            children.addAll(List.of(
                new ButtonWidget(new Vector3f(250, 3, -0.05f), new Vector2f(500, 50), previewDisplayer,
                    new HoverWidget(new Vector3f(), new Vector2f(500, 50), List.of(
                        new GradientWidget(new Vector3f(), new Vector2f(500, 50), 0x5F222222, 0x5F222222, 0)
                    ), List.of(
                        new GradientWidget(new Vector3f(), new Vector2f(500, 50), 0x5F444444, 0x5F444444, 0)
                    ))
                ),
                //new TextureWidget(coverId, new Vector3f(), new Vector2f(width, height)).withScale(75/(float) width, 75/(float) height),
                new TextWidget(preview.name(), new Vector3f(2, -15, 0)).alignedLeft().withScale(2).withDynamicScaling(LIST_MAX_WIDTH/2),
                new TextWidget(preview.uploaderData().name(), new Vector3f(6, 3, 0)).alignedLeft().withScale(1.5f).withColor(0xFFAAAAAA).withDynamicScaling((int) (LIST_MAX_WIDTH/1.5f))
                //new TextWidget("[" + String.join(", ", songData.getMappers()) + "]", new Vector3f(43, 10, 0)).alignedLeft().withScale(1.2f).withColor(0xFF44CC22).withDynamicScaling((int) (LIST_MAX_WIDTH*1.6666f))
            ));

        }

        @Override
        protected void render(GuiGraphics context, Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);
        }
    }

    public SongDownloaderMenuPanel(HUDRenderer hudRenderer) {
        super(new SongDownloaderMenu(hudRenderer));
        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);

        initLayout();
    }

    private final TextInput searchInput = new TextInput();
    private final ContainerWidget resultsContainer = new ContainerWidget(
        new Vector3f(-200, 0, 0),
        new Vector2f(600, 450)
    );
    private final ContainerWidget previewContainer = new ContainerWidget(
        new Vector3f(300, 0, 0),
        new Vector2f(300, 500)
    );

    private static final Component SEARCH = Component.translatable("menu.beatcraft.song_download.search");

    private void initLayout() {
        widgets.clear();

        widgets.addAll(List.of(
            SettingsMenuPanel.getButton(
                new TextWidget(() -> {
                    var t = searchInput.buffer.toString();
                    return t.isEmpty() ? SEARCH.getString() : t;
                }, new Vector3f(-290, -11, 0.01f), 2).alignedLeft(),
                () -> this.data.hudRenderer.hookToKeyboard(searchInput),
                new Vector3f(-200, -225, 0), new Vector2f(600, 50)
            ),

            SettingsMenuPanel.getButton(
                new TextWidget("\uD83D\uDD0E", new Vector3f(0, -11, 0.01f), 2),
                () -> {
                    this.data.hudRenderer.hideKeyboard();
                    SongDownloader.queryBuilder.q = searchInput.buffer.toString();
                    SongDownloader.queryBuilder.page = 0;
                    SongDownloader.loadFromSearch(() -> refresh = true);
                },
                new Vector3f(130, -225, 0), new Vector2f(50, 50)
            ),
            resultsContainer,
            previewContainer,

            new ButtonWidget(new Vector3f(30, -150, 0.05f), new Vector2f(50, 50), this::scrollUp,
                new TextureWidget(Beatcraft.id("textures/gui/song_selector/up_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f)
            ),
            new ButtonWidget(new Vector3f(30, 185, 0.05f), new Vector2f(50, 50), this::scrollDown,
                new TextureWidget(Beatcraft.id("textures/gui/song_selector/down_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f)
            )
        ));
    }

    private void scrollUp() {
        SongDownloader.queryBuilder.page = Math.max(0, SongDownloader.queryBuilder.page-1);
        SongDownloader.loadFromSearch(() -> refresh = true);
    }

    private void scrollDown() {
        SongDownloader.queryBuilder.page += 1;
        SongDownloader.loadFromSearch(() -> refresh = true);

    }

    private boolean refresh = false;

    private void refreshList() {
        resultsContainer.children.clear();

        var pos = new Vector3f(-300, -150, 0);

        for (var previewData : SongDownloader.songPreviews) {
            var previewCard = new SongPreviewWidget(previewData, new Vector3f(pos), () -> setPreview(previewData));
            resultsContainer.children.add(previewCard);
            pos.y += 55;
        }

    }

    private final ContainerWidget coverContainer = new ContainerWidget(
        new Vector3f(0, -200, 0),
        new Vector2f(200, 200)
    );
    private static final int DISPLAY_WIDTH = 300;

    private static final Component DOWNLOAD = Component.translatable("menu.beatcraft.song_download.download");

    private void setPreview(SongPreview data) {
        previewContainer.children.clear();

        previewContainer.children.addAll(List.of(
            new TextWidget(data.id(), new Vector3f(-125, -25, 0.01f), 1f).withDynamicScaling(DISPLAY_WIDTH).alignedLeft(),
            new TextWidget(data.name(), new Vector3f(0, -10, 0.01f), 2).withDynamicScaling(DISPLAY_WIDTH/2),
            new TextWidget(data.uploaderData().name(), new Vector3f(0, 20, 0.01f), 1.5f).withDynamicScaling((int) (DISPLAY_WIDTH/1.5f)),
            new TextWidget(data.getSets(), new Vector3f(0, 50, 0.01f), 1.5f).withDynamicScaling((int) (DISPLAY_WIDTH/1.5f)),
            new TextWidget(data.getDiffs(), new Vector3f(0, 80, 0.01f), 1.5f).withDynamicScaling((int) (DISPLAY_WIDTH/1.5f)),
            SettingsMenuPanel.getButton(
                new TextWidget(DOWNLOAD, new Vector3f(0, -11, 0.01f), 2),
                () -> downloadSong(data),
                new Vector3f(0, 140, 0), new Vector2f(250, 50)
            )
        ));

    }

    @Override
    public void render(MultiBufferSource.BufferSource immediate, Vector2f pointerPosition, boolean triggerPressed) {
        super.render(immediate, pointerPosition, triggerPressed);
        if (refresh) {
            refresh = false;
            refreshList();
        }
    }


    private void downloadSong(SongPreview data) {
        SongDownloader.downloadSong(data, Minecraft.getInstance().gameDirectory.getAbsolutePath(), () -> {
            BeatcraftClient.songs.loadSongs();
            SongSelectMenuPanel.refreshList = true;
        });
    }

}
