package com.beatcraft.render;


import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Rank;
import com.beatcraft.menu.EndScreenData;
import com.beatcraft.menu.ModifierMenu;
import com.beatcraft.menu.SongSelectMenu;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.render.menu.EndScreenPanel;
import com.beatcraft.render.menu.ModifierMenuPanel;
import com.beatcraft.render.menu.SongSelectMenuPanel;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.particle.ScoreDisplay;
import com.beatcraft.utils.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class HUDRenderer {

    public enum MenuScene {
        InGame,
        SongSelect,
        MainMenu,
        Downloader,
        EndScreen
    }

    public static MenuScene scene = MenuScene.SongSelect;
    public static NoteType pointerSaber = NoteType.BLUE;

    private static boolean showHUD = true;
    private static boolean advancedHUD = true;

    private static Vector3f leftHudPosition = new Vector3f(3, 1, 0);
    private static Vector3f rightHudPosition = new Vector3f(-3, 1, 0);
    private static Vector3f healthBarPosition = new Vector3f(0, -2, 0);

    private static Quaternionf leftHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static Quaternionf rightHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static Quaternionf healthBarOrientation = new Quaternionf().rotateZ((float) Math.PI);

    private static final Function<Float, Float> opacityEasing = Easing.getEasing("easeInExpo");

    public static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final int TEXT_LIGHT = 255;

    public static final SongSelectMenu songSelectMenu = new SongSelectMenu();
    private static final SongSelectMenuPanel songSelectMenuPanel = new SongSelectMenuPanel(songSelectMenu);

    public static final ModifierMenu modifierMenu = new ModifierMenu();
    private static final ModifierMenuPanel modifierMenuPanel = new ModifierMenuPanel(modifierMenu);

    public static final EndScreenPanel endScreenPanel = new EndScreenPanel(new EndScreenData(0, Rank.A, 0, 0, 0, 0));

    public static void postScore(int score, Vector3f position, Vector3f endpoint, Quaternionf orientation) {
        BeatcraftParticleRenderer.addParticle(new ScoreDisplay(score, position, endpoint, orientation));
    }

    public static VertexConsumerProvider vertexConsumerProvider;

    public static void render(VertexConsumerProvider immediate) {
        vertexConsumerProvider = immediate;

        switch (scene) {
            case InGame -> {
                renderGameHud(immediate);
            }
            case SongSelect -> {
                renderSongSelectHud(immediate);
            }
            case MainMenu -> {
            }
            case Downloader -> {
            }
            case EndScreen -> {
                renderEndScreen(immediate);
            }
        }

    }

    public static void renderGameHud(VertexConsumerProvider immediate) {

        if (!showHUD) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        MatrixStack matrices = new MatrixStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        matrices.translate(0, 0, 8);
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        matrices.push();
        matrices.translate(leftHudPosition.x, leftHudPosition.y, leftHudPosition.z);
        matrices.multiply(leftHudOrientation);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderRank(matrices, textRenderer, buffer, cameraPos, immediate);
        renderCombo(matrices, textRenderer, buffer, cameraPos, immediate);
        renderScore(matrices, textRenderer, buffer, cameraPos, immediate);
        renderAccuracy(matrices, textRenderer, buffer, cameraPos, immediate);
        matrices.pop();

        matrices.push();
        matrices.translate(rightHudPosition.x, rightHudPosition.y, rightHudPosition.z);
        matrices.multiply(rightHudOrientation);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderModifier(matrices, textRenderer, buffer, cameraPos, immediate);
        renderTime(matrices, textRenderer, buffer, cameraPos, immediate);
        matrices.pop();

        matrices.push();
        matrices.translate(healthBarPosition.x, healthBarPosition.y, healthBarPosition.z);
        matrices.multiply(healthBarOrientation);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderPlayerHealth(matrices, textRenderer, buffer, cameraPos, immediate);
        matrices.pop();

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
        BufferRenderer.drawWithGlobalProgram(buff);
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private static void renderSongSelectHud(VertexConsumerProvider immediate) {
        songSelectMenuPanel.render((VertexConsumerProvider.Immediate) immediate);
        modifierMenuPanel.render((VertexConsumerProvider.Immediate) immediate);
    }

    private static void renderEndScreen(VertexConsumerProvider immediate) {
        endScreenPanel.render((VertexConsumerProvider.Immediate) immediate);
    }


    private static void renderRank(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String rank = GameLogicHandler.getRank().toString();

        int w = textRenderer.getWidth(rank);

        matrices.push();
        matrices.scale(2, 2, 2);

        textRenderer.draw(
            Text.literal(rank),
            -w/2f, 12, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );

        matrices.pop();

    }

    private static void renderCombo(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        int w = textRenderer.getWidth("COMBO");

        textRenderer.draw(
            Text.literal("COMBO"),
            -w/2f, -28, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );

        matrices.push();
        matrices.translate(0, -31, 0);
        Vector3f topLine = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        matrices.pop();

        matrices.push();
        matrices.translate(0, -4, 0);
        Vector3f bottomLine = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());
        matrices.pop();

        float op = opacityEasing.apply(GameLogicHandler.getComboBarOpacity());

        if (op > 0) {
            float width = 0.5f * (1/op);
            int color = 0xFFFFFF + (((int) (255 * op)) << 24);

            buffer.vertex(topLine.x+width, topLine.y, topLine.z).color(color);
            buffer.vertex(topLine.x+width, topLine.y+0.05f, topLine.z).color(color);
            buffer.vertex(topLine.x-width, topLine.y+0.05f, topLine.z).color(color);
            buffer.vertex(topLine.x-width, topLine.y, topLine.z).color(color);

            buffer.vertex(bottomLine.x+width, bottomLine.y, bottomLine.z).color(color);
            buffer.vertex(bottomLine.x+width, bottomLine.y+0.05f, bottomLine.z).color(color);
            buffer.vertex(bottomLine.x-width, bottomLine.y+0.05f, bottomLine.z).color(color);
            buffer.vertex(bottomLine.x-width, bottomLine.y, bottomLine.z).color(color);


        }

        matrices.push();
        matrices.scale(1.5f, 1.5f, 1.5f);

        String combo = String.valueOf(GameLogicHandler.getCombo());
        w = textRenderer.getWidth(combo);

        textRenderer.draw(
            Text.literal(combo),
            -w/2f, -12, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );

        matrices.pop();
    }

    private static void renderScore(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String score = String.valueOf(GameLogicHandler.getScore());

        int w = textRenderer.getWidth(score);

        matrices.push();

        matrices.scale(1.2f, 1.2f, 1.2f);
        textRenderer.draw(
            Text.literal(score),
            -w/2f, 2, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );


        matrices.pop();

    }

    private static void renderAccuracy(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String accuracy = String.format("%.1f", GameLogicHandler.getAccuracy()*100) + "%";

        int w = textRenderer.getWidth(accuracy);

        matrices.push();

        matrices.scale(0.8f, 0.8f, 0.8f);
        textRenderer.draw(
            Text.literal(accuracy),
            -w/2f, 18, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );


        matrices.pop();

    }

    private static void drawArc(Vector3f center, Vector3f[] innerArc, BufferBuilder buffer, Vector3f cameraPos, int color) {
//        ArrayList<Vector3f[]> quads = new ArrayList<>();

        for (int i = 0; i < innerArc.length-1; i++) {
            Vector3f a = innerArc[i];
            Vector3f b = innerArc[i+1];
            Vector3f c = a.sub(center, new Vector3f()).normalize().mul(0.05f).add(a);
            Vector3f d = b.sub(center, new Vector3f()).normalize().mul(0.05f).add(b);

            buffer.vertex(a.x, a.y, a.z).color(color);
            buffer.vertex(b.x, b.y, b.z).color(color);
            buffer.vertex(d.x, d.y, d.z).color(color);
            buffer.vertex(c.x, c.y, c.z).color(color);
        }

    }

    private static void renderModifier(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String mod = String.valueOf(GameLogicHandler.getBonusModifier());

        //int w = textRenderer.getWidth(mod);

        textRenderer.draw(
            Text.literal("x"),
            -8.5f, -20, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );

        matrices.push();
        matrices.scale(2.5f, 2.5f, 2.5f);

        textRenderer.draw(
            Text.literal(mod),
            -1, -8, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH, 0, TEXT_LIGHT
        );

        matrices.pop();

        Vector3f center = matrices.peek().getPositionMatrix().getTranslation(new Vector3f()).add(0, 0.375f, 0);

        Vector3f normal = new Vector3f(0, 0, 1);
        float radius = 0.5f;

        float circleProgress = GameLogicHandler.getModifierPercentage();

        if (circleProgress > 0) {

            Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) (circleProgress * 20), center, 360*circleProgress, 180);

            drawArc(center, arcPoints, buffer, cameraPos, 0xFFFFFFFF);
        }

        if (circleProgress < 1) {
            Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) ((1-circleProgress) * 20), center, 360*(1-circleProgress), 180+360*circleProgress);

            drawArc(center, arcPoints, buffer, cameraPos, 0x7F7F7F7F);
        }

    }

    private static void renderTime(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {
        float songDuration = 0;
        if (BeatmapPlayer.currentInfo != null) {
            songDuration = BeatmapPlayer.currentInfo.getSongDuration();
        }
        float t = BeatmapPlayer.getCurrentSeconds();

        String currentTime = MathUtil.timeToString((int) t);
        String length = MathUtil.timeToString((int) songDuration);

        float progress = MathUtil.inverseLerp(0, songDuration, t);

        if (progress > 1 && BeatmapPlayer.currentInfo != null) {
            GameLogicHandler.triggerSongEnd();
        }

        String display = currentTime + " | " + length;

        int w = textRenderer.getWidth(display);

        Vector3f leftPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f()).add(0.65f, -0.4f, 0);

        Vector3f rightPos = leftPos.add(-1.3f, 0, 0, new Vector3f());

        Vector3f midPos = MathUtil.lerpVector3(leftPos, rightPos, progress);

        buffer.vertex(leftPos.x, leftPos.y, leftPos.z).color(0xFFFFFFFF);
        buffer.vertex(leftPos.x, leftPos.y+0.05f, leftPos.z).color(0xFFFFFFFF);
        buffer.vertex(midPos.x, midPos.y+0.05f, midPos.z).color(0xFFFFFFFF);
        buffer.vertex(midPos.x, midPos.y, midPos.z).color(0xFFFFFFFF);

        buffer.vertex(midPos.x, midPos.y, midPos.z).color(0x7F7F7F7F);
        buffer.vertex(midPos.x, midPos.y+0.05f, midPos.z).color(0x7F7F7F7F);
        buffer.vertex(rightPos.x, rightPos.y+0.05f, rightPos.z).color(0x7F7F7F7F);
        buffer.vertex(rightPos.x, rightPos.y, rightPos.z).color(0x7F7F7F7F);

        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);
        textRenderer.draw(
            Text.literal(display),
            -w/2f, 32, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0, TEXT_LIGHT
        );

        matrices.pop();
    }

    private static void renderPlayerHealth(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

    }


}
