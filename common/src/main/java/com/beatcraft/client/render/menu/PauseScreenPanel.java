package com.beatcraft.client.render.menu;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.logic.InputSystem;
import com.beatcraft.client.menu.PauseMenu;
import com.beatcraft.client.render.HUDRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;

public class PauseScreenPanel extends MenuPanel<PauseMenu> {

    private static final Component MENU = Component.translatable("menu.beatcraft.paused.menu");
    private static final Component RESTART = Component.translatable("menu.beatcraft.paused.restart");
    private static final Component CONTINUE = Component.translatable("menu.beatcraft.paused.continue");

    public PauseScreenPanel(HUDRenderer hudRenderer) {
        super(new PauseMenu(hudRenderer));
        position.set(0, 1.5f, 6);
        size = new Vector2f(600, 150);

        widgets.clear();
        widgets.addAll(List.of(
            new ButtonWidget(
                new Vector3f(-160, 0, 0.02f), new Vector2f(130, 50),
                () -> {
                    try {
                        hudRenderer.controller.playRecorder.save();
                    } catch (IOException e) {
                        Beatcraft.LOGGER.error("Error saving recording", e);
                    }
                    hudRenderer.controller.difficulty = null;
                    hudRenderer.controller.info = null;
                    hudRenderer.controller.audio.close();
                    hudRenderer.controller.audio = null;
                    hudRenderer.controller.scene = HUDRenderer.MenuScene.SongSelect;
                    // BeatmapAudioPlayer.unmuteVanillaMusic();
                    InputSystem.unlockHotbar();
                    // HUDRenderer.sendSceneSync();
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget(MENU, new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(0, 0, 0.02f), new Vector2f(150, 50),
                () -> {
                    hudRenderer.controller.logic.reset();
                    hudRenderer.controller.restart();
                    hudRenderer.controller.scene = HUDRenderer.MenuScene.InGame;
                    InputSystem.lockHotbar();
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget(RESTART, new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(160, 0, 0.02f), new Vector2f(150, 50),
                () -> {
                    hudRenderer.controller.resume();
                    hudRenderer.controller.scene = HUDRenderer.MenuScene.InGame;
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget(CONTINUE, new Vector3f(0, -11, 0.05f)).withScale(3)
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
