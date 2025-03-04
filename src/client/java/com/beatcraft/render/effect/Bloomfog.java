package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.vivecraft.client_vr.render.VRShaders;

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
    public SimpleFramebuffer framebuffer;
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> renderCalls = new ArrayList<>();
    private final Identifier textureId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/main");
    private final BloomfogTex tex;

    private SimpleFramebuffer[] pingPongBuffers = new SimpleFramebuffer[2];
    private final Identifier[] pingPongTexIds = new Identifier[]{
            Identifier.of(BeatCraft.MOD_ID, "bloomfog/ping_pong_0"),
            Identifier.of(BeatCraft.MOD_ID, "bloomfog/ping_pong_1")
    };
    private final BloomfogTex[] pingPongTextures = new BloomfogTex[2];

    private SimpleFramebuffer blurredBuffer;
    private final Identifier blurredTexId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/blurred");
    private BloomfogTex blurredTex;

    private ShaderProgram blurShaderH;
    private ShaderProgram blurShaderV;

    //private final Uniform vTexSize;
    //private final Uniform hTexSize;

    private Bloomfog() {

        try {
            blurShaderH = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_blur_h", VertexFormats.POSITION_TEXTURE);
            blurShaderV = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_blur_v", VertexFormats.POSITION_TEXTURE);
            //hTexSize = blurShaderH.getUniform("texSize");
            //vTexSize = blurShaderV.getUniform("texSize");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        framebuffer = new SimpleFramebuffer(1920, 1080, true, true);

        pingPongBuffers[0] = new SimpleFramebuffer(1920/4, 1080/4, true, true);
        pingPongBuffers[1] = new SimpleFramebuffer(1920, 1080, true, true);
        blurredBuffer = new SimpleFramebuffer(1920, 1080, true, true);

        tex = new BloomfogTex(framebuffer);
        pingPongTextures[0] = new BloomfogTex(pingPongBuffers[0]);
        pingPongTextures[1] = new BloomfogTex(pingPongBuffers[1]);
        blurredTex = new BloomfogTex(blurredBuffer);

        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, tex);
        MinecraftClient.getInstance().getTextureManager().registerTexture(pingPongTexIds[0], pingPongTextures[0]);
        MinecraftClient.getInstance().getTextureManager().registerTexture(pingPongTexIds[1], pingPongTextures[1]);
        MinecraftClient.getInstance().getTextureManager().registerTexture(blurredTexId, blurredTex);

    }


    private static Bloomfog INSTANCE = null;
    public static Bloomfog create() {
        if (INSTANCE == null) {
            INSTANCE = new Bloomfog();
        }
        return INSTANCE;
    }

    public void resize(int width, int height) {
        framebuffer.resize(width, height, true);
        pingPongBuffers[1].resize(width, height, true);
        blurredBuffer.resize(width, height, true);
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

    public void render() {

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


        var oldProjMat = RenderSystem.getProjectionMatrix();
        var oldSort = RenderSystem.getVertexSorting();

        Matrix4f orthoMatrix = new Matrix4f();
        orthoMatrix.ortho(-0.5f, 0.5f, -0.5f, 0.5f, 1, -1);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        MinecraftClient client = MinecraftClient.getInstance();
        GameRenderer renderer = client.gameRenderer;

        float aspectRatio = (float) window.getScaledWidth() / (float) window.getScaledHeight();
        float fov = (float) Math.toRadians(renderer.getFov(renderer.getCamera(), 0, true));


        int prevMinFilter = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        int prevMagFilter = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);

        //BeatCraft.LOGGER.info("Player FOV: {}", fov);
        float quadHeight = (float) Math.tan(fov / 2.0f);
        float quadWidth = quadHeight * aspectRatio;

        applyBlur(quadWidth, quadHeight);

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);



        buffer.vertex(-quadWidth / 2, -quadHeight / 2, -0.5f).texture(0.0f, 0.0f); // Top-left
        buffer.vertex( quadWidth / 2, -quadHeight / 2, -0.5f).texture(1.0f, 0.0f); // Top-right
        buffer.vertex( quadWidth / 2,  quadHeight / 2, -0.5f).texture(1.0f, 1.0f); // Bottom-right
        buffer.vertex(-quadWidth / 2,  quadHeight / 2, -0.5f).texture(0.0f, 1.0f); // Bottom-left


        RenderSystem.setShaderTexture(0, blurredTexId);
        RenderSystem.enableBlend();

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setProjectionMatrix(oldProjMat, oldSort);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, prevMinFilter);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, prevMagFilter);

    }

    private void applyBlur(float width, float height) {
        // blur input to ping-pong
        // blur ping-pong back and forth a couple times
        // blur ping-pong to output


        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        //Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        //Quaternionf cameraRot = MinecraftClient.getInstance().gameRenderer.getCamera().getRotation().conjugate(new Quaternionf());

        applyBlurPass(framebuffer, pingPongBuffers[0], width, height, true);

        for (int i = 0; i < 1; i++) {
            applyBlurPass(pingPongBuffers[0], pingPongBuffers[1], width, height, false);
            applyBlurPass(pingPongBuffers[1], pingPongBuffers[0], width, height, true);
        }

        applyBlurPass(pingPongBuffers[0], blurredBuffer, width, height, false);



    }

    private void applyBlurPass(Framebuffer in, Framebuffer out, float width, float height, boolean verticalPass) {

        out.setClearColor(0, 0, 0, 0);
        out.clear(true);
        out.beginWrite(true);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.setShaderTexture(0, in.getColorAttachment());
        RenderSystem.setShader(() -> verticalPass ? blurShaderV : blurShaderH);

        RenderSystem.enableBlend();


        buffer.vertex(new Vector3f(-width/2,  height/2, -0.5f)).texture(0, 0).color(width, height, 0, 0);
        buffer.vertex(new Vector3f( width/2,  height/2, -0.5f)).texture(1, 0).color(width, height, 0, 0);
        buffer.vertex(new Vector3f( width/2, -height/2, -0.5f)).texture(1, 1).color(width, height, 0, 0);
        buffer.vertex(new Vector3f(-width/2, -height/2, -0.5f)).texture(0, 1).color(width, height, 0, 0);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        out.endWrite();
    }

}