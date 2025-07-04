package com.beatcraft.render.menu;

import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.logic.InputSystem;
import com.beatcraft.menu.EndScreenData;
import com.beatcraft.render.HUDRenderer;
import net.minecraft.text.Text;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class EndScreenPanel extends MenuPanel<EndScreenData> {
    public EndScreenPanel(EndScreenData data) {
        super(data);
        position.set(0, 2f, 6);
        size.set(800, 500);
        backgroundColor = 0;
        setData(data);
    }

    private static final Text FAILED = Text.translatable("menu.beatcraft.failed.level_failed");
    private static final Text CONTINUE = Text.translatable("menu.beatcraft.paused.continue");
    private static final Text CLEARED = Text.translatable("menu.beatcraft.passed.cleared");
    private static final Text GOOD_CUTS = Text.translatable("menu.beatcraft.passed.good_cuts");
    private static final Text SCORE = Text.translatable("menu.beatcraft.passed.score");
    private static final Text RANK = Text.translatable("menu.beatcraft.passed.rank");
    private static final Text MAX_COMBO = Text.translatable("menu.beatcraft.passed.max_combo");


    public void setFailed() {
        this.data = null;

        widgets.clear();
        widgets.addAll(List.of(
            new TextWidget(FAILED, new Vector3f(0, -80, 0), 5),
            new ButtonWidget(
                    new Vector3f(0, 200, 0), new Vector2f(250, 50),
                    () -> {
                        HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                        BeatmapAudioPlayer.unmuteVanillaMusic();
                        InputSystem.unlockHotbar();
                    },
                    new HoverWidget(
                            new Vector3f(),
                            new Vector2f(250, 50),
                            List.of(
                                    new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(250, 50), 0x7F222222, 0x22222222, 0)
                            ),
                            List.of(
                                    new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(250, 50), 0x7F2260B0, 0x22226080, 0)
                            )
                    ),
                    new TextWidget(CONTINUE, new Vector3f(0, -20, 0.05f), 5)
            )
        ));
    }

    public void setData(EndScreenData data) {
        this.data = data;

        widgets.clear();
        widgets.addAll(List.of(
                new TextWidget(CLEARED, new Vector3f(0, -80, 0), 5),
                new TextWidget(GOOD_CUTS, new Vector3f(-280, 30, -0.01f), 2),
                new TextWidget(SCORE, new Vector3f(0, 30, -0.01f), 2),
                new TextWidget(RANK, new Vector3f(280, 30, -0.01f), 2),
                new TextWidget(data.goodCuts + "/" + data.totalNotes, new Vector3f(-280, 50, -0.01f), 5),
                new TextWidget(String.valueOf(data.score), new Vector3f(0, 50, -0.01f), 5),
                new TextWidget(String.valueOf(data.rank), new Vector3f(280, 50, -0.01f), 5),
                new TextWidget(() -> MAX_COMBO.getString() + " " + data.maxCombo, new Vector3f(-280, 90, -0.01f), 1.5f),
                new ButtonWidget(
                        new Vector3f(0, 200, 0), new Vector2f(250, 50),
                        () -> {
                            HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                            BeatmapAudioPlayer.unmuteVanillaMusic();
                            InputSystem.unlockHotbar();
                        },
                        new HoverWidget(
                                new Vector3f(),
                                new Vector2f(250, 50),
                                List.of(
                                        new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(250, 50), 0x7F222222, 0x22222222, 0)
                                ),
                                List.of(
                                        new GradientWidget(new Vector3f(0, 0, 0.005f), new Vector2f(250, 50), 0x7F2260B0, 0x22226080, 0)
                                )
                        ),
                        new TextWidget(CONTINUE, new Vector3f(0, -20, 0.05f), 5)
                )
        ));
    }
}
