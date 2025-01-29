package com.beatcraft.render;


import com.beatcraft.BeatCraft;
import com.beatcraft.animation.Easing;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.mixin_utils.BufferBuilderAccessible;
import com.beatcraft.render.effect.BeatcraftParticleRenderer;
import com.beatcraft.render.effect.Particle;
import com.beatcraft.utils.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class HUDRenderer {

    private static boolean showHUD = true;
    private static boolean advancedHUD = true;

    private static Vector3f leftHudPosition = new Vector3f(3, 1, 0);
    private static Vector3f rightHudPosition = new Vector3f(-3, 1, 0);
    private static Vector3f healthBarPosition = new Vector3f(0, -2, 0);

    private static Quaternionf leftHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static Quaternionf rightHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static Quaternionf healthBarOrientation = new Quaternionf().rotateZ((float) Math.PI);

    private static class ScoreDisplay implements Particle {

        private final int score;
        private double spawnTime;
        private Vector3f position;
        private Vector3f endPoint;

        public ScoreDisplay(int score, Vector3f position, Vector3f endPoint) {
            this.score = score;
            this.position = position;
            this.endPoint = endPoint;
            this.spawnTime = System.nanoTime() / 1_000_000_000d;
        }

        private static final Function<Float, Float> easing = Easing.getEasing("easeOutQuad");

        @Override
        public void update(float deltaTime, BufferBuilder buffer, Vector3f cameraPos) {

            float f = (float) MathUtil.inverseLerp(spawnTime, spawnTime+1.25d, System.nanoTime() / 1_000_000_000d);
            f = Math.clamp(f, 0, 1);
            Vector3f currentPos = MathUtil.lerpVector3(position, endPoint, easing.apply(f));



        }

        @Override
        public boolean shouldRemove() {
            float f = (float) MathUtil.inverseLerp(spawnTime, spawnTime+1.25d, System.nanoTime() / 1_000_000_000d);
            return f >= 1.25;
        }
    }

    public static void postScore(int score, Vector3f position, Vector3f endpoint) {
        BeatcraftParticleRenderer.addParticle(new ScoreDisplay(score, position, endpoint));
    }

    public static void render(VertexConsumerProvider immediate) {

        if (!showHUD) return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        MatrixStack matrices = new MatrixStack();


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
        buff.sortQuads(((BufferBuilderAccessible) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
        BufferRenderer.drawWithGlobalProgram(buff);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);


    }


    private static void renderRank(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String rank = GameLogicHandler.getRank().toString();

        int w = textRenderer.getWidth(rank);

        matrices.push();
        matrices.scale(2, 2, 2);

        textRenderer.draw(
            Text.literal(rank),
            -w/2f, 12, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );

        matrices.pop();

    }

    private static void renderCombo(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        int w = textRenderer.getWidth("COMBO");

        textRenderer.draw(
            Text.literal("COMBO"),
            -w/2f, -28, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );

        matrices.push();
        matrices.scale(1.5f, 1.5f, 1.5f);

        String combo = String.valueOf(GameLogicHandler.getCombo());
        w = textRenderer.getWidth(combo);

        textRenderer.draw(
            Text.literal(combo),
            -w/2f, -12, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
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
            -w/2f, 2, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );


        matrices.pop();

    }

    private static void renderAccuracy(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String accuracy = String.format("%.1f", GameLogicHandler.getAccuracy()) + "%";


        int w = textRenderer.getWidth(accuracy);

        matrices.push();

        matrices.scale(0.8f, 0.8f, 0.8f);
        textRenderer.draw(
            Text.literal(accuracy),
            -w/2f, 18, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );


        matrices.pop();

    }

    private static void renderModifier(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        String mod = String.valueOf(GameLogicHandler.getBonusModifier());

        //int w = textRenderer.getWidth(mod);

        textRenderer.draw(
            Text.literal("x"),
            -6, -20, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );

        matrices.push();
        matrices.scale(2.5f, 2.5f, 2.5f);

        textRenderer.draw(
            Text.literal(mod),
            0, -8, 0xFFFFFFFF, false,
            matrices.peek().getPositionMatrix(), immediate,
            TextRenderer.TextLayerType.NORMAL, 0, 0xFFFFFF
        );

        matrices.pop();
    }

    private static void renderTime(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

    }

    private static void renderPlayerHealth(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

    }


}
