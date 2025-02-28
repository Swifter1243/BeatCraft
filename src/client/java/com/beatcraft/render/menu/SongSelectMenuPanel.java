package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.menu.ConfirmSongDeleteMenu;
import com.beatcraft.menu.SongSelectMenu;
import com.beatcraft.networking.c2s.MapSyncC2SPayload;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SongSelectMenuPanel extends MenuPanel<SongSelectMenu> {
    private static final int SONGS_PER_PAGE = 6;
    private static final float PREVIEW_FADE_TIME = 1.2f;

    private static SongData currentDisplay = null;
    private static Identifier textureId = null;

    private static String selectedSet = "Standard";
    private static String selectedDiff = "ExpertPlus";

    private static class SongDisplayWidget extends Widget {

        public DynamicTexture coverImage = null;
        protected SongData data;

        protected SongDisplayWidget(SongData songData, Vector3f position, Runnable previewDisplayer) {
            data = songData;
            Identifier coverId;
            this.position = position;
            int width;
            int height;
            try {
                coverImage = new DynamicTexture(songData.getCoverImageFilename());
                width = coverImage.width();
                height = coverImage.height();
                coverId = coverImage.id();
            } catch (IOException e) {
                coverId = Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/no_cover.png");
                width = 256;
                height = 256;
            }

            children.addAll(List.of(
                new ButtonWidget(new Vector3f(113, 3, -0.05f), new Vector2f(300, 80), previewDisplayer,
                    new HoverWidget(new Vector3f(), new Vector2f(300, 80), List.of(
                        new GradientWidget(new Vector3f(), new Vector2f(300, 80), 0x5F222222, 0x5F222222, 0)
                    ), List.of(
                        new GradientWidget(new Vector3f(), new Vector2f(300, 80), 0x5F444444, 0x5F444444, 0)
                    ))
                ),
                new TextureWidget(coverId, new Vector3f(), new Vector2f(width, height)).withScale(75/(float) width, 75/(float) height),
                new TextWidget(songData.getTitle(), new Vector3f(43, -35, 0)).alignedLeft().withScale(2),
                new TextWidget(songData.getAuthor(), new Vector3f(43, -10, 0)).alignedLeft().withScale(1.5f).withColor(0xFFAAAAAA),
                new TextWidget("[" + String.join(", ", songData.getMappers()) + "]", new Vector3f(43, 10, 0)).alignedLeft().withScale(1.2f).withColor(0xFF44CC22)
            ));

        }

        public void unloadCover() {
            if (coverImage != null) {
                coverImage.unloadTexture();
            }
        }

        @Override
        protected void render(DrawContext context, Vector2f pointerPosition) {
            context.translate(-position.x, -position.y, -position.z);
        }
    }

    private int scrollIndex = 0;
    private final ContainerWidget songListContainer = new ContainerWidget(new Vector3f(-350, -200, -0.01f), new Vector2f(300, 500));
    private final ContainerWidget songDisplay = new ContainerWidget(new Vector3f(200, 0, -0.01f), new Vector2f());
    private final ContainerWidget setDifficulties = new ContainerWidget(new Vector3f(200, 0, -0.01f), new Vector2f());
    private final ContainerWidget difficultyStats = new ContainerWidget(new Vector3f(200, 0, -0.01f), new Vector2f());

    public SongSelectMenuPanel(SongSelectMenu data) {
        super(data);
        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);

        initLayout();
    }

    private void scrollDown() {
        scrollIndex = Math.min(scrollIndex+1, BeatCraftClient.songs.getSongs().size() / SONGS_PER_PAGE);
        updateList();
    }

    private void scrollUp() {
        scrollIndex = Math.max(0, scrollIndex-1);
        updateList();
    }

    public void initLayout() {
        widgets.clear();
        songDisplay.children.clear();
        setDifficulties.children.clear();
        difficultyStats.children.clear();

        widgets.addAll(List.of(
            new ButtonWidget(new Vector3f(-30, -200, 0.05f), new Vector2f(50, 50), this::scrollUp,
                new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/up_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f)
            ),
            new ButtonWidget(new Vector3f(-30, 200, 0.05f), new Vector2f(50, 50), this::scrollDown,
                new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/down_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f)
            ),
            songListContainer, songDisplay, setDifficulties, difficultyStats
        ));

        songDisplay.children.add(new TextureWidget(
            Identifier.of(BeatCraft.MOD_ID, "icon.png"),
            new Vector3f(), new Vector2f(128, 128)
        ).withScale(300f/128f));

        updateList();
    }

    public void updateList() {
        int start = scrollIndex * SONGS_PER_PAGE;
        int end = Math.min(BeatCraftClient.songs.getSongs().size(), start+SONGS_PER_PAGE);

        songListContainer.children.forEach(c -> {
            if (c instanceof SongDisplayWidget songDisp && songDisp.data != currentDisplay) {
                songDisp.unloadCover();
            }
        });

        songListContainer.children.clear();

        for (int i = start; i < end; i++) {
            SongData data = BeatCraftClient.songs.getSongs().get(i);
            songListContainer.children.add(new SongDisplayWidget(data, new Vector3f(0, (i-start) * 80, 0.05f), getPreviewGetter(data)));
        }

    }

    private Runnable getPreviewGetter(SongData data) {
        return () -> {
            this.setPreview(data);
        };
    }

    private static Widget _getSetDisplayWidget(String set) {
        Vector3f pos = new Vector3f(0, 0, 0.05f);
        return switch (set) {
            case "Standard" -> new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/set_icons/standard.png"), pos, new Vector2f(16, 16)).withScale(25f/16f);
            case "OneSaber" -> new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/set_icons/one_saber.png"), pos, new Vector2f(16, 16)).withScale(25f/16f);
            case "NoArrows" -> new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/set_icons/no_arrows.png"), pos, new Vector2f(16, 16)).withScale(25f/16f);
            case "Lawless" -> new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/set_icons/lawless.png"), pos, new Vector2f(16, 16)).withScale(25f/16f);
            case "360Degree" -> new TextWidget("360", pos.add(0, -8, 0), 2);
            case "90Degree" -> new TextWidget("90", pos.add(0, -8, 0), 2);
            default -> new TextWidget(set, pos.add(0, -8, 0), 1);
        };
    }

    private static Widget _getDiffDisplayWidget(String diff) {
        Vector3f pos = new Vector3f(0, -8, 0.05f);
        return new TextWidget(diff, pos, 2);
    }

    private static int getSetSortRank(String set) {
        return switch (set) {
            case "Standard" -> 0;
            case "OneSaber" -> 1;
            case "NoArrows" -> 2;
            case "360Degree" -> 3;
            case "90Degree" -> 4;
            case "Lawless" -> 5;
            default -> 6;
        };
    }

    private Widget getSetWidget(String set, SongData data, int setIndex, int setCount) {
        int AVAILABLE_WIDTH = 450;

        int widgetWidth = AVAILABLE_WIDTH / setCount;

        int widgetX = 75 + (-(setCount * widgetWidth) / 2 + setIndex * widgetWidth) + (widgetWidth/2);

        Widget display = _getSetDisplayWidget(set);

        return new ButtonWidget(new Vector3f(widgetX, 50, 0), new Vector2f(widgetWidth, 40), () -> openSet(set, data),
            new HoverWidget(new Vector3f(), new Vector2f(widgetWidth, 40), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> set.equals(selectedSet) ? 0x5F444444 : 0x5F222222, () -> set.equals(selectedSet) ? 0x5F444444 : 0x5F222222, 0)
            ), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> set.equals(selectedSet) ? 0x5F888888 : 0x5F666666, () -> set.equals(selectedSet) ? 0x5F888888 : 0x5F666666, 0)
            )),
            display
        );
    }

    private Widget getDiffWidget(String diff, String set, SongData data, int setIndex, int setCount) {
        int AVAILABLE_WIDTH = 450;

        int widgetWidth = AVAILABLE_WIDTH / setCount;

        int widgetX = 75 + (-(setCount * widgetWidth) / 2 + setIndex * widgetWidth) + (widgetWidth/2);

        Widget display = _getDiffDisplayWidget(diff);

        return new ButtonWidget(new Vector3f(widgetX, 100, 0), new Vector2f(widgetWidth, 40), () -> openDiff(diff, set, data),
            new HoverWidget(new Vector3f(), new Vector2f(widgetWidth, 40), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> diff.equals(selectedDiff) ? 0x5F444444 : 0x5F222222, () -> diff.equals(selectedDiff) ? 0x5F444444 : 0x5F222222, 0)
            ), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> diff.equals(selectedDiff) ? 0x5F888888 : 0x5F666666, () -> diff.equals(selectedDiff) ? 0x5F888888 : 0x5F666666, 0)
            )),
            display
        );
    }

    private void openSet(String set, SongData data) {
        List<String> diffs = data.getDifficulties(set);

        selectedSet = set;
        setDifficulties.children.clear();

        int i = 0;
        for (String diff : diffs) {
            Widget diffWidget = getDiffWidget(diff, set, data, i, diffs.size());
            setDifficulties.children.add(diffWidget);
            i++;
        }

        if (!diffs.contains(selectedDiff)) {
            selectedDiff = diffs.getLast();
        }

        openDiff(selectedDiff, set, data);

    }

    private void openDiff(String diff, String set, SongData data) {
        var info = data.getBeatMapInfo(set, diff);

        selectedDiff = diff;

        difficultyStats.children.clear();

        difficultyStats.children.add(
            new ButtonWidget(
                new Vector3f(150, 200, 0),
                new Vector2f(130, 50),
                () -> {
                    try {
                        song_play_request.cancel(true);
                        currentDisplay = null;
                        HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
                        BeatmapPlayer.setupDifficultyFromFile(info.getBeatmapLocation().toString());
                        BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
                        BeatmapPlayer.restart();
                        GameLogicHandler.reset();
                        BeatmapAudioPlayer.muteVanillaMusic();
                        ClientPlayNetworking.send(new MapSyncC2SPayload(data.getId(), set, diff));

                    } catch (IOException e) {
                        BeatCraft.LOGGER.error("There was a tragic failure whilst loading a beatmap", e);
                    }
                },
                new HoverWidget(
                    new Vector3f(),
                    new Vector2f(130, 50),
                    List.of(
                        new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(130, 50), 0x7F2F5080, 0x222D5090, 0)
                    ),
                    List.of(
                        new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(130, 50), 0x7F4270E0, 0x224270C0, 0)
                    )
                ),
                new TextWidget("PLAY", new Vector3f(0, -11, 0)).withScale(3)
            )
        );

    }

    private CompletableFuture<Void> song_play_request = null;
    private void setPreview(SongData data) {
        this.songDisplay.children.clear();

        //boolean in_list = false;
        //for (Widget widget : songListContainer.children) {
        //    if (widget instanceof SongDisplayWidget songDisp && songDisp.data == currentDisplay) {
        //        in_list = true;
        //    }
        //}
        //
        //if (!in_list) {
        //    DynamicTexture.unloadTextureFromId(textureId);
        //}

        currentDisplay = data;

        BeatmapAudioPlayer.beatmapAudio.setPlaybackSpeed(1);
        BeatmapAudioPlayer.playAudioFromFile(data.getPreviewFilename());
        if (song_play_request != null) {
            song_play_request.cancel(true);
        }
        song_play_request = CompletableFuture.runAsync(() -> {
            while (!BeatmapAudioPlayer.isReady()) {}
            if (currentDisplay != data) {
                return;
            }
            BeatmapAudioPlayer.beatmapAudio.play(data.getPreviewStartTime());
            double start = System.nanoTime() / 1_000_000_000d;
            double fadeTime = PREVIEW_FADE_TIME;
            double fadeStartTime = data.getPreviewDuration() - fadeTime;

            BeatmapAudioPlayer.beatmapAudio.setVolume(BeatCraftClient.playerConfig.getVolume());

            while ((System.nanoTime() / 1_000_000_000d) - start < fadeStartTime) {}

            if (currentDisplay != data) {
                return;
            }

            while ((System.nanoTime() / 1_000_000_000d) - start < data.getPreviewDuration()) {
                double elapsed = (System.nanoTime() / 1_000_000_000d) - start;
                float fadeProgress = (float) ((elapsed - fadeStartTime) / fadeTime);
                float newVolume = BeatCraftClient.playerConfig.getVolume() * (1.0f - fadeProgress);
                BeatmapAudioPlayer.beatmapAudio.setVolume(Math.max(newVolume, 0.0f));
                if (currentDisplay != data) {
                    return;
                }
            }

            BeatmapAudioPlayer.unload();
            BeatmapAudioPlayer.beatmapAudio.setVolume(BeatCraftClient.playerConfig.getVolume());
        });

        Identifier coverId;
        int width;
        int height;
        try {
            var coverImage = new DynamicTexture(data.getCoverImageFilename());
            width = coverImage.width();
            height = coverImage.height();
            coverId = coverImage.id();
        } catch (IOException e) {
            coverId = Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/no_cover.png");
            width = 256;
            height = 256;
        }

        textureId = coverId;

        songDisplay.children.addAll(List.of(
            new TextureWidget(coverId, new Vector3f(-85, -100, 0), new Vector2f(width, height)).withScale(150/(float) width, 150/(float) height),
            new TextWidget(data.getTitle(), new Vector3f(0, -175, 0)).withScale(3).alignedLeft(),
            new TextWidget(data.getAuthor(), new Vector3f(0, -145, 0)).withScale(2.5f).alignedLeft(),
            new TextWidget("["+String.join(", ", data.getMappers())+"]", new Vector3f(0, -115, 0)).withScale(2).alignedLeft().withColor(0xFF44CC22),
            new ButtonWidget(new Vector3f(30, -35, 0), new Vector2f(60, 25), () -> {
                HUDRenderer.confirmSongDeleteMenuPanel = new ConfirmSongDeleteMenuPanel(new ConfirmSongDeleteMenu(data));
                HUDRenderer.scene = HUDRenderer.MenuScene.ConfirmSongDelete;
            },
                new HoverWidget(new Vector3f(), new Vector2f(60, 25), List.of(
                    new GradientWidget(new Vector3f(0, 0, 0.05f), new Vector2f(60, 25), 0x7FAA1111, 0x7F881111, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(0, 0, 0.05f), new Vector2f(60, 25), 0x7FCC2222, 0x7FAA2222, 0)
                )),
                new TextWidget("DELETE", new Vector3f(0, -6, 0.01f)).withScale(1.5f)
            )
            // TODO: set, difficulty, play button, delete button, practice button, level stats (note count, wall count, etc...)
        ));

        ArrayList<String> sets = new ArrayList<>(data.getDifficultySets());

        sets.sort(Comparator.comparingInt(SongSelectMenuPanel::getSetSortRank));

        int selectedSetRank = getSetSortRank(selectedSet);

        int selectionDifference = 100;
        String closestSelection = selectedSet;

        int i = 0;
        for (String set : sets) {
            Widget widget = getSetWidget(set, data, i, sets.size());

            int setRank = getSetSortRank(set);
            int df = Math.abs(setRank - selectedSetRank);
            if (df < selectionDifference) {
                selectionDifference = df;
                closestSelection = set;
            }

            songDisplay.children.add(widget);

            i++;
        }

        openSet(closestSelection, data);

    }

}
