package com.beatcraft.screen;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.beatmap.BeatmapLoader;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.data.menu.song_preview.SongPreview;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SongDownloaderScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private TextBoxComponent search;
    private FlowLayout listComponent;
    private FlowLayout previewComponent;

    public SongDownloaderScreen(Screen parent) {
        super(Text.translatable(""));
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }


    @Override
    protected void build(FlowLayout flowLayout) {
        SongDownloader.loadFromSearch(this::updateList);
        search = (TextBoxComponent) Components.textBox(Sizing.fill(80), SongDownloader.search).positioning(Positioning.relative(50, 1));
        search.active = true;
        search.onChanged().subscribe(this::updateSearch);
        listComponent = Components.list(
            SongDownloader.songPreviews,
            this::previewLayoutConfigurator,
            this::makeSongPreviewDisplay,
            true
        );
        previewComponent = Containers.verticalFlow(Sizing.fill(), Sizing.fill());
        flowLayout.surface(Surface.VANILLA_TRANSLUCENT);
        flowLayout.child(
            search
        ).child(
            Containers.horizontalFlow(Sizing.fill(), Sizing.fill(80))
                .child(
                    Containers.verticalFlow(Sizing.fill(50), Sizing.content())
                        .child(
                            Containers.verticalScroll(
                                Sizing.content(), Sizing.fill(),
                                listComponent
                            )
                        )
                )
                .child(
                    Containers.verticalFlow(Sizing.fill(50), Sizing.content())
                        .child(
                            previewComponent
                        )
                )
                .positioning(Positioning.relative(0, 100))
        );

    }

    protected void previewLayoutConfigurator(FlowLayout layout) {

    }

    protected Component makeSongPreviewDisplay(SongPreview preview) {

        var layoutA = Containers.horizontalFlow(Sizing.fixed(450), Sizing.fixed(50));

        var layoutB = Containers.verticalFlow(Sizing.fill(50), Sizing.fill());

        var openButton = Components.button(Text.literal(" > "), (b) -> openPreview(b, preview)).positioning(Positioning.relative(35, 0));

        var titleScroll = Containers.horizontalScroll(Sizing.fill(50), Sizing.content(), Components.label(Text.literal(preview.name())).lineHeight(8));

        layoutB.child(
                titleScroll
        ).child(
                Components.label(Text.literal(preview.metaData().levelAuthorName()))
        );

        layoutA.child(
            layoutB
        ).child(
            openButton
        );

        return layoutA;
    }

    private void openPreview(ButtonComponent button, SongPreview preview) {
        previewComponent.clearChildren();

        //var image_placeholder = Components.box(Sizing.fixed(100), Sizing.fixed(100)).positioning(Positioning.relative(50, 0));

        //previewComponent.child(image_placeholder);

        previewComponent.child(
                Containers.horizontalScroll(Sizing.fill(), Sizing.content(),
                        Components.label(Text.literal(preview.name()))
                )
        ).child(
                Containers.horizontalScroll(Sizing.fill(), Sizing.content(),
                        Components.label(Text.literal(preview.metaData().songName() + " - " + preview.metaData().songAuthorName()))
                )
        ).child(
                Containers.horizontalScroll(Sizing.fill(), Sizing.content(),
                        Components.label(Text.literal(preview.metaData().levelAuthorName()))
                )
        ).child(
                Containers.horizontalScroll(Sizing.fill(), Sizing.content(),
                        Components.label(Text.literal(preview.getSets()))
                )
        ).child(
                Containers.horizontalScroll(Sizing.fill(), Sizing.content(),
                        Components.label(Text.literal(preview.getDiffs()))
                )
        ).child(
                Components.button(
                        Text.translatable("gui.beatcraft.download_song"),
                        b -> downloadSong(b, preview)
                )
        );

    }

    private void downloadSong(ButtonComponent button, SongPreview preview) {
        SongDownloader.downloadSong(preview, MinecraftClient.getInstance().runDirectory.getAbsolutePath(), BeatCraftClient.songs::loadSongs);
    }

    private static final Queue<Runnable> listUpdateQueue = new ConcurrentLinkedQueue<>();
    @Override
    public void tick() {

        while (!listUpdateQueue.isEmpty()) {
            Runnable call = listUpdateQueue.poll();
            if (call == null) continue;
            call.run();
        }

        super.tick();
    }

    protected void updateList() {
        listUpdateQueue.add(() -> {
            listComponent.clearChildren();
            SongDownloader.songPreviews.forEach(preview -> {
                listComponent.child(this.makeSongPreviewDisplay(preview));
            });
        });

    }

    protected void updateSearch(String content) {
        if (!SongDownloader.search.equals(content)) {
            SongDownloader.search = content;
            SongDownloader.loadFromSearch(this::updateList);
        }
    }


    @Override
    public void close() {
        client.setScreen(parent);
    }
}
