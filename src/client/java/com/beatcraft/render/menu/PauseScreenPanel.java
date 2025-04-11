package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.InputSystem;
import com.beatcraft.menu.PauseMenu;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.replay.PlayRecorder;
import com.beatcraft.replay.ReplayHandler;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;

public class PauseScreenPanel extends MenuPanel<PauseMenu> {
    public PauseScreenPanel() {
        super(new PauseMenu());
        position.set(0, 1.5f, 6);
        size = new Vector2f(600, 150);

        widgets.clear();
        widgets.addAll(List.of(
            new ButtonWidget(
                new Vector3f(-160, 0, 0.02f), new Vector2f(130, 50),
                () -> {
                    try {
                        PlayRecorder.save();
                    } catch (IOException e) {
                        BeatCraft.LOGGER.error("Error saving recording", e);
                    }
                    BeatmapPlayer.currentBeatmap = null;
                    BeatmapPlayer.currentInfo = null;
                    BeatmapAudioPlayer.unload();
                    HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                    BeatmapAudioPlayer.unmuteVanillaMusic();
                    InputSystem.unlockHotbar();
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("MENU", new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(0, 0, 0.02f), new Vector2f(150, 50),
                () -> {
                    GameLogicHandler.reset();
                    BeatmapPlayer.restart();
                    HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
                    InputSystem.lockHotbar();
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("RESTART", new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(160, 0, 0.02f), new Vector2f(150, 50),
                GameLogicHandler::unpauseMap,
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("CONTINUE", new Vector3f(0, -11, 0.05f)).withScale(3)
            )
        ));

    }


    //BeatcraftRenderer.recordRenderCall(() -> {
    //
    //    Tessellator tessellator = Tessellator.getInstance();
    //    var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    //
    //    Vector3f center = new Vector3f(0, 1.5f, 6);
    //
    //    Vector3f normal = new Vector3f(0, 0, -1);
    //    float radius = 0.5f;
    //
    //    float circleProgress = (float) dt;
    //
    //    if (circleProgress > 0) {
    //
    //        Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) (circleProgress * 20), center, 360*circleProgress, 180);
    //
    //        HUDRenderer.drawArc(center, arcPoints, buffer, 0xFFFFFFFF);
    //    }
    //
    //    if (circleProgress < 1) {
    //        Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) ((1-circleProgress) * 20), center, 360*(1-circleProgress), 180+360*circleProgress);
    //
    //        HUDRenderer.drawArc(center, arcPoints, buffer, 0x7F7F7F7F);
    //    }
    //
    //    BuiltBuffer buff = buffer.endNullable();
    //    if (buff == null) return;
    //
    //    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    //    RenderSystem.enableBlend();
    //    RenderSystem.defaultBlendFunc();
    //    RenderSystem.disableCull();
    //    RenderSystem.enableDepthTest();
    //    buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
    //    BufferRenderer.drawWithGlobalProgram(buff);
    //    RenderSystem.disableDepthTest();
    //    RenderSystem.enableCull();
    //    RenderSystem.disableBlend();
    //    RenderSystem.depthMask(true);
    //
    //});

}
