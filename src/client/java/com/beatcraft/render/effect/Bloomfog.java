package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class Bloomfog {

    public static class BloomfogTex extends AbstractTexture {

        private final SimpleFramebuffer buffer;
        protected BloomfogTex(SimpleFramebuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int getGlId() {
            return buffer.getColorAttachment();
        }

        @Override
        public void load(ResourceManager manager) throws IOException {

        }
    }

    public boolean overrideBuffer = false;
    public SimpleFramebuffer framebuffer = null;
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> renderCalls = new ArrayList<>();
    private final Identifier textureId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/" + UUID.randomUUID());
    private final BloomfogTex tex;

    public Bloomfog() {
        framebuffer = new SimpleFramebuffer(1920, 1080, true, true);

        tex = new BloomfogTex(framebuffer);

        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, tex);
    }

    public void resize(int width, int height) {
        framebuffer.resize(width, height, true);
    }

    public void unload() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
        framebuffer.delete();
    }

    public int getTexture() {
        return framebuffer.getColorAttachment();
    }

    public void record(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        renderCalls.add(call);
    }

    public void render(float tickDelta) {

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(true);

        var window = MinecraftClient.getInstance().getWindow();

        overrideBuffer = true;
        framebuffer.beginWrite(true);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        var invCameraRotation = MinecraftClient.getInstance().gameRenderer.getCamera().getRotation().conjugate(new Quaternionf());

        for (var call : renderCalls) {
            call.accept(buffer, cameraPos, invCameraRotation);
        }

        renderCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            BufferRenderer.drawWithGlobalProgram(buff);
        }

        framebuffer.endWrite();
        overrideBuffer = false;
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer renderer = client.gameRenderer;

        float aspectRatio = (float) window.getScaledWidth() / (float) window.getScaledHeight();

        float fov = (float) Math.toRadians(renderer.getFov(renderer.getCamera(), tickDelta, false));

        float quadHeight = 2.0f * (float) Math.tan(fov / 2.0f);
        float quadWidth = quadHeight * aspectRatio;

        buffer.vertex(-quadWidth / 2, -quadHeight / 2, -0.5f).texture(0.0f, 0.0f); // Top-left
        buffer.vertex( quadWidth / 2, -quadHeight / 2, -0.5f).texture(1.0f, 0.0f); // Top-right
        buffer.vertex( quadWidth / 2,  quadHeight / 2, -0.5f).texture(1.0f, 1.0f); // Bottom-right
        buffer.vertex(-quadWidth / 2,  quadHeight / 2, -0.5f).texture(0.0f, 1.0f); // Bottom-left


        RenderSystem.setShaderTexture(0, textureId);
        BufferRenderer.drawWithGlobalProgram(buffer.end());


        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);

    }

}