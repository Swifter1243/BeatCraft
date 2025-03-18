package com.beatcraft.render.effect;

import com.beatcraft.render.BeatcraftRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MirrorHandler {

    public static Quaternionf invCameraRotation = new Quaternionf();

    private static final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> drawCalls = new ArrayList<>();
    private static final ArrayList<Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> mirrorDraws = new ArrayList<>();
    public static SimpleFramebuffer mirrorFramebuffer;

    public static ShaderProgram mirrorShader;

    public static void init() {
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer = new SimpleFramebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);

        try {
            mirrorShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "light_mirror", VertexFormats.POSITION_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resize() {
        var window = MinecraftClient.getInstance().getWindow();
        mirrorFramebuffer.resize(window.getWidth(), window.getHeight(), true);
    }

    public static void recordMirrorLightDraw(Bloomfog.QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        mirrorDraws.add(call);
    }

    public static void recordCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        drawCalls.add(call);
    }


    public static void drawMirror() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        mirrorFramebuffer.setClearColor(0, 0, 0, 1);
        mirrorFramebuffer.clear(true);

        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = mirrorFramebuffer;
        mirrorFramebuffer.beginWrite(true);

        for (var call : mirrorDraws) {
            call.accept(buffer, cameraPos, invCameraRotation, true);
        }
        mirrorDraws.clear();
        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.depthMask(true);
            BufferRenderer.drawWithGlobalProgram(buff);
            RenderSystem.depthMask(false);

        }


        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        mirrorFramebuffer.endWrite();
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);


        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (var call : drawCalls) {
            call.accept(buffer, cameraPos, invCameraRotation.conjugate(new Quaternionf()));
        }
        drawCalls.clear();

        buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(() -> mirrorShader);
            RenderSystem.setShaderTexture(0, mirrorFramebuffer.getColorAttachment());
            mirrorShader.addSampler("Sampler0", mirrorFramebuffer.getColorAttachment());
            RenderSystem.setShaderTexture(1, mirrorFramebuffer.getDepthAttachment());
            mirrorShader.addSampler("Sampler1", mirrorFramebuffer.getDepthAttachment());

            BufferRenderer.drawWithGlobalProgram(buff);
        }

    }


}
