package com.beatcraft.screen;

import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.data.menu.song_preview.SongPreview;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SongDownloaderScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private TextBoxComponent search;
    private FlowLayout listComponent;

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
                )
                .positioning(Positioning.relative(0, 100))
        );

    }

    protected void previewLayoutConfigurator(FlowLayout layout) {

    }

    protected Component makeSongPreviewDisplay(SongPreview preview) {

        return Components.label(Text.literal(preview.name()));
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
