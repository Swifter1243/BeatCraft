package com.beatcraft.render.menu;

import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.menu.PauseMenu;
import com.beatcraft.render.HUDRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PauseScreenPanel extends MenuPanel<PauseMenu> {
    public PauseScreenPanel() {
        super(new PauseMenu());
        position.set(0, 1.5f, 6);
        size = new Vector2f(600, 150);

        widgets.clear();
        widgets.addAll(List.of(
            new ButtonWidget(
                new Vector3f(-160, 0, 0.01f), new Vector2f(130, 50),
                () -> {
                    BeatmapPlayer.currentBeatmap = null;
                    BeatmapPlayer.currentInfo = null;
                    BeatmapAudioPlayer.unload();
                    HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                    BeatmapAudioPlayer.unmuteVanillaMusic();
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("MENU", new Vector3f(0, -11, -0.005f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(0, 0, 0.01f), new Vector2f(150, 50),
                () -> {
                    BeatmapPlayer.restart();
                    HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("RESTART", new Vector3f(0, -11, -0.005f)).withScale(3)
            ),
            new ButtonWidget(
                new Vector3f(160, 0, 0.01f), new Vector2f(150, 50),
                () -> {
                    CompletableFuture.runAsync(() -> {
                        HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
                        double start = System.nanoTime() / 1_000_000_000d;

                        while ((System.nanoTime() / 1_000_000_000d) - start < 1) {
                            double dt = 1-(System.nanoTime() / 1_000_000_000d);
                            if (!(HUDRenderer.scene == HUDRenderer.MenuScene.InGame)) {
                                return;
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

                        if (!(HUDRenderer.scene == HUDRenderer.MenuScene.InGame)) {
                            return;
                        }
                        BeatmapPlayer.play();

                    });
                },
                new HoverWidget(new Vector3f(), new Vector2f(150, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7F7F7F7F, 0x7F7F7F7F, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(150, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("CONTINUE", new Vector3f(0, -11, -0.005f)).withScale(3)
            )
        ));

    }


}
