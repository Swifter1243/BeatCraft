package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.DynamicTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
public class Bloomfog {

    protected static class BloomfogTex extends AbstractTexture {

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
    public SimpleFramebuffer framebuffer;
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> renderCalls = new ArrayList<>();
    private final Identifier textureId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/" + UUID.randomUUID());
    private final BloomfogTex tex;

    public Bloomfog() {
        framebuffer = new SimpleFramebuffer(256, 256, true, true);

        tex = new BloomfogTex(framebuffer);

        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, tex);
    }

    public void unload() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
        framebuffer.delete();
    }

    public void resize(int width, int height) {
        framebuffer.resize(width, height, true);
    }

    public int getTexture() {
        return framebuffer.getColorAttachment();
    }

    public void record(BiConsumer<BufferBuilder, Vector3f> call) {
        renderCalls.add(call);
    }

    public void render() {

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(true);

        var window = MinecraftClient.getInstance().getWindow();

        var right = (float) ((double) framebuffer.textureWidth / window.getScaleFactor());
        var bottom = (float) ((double) framebuffer.textureHeight / window.getScaleFactor());
        var mat4 = new Matrix4f().setOrtho(
                0, right, bottom, 0,
                1000, 21000
        );
        var oldProjMat = RenderSystem.getProjectionMatrix();
        var oldVertSort = RenderSystem.getVertexSorting();

        RenderSystem.setProjectionMatrix(mat4, VertexSorter.BY_Z);

        RenderSystem.getModelViewStack().pushMatrix().translation(0, 0, -11000);

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

        for (var call : renderCalls) {
            call.accept(buffer, new Vector3f());
        }

        var buff = buffer.endNullable();
        if (buff != null) {
            BufferRenderer.drawWithGlobalProgram(buff);
        }

        framebuffer.endWrite();
        overrideBuffer = false;
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.setProjectionMatrix(oldProjMat, oldVertSort);
        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        buffer.vertex(new Vector3f(0.5f, -0.5f, -1)).texture(0, 0);
        buffer.vertex(new Vector3f(0.5f, 0.5f, -1)).texture(0, 1);
        buffer.vertex(new Vector3f(-0.5f, 0.5f, -1)).texture(1, 1);
        buffer.vertex(new Vector3f(-0.5f, -0.5f, -1)).texture(1, 0);

        int t = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, textureId);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderTexture(0, t);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);

    }

}