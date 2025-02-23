package com.beatcraft.screen;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.data.menu.FileDownloader;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.data.menu.song_preview.SongPreview;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SongDownloaderScreen extends BaseOwoScreen<FlowLayout> {

    private final Screen parent;
    private TextBoxComponent search;
    private FlowLayout listComponent;
    private FlowLayout previewComponent;
    private LabelComponent pageDisplay;
    private FlowLayout coverContainer;
    private DynamicTexture coverImage;

    public SongDownloaderScreen(Screen parent) {
        super(Text.translatable("screen.beatcraft.song_downloader"));
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
        pageDisplay = (LabelComponent) Components.label(Text.literal(String.format("PAGE %s", SongDownloader.page+1))).lineHeight(15).tooltip(Text.translatable("gui.beatcraft.button.page_error"));
        previewComponent = Containers.verticalFlow(Sizing.fill(), Sizing.fill());
        flowLayout.surface(Surface.VANILLA_TRANSLUCENT);
        flowLayout.child(
            search
        ).child(
            Containers.horizontalFlow(Sizing.fill(), Sizing.fill(90))
                .child(
                    Containers.verticalFlow(Sizing.fill(50), Sizing.fill())
                        .child(
                            Containers.horizontalFlow(Sizing.fill(), Sizing.content())
                                .child(
                                    Components.box(Sizing.fill(5), Sizing.fixed(2)).color(Color.ofArgb(0))
                                ).child(
                                    Components.button(Text.literal(" < "), this::pageLeft)
                                ).child(
                                    Components.box(Sizing.fill(25), Sizing.fixed(2)).color(Color.ofArgb(0))
                                ).child(
                                    pageDisplay
                                ).child(
                                    Components.box(Sizing.fill(25), Sizing.fixed(2)).color(Color.ofArgb(0))
                                ).child(
                                    Components.button(Text.literal(" > "), this::pageRight)
                                )
                        ).child(
                            Containers.verticalScroll(
                                Sizing.content(), Sizing.fill(90),
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

    private void pageLeft(ButtonComponent button) {
        SongDownloader.pageLeft(this::updateList);
    }

    private void pageRight(ButtonComponent button) {
        SongDownloader.pageRight(this::updateList);
    }

    protected void previewLayoutConfigurator(FlowLayout layout) {

    }

    protected Component makeSongPreviewDisplay(SongPreview preview) {

        var layoutA = Containers.horizontalFlow(Sizing.fixed(450), Sizing.fixed(30));

        var layoutB = Containers.verticalFlow(Sizing.fill(45), Sizing.fill());

        var openButton = Components.button(Text.literal(" > "), (b) -> openPreview(b, preview)).positioning(Positioning.relative(45, 0));

        var titleScroll = Containers.horizontalScroll(
            Sizing.fill(65), Sizing.content(),
            Components.label(Text.literal(preview.name()))
        );

        layoutB.child(
                titleScroll
        ).child(
                Components.label(Text.literal(preview.metaData().levelAuthorName()))
        );

        layoutA.child(
            Components.spacer(5)
        ).child(
            layoutB
        ).child(
            openButton
        );

        return layoutA;
    }

    private void setCoverImage() {
        coverContainer.clearChildren();
        if (coverImage != null) {
            coverImage.unloadTexture();
            coverImage = null;
        }
        try {
            coverImage = new DynamicTexture(
                MinecraftClient.getInstance().runDirectory + "/beatcraft/temp/cover.png"
            );

            coverContainer.child(
                Components.texture(coverImage.id(), 0, 0, 100, 100, coverImage.width(), coverImage.height())
            );

        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to change image!", e);
            throw new RuntimeException(e);
        }
    }

    private void openPreview(ButtonComponent button, SongPreview preview) {
        previewComponent.clearChildren();
        coverContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());

        coverContainer.child(
            Components.texture(
                Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_downloader/default_cover.png"),
                0, 0, 100, 100, 256, 256
            )
        ).horizontalAlignment(HorizontalAlignment.CENTER);

        FileDownloader.downloadCoverImage(
            preview.versions().getFirst().coverURL(),
            MinecraftClient.getInstance().runDirectory + "/beatcraft/temp/cover.png",
            this::setCoverImage
        );


        previewComponent.child(
                coverContainer
        ).child(
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
        ).horizontalAlignment(HorizontalAlignment.CENTER);

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
            SongDownloader.listModifyLock.lock();
            SongDownloader.songPreviews.forEach(preview -> {
                listComponent.child(this.makeSongPreviewDisplay(preview));
            });
            SongDownloader.listModifyLock.unlock();
            pageDisplay.text(Text.literal(String.format("PAGE %s", SongDownloader.page+1)));
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
        if (coverImage != null) {
            coverImage.unloadTexture();
            coverImage = null;
        }
        client.setScreen(parent);
    }
}
