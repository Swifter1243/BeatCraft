package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;

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
    public Framebuffer overrideFramebuffer = null;

    public SimpleFramebuffer framebuffer;
    private final ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> renderCalls = new ArrayList<>();
    private final Identifier textureId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/main");
    private final BloomfogTex tex;

    //private final SimpleFramebuffer[] pingPongBuffers = new SimpleFramebuffer[2];
    //private final Identifier[] pingPongTexIds = new Identifier[]{
    //    Identifier.of(BeatCraft.MOD_ID, "bloomfog/ping_pong_0"),
    //    Identifier.of(BeatCraft.MOD_ID, "bloomfog/ping_pong_1")
    //};
    //private final BloomfogTex[] pingPongTextures = new BloomfogTex[2];

    private final SimpleFramebuffer extraBuffer;
    public final SimpleFramebuffer blurredBuffer;
    private final Identifier blurredTexId = Identifier.of(BeatCraft.MOD_ID, "bloomfog/blurred");
    private BloomfogTex blurredTex;

    private final ShaderProgram blurShaderUp;
    private final ShaderProgram blurShaderDown;
    private final ShaderProgram gaussianV;
    private final ShaderProgram gaussianH;
    public static ShaderProgram bloomfog_solid_shader;
    public static ShaderProgram bloomfogLineShader;
    public static ShaderProgram bloomfogPositionColor;
    public static ShaderProgram bloomfogColorFix;
    public static ShaderProgram blueNoise;

    public static ShaderProgram blitShader;
    public static ShaderProgram compositeShader;

    private static final Identifier blueNoiseTexture = BeatCraft.id("textures/noise/blue_noise.png");

    private static final float radius = 10;

    private Identifier[] pyramidTexIds = new Identifier[16];
    private SimpleFramebuffer[] pyramidBuffers = new SimpleFramebuffer[16];
    private SimpleFramebuffer[] pyramidBuffers2 = new SimpleFramebuffer[16];
    private BloomfogTex[] pyramidTextures = new BloomfogTex[16];

    private Bloomfog() {

        try {
            blurShaderUp = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_upsample", VertexFormats.POSITION_TEXTURE_COLOR);
            blurShaderDown = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_downsample", VertexFormats.POSITION_TEXTURE_COLOR);
            gaussianV = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "gaussian_v", VertexFormats.POSITION_TEXTURE_COLOR);
            gaussianH = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "gaussian_h", VertexFormats.POSITION_TEXTURE_COLOR);
            blueNoise = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "blue_noise", VertexFormats.POSITION_TEXTURE_COLOR);
            blitShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "beatcraft_blit", VertexFormats.POSITION_TEXTURE_COLOR);
            compositeShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "composite", VertexFormats.POSITION_TEXTURE_COLOR);
            bloomfogPositionColor = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "col_bloomfog", VertexFormats.POSITION_COLOR);
            bloomfogLineShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_lines", VertexFormats.LINES);
            bloomfogColorFix = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_colorfix", VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        framebuffer = new SimpleFramebuffer(1920, 1080, true, true);
        //pingPongBuffers[0] = new SimpleFramebuffer(1920, 1080, true, true);
        //pingPongBuffers[1] = new SimpleFramebuffer(1920, 1080, true, true);
        blurredBuffer = new SimpleFramebuffer(1920, 1080, true, true);
        extraBuffer = new SimpleFramebuffer(1920, 1080, true, true);

        tex = new BloomfogTex(framebuffer);
        //pingPongTextures[0] = new BloomfogTex(pingPongBuffers[0]);
        //pingPongTextures[1] = new BloomfogTex(pingPongBuffers[1]);
        blurredTex = new BloomfogTex(blurredBuffer);
        var texManager = MinecraftClient.getInstance().getTextureManager();

        texManager.registerTexture(textureId, tex);
        //MinecraftClient.getInstance().getTextureManager().registerTexture(pingPongTexIds[0], pingPongTextures[0]);
        //MinecraftClient.getInstance().getTextureManager().registerTexture(pingPongTexIds[1], pingPongTextures[1]);
        texManager.registerTexture(blurredTexId, blurredTex);

        int i;
        for (i = 0; i < 16; i++) {
            pyramidBuffers[i] = new SimpleFramebuffer(512, 512, false, true);
            pyramidBuffers2[i] = new SimpleFramebuffer(512, 512, false, true);
            //pyramidTexIds[i] = BeatCraft.id("bloomfog/pyramid" + i);
            //pyramidTextures[i] = new BloomfogTex(pyramidBuffers[i]);
            //texManager.registerTexture(pyramidTexIds[i], pyramidTextures[i]);
        }

    }


    private static Bloomfog INSTANCE = null;
    public static Bloomfog create() {
        if (INSTANCE == null) {
            INSTANCE = new Bloomfog();
        }
        return INSTANCE;
    }

    private float layers = 1;
    public void resize(int width, int height) {
        framebuffer.resize(width, height, true);
        blurredBuffer.resize(width, height, true);
        extraBuffer.resize(width, height, true);

        //double num = (Math.log((float) Math.max(width, height)) / Math.log(2f)) + Math.min(radius, 10f) - 10f;
        //int num2 = (int) Math.floor(num);
        layers = 4;//Math.clamp(num2, 1, 16);

        float mod = 2;
        for (int l = 0; l < layers; l++) {
            if ((int) (width / mod) > 0 && (int) (height / mod) > 0) {
                pyramidBuffers[l].resize((int) (width / mod), (int) (height / mod), true);
                pyramidBuffers2[l].resize((int) (width / mod), (int) (height / mod), true);
            }
            mod *= 2;
        }

    }

    public void unload() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
        framebuffer.delete();
    }

    public int getTexture() {
        return framebuffer.getColorAttachment();
    }

    public Identifier getId() {
        return blurredTexId;
    }

    public void record(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        renderCalls.add(call);
    }

    public static float calculateRoll(Vector3f up, Vector3f left) {
        return (float) Math.atan2(left.y, up.y);
    }

    private int[] lastSize = new int[]{1, 1};
    public void render(float tickDelta) {

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(true);

        MinecraftClient client = MinecraftClient.getInstance();
        var window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        if (window.getWidth() != lastSize[0] || window.getHeight() != lastSize[1]) {
            lastSize = new int[]{Math.max(1, window.getWidth()), Math.max(1, window.getHeight())};
            resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()));
        }

        overrideBuffer = true;
        overrideFramebuffer = framebuffer;
        framebuffer.beginWrite(true);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        Vector3f up = camera.getVerticalPlane();
        Vector3f left = camera.getDiagonalPlane();

        float roll = calculateRoll(up, left);

        var invCameraRotation = camera.getRotation().conjugate(new Quaternionf());

        Quaternionf rollQuat = new Quaternionf().rotationAxis(roll, new Vector3f(0, 0, 1));

        rollQuat.mul(invCameraRotation, invCameraRotation);

        SkyFogController.render(buffer, cameraPos, invCameraRotation);

        for (var call : renderCalls) {
            call.accept(buffer, cameraPos, invCameraRotation);
        }

        renderCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(() -> bloomfogLineShader);
            RenderSystem.lineWidth(window.getWidth()/225f);
            BufferRenderer.drawWithGlobalProgram(buff);
        }

        framebuffer.endWrite();
        overrideBuffer = false;
        overrideFramebuffer = null;


        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);

        applyPyramidBlur();

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        float z = 0;
        buffer.vertex(-1, -1, z).texture(0.0f, 0.0f);
        buffer.vertex( 1, -1, z).texture(1.0f, 0.0f);
        buffer.vertex( 1,  1, z).texture(1.0f, 1.0f);
        buffer.vertex(-1,  1, z).texture(0.0f, 1.0f);


        var oldProjMat = RenderSystem.getProjectionMatrix();
        var oldVertexSort = RenderSystem.getVertexSorting();
        var orthoMatrix = new Matrix4f().ortho(0, width, height, 0, -1000, 1000);
        RenderSystem.setProjectionMatrix(orthoMatrix, VertexSorter.BY_DISTANCE);
        RenderSystem.setShaderTexture(0, blurredTexId);
        RenderSystem.enableBlend();

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setProjectionMatrix(oldProjMat, oldVertexSort);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();

    }

    public void loadTex() {
        RenderSystem.setShaderTexture(0, blurredBuffer.getColorAttachment());
    }

    public void loadTexSecondary() {
        RenderSystem.setShaderTexture(1, blurredBuffer.getColorAttachment());
    }

    private int secondaryBindTex = 0;

    private void applyPyramidBlur() {
        var current = framebuffer;
        int l;
        for (l = 0; l < layers; l++) {
            applyBlurPass(current, pyramidBuffers[l], PassType.DOWNSAMPLE);
            //applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V);
            //applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H);
            //applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V);
            //applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H);
            if (l > 0) {
                secondaryBindTex = pyramidBuffers[0].getColorAttachment();
                applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.COMP);
                applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.BLIT);
            }

            current = pyramidBuffers[l];

        }

        applyBlurPass(current, blurredBuffer, PassType.UPSAMPLE);
        applyBlurPass(blurredBuffer, framebuffer, PassType.GAUSSIAN_V);
        //applyBlurPass(framebuffer, blurredBuffer, PassType.GAUSSIAN_H);
        //applyBlurPass(blurredBuffer, framebuffer, PassType.GAUSSIAN_V);
        applyBlurPass(framebuffer, extraBuffer, PassType.GAUSSIAN_H);
        applyBlurPass(extraBuffer, blurredBuffer, PassType.BLUE_NOISE);
    }

    private void applyBlur() {
        //applyBlurPass(framebuffer, pingPongBuffers[0], false);
        //
        //int passes = 5;
        //
        //for (int i = 0; i < passes; i++) {
        //    applyBlurPass(pingPongBuffers[0], pingPongBuffers[1], false);
        //    applyBlurPass(pingPongBuffers[1], pingPongBuffers[0], false);
        //}
        //
        //for (int i = 0; i < passes; i++) {
        //    applyBlurPass(pingPongBuffers[0], pingPongBuffers[1], true);
        //    applyBlurPass(pingPongBuffers[1], pingPongBuffers[0], true);
        //}
        //
        //applyBlurPass(pingPongBuffers[0], pingPongBuffers[1], true);
        //
        //applyColorCorrectionPass(pingPongBuffers[1], blurredBuffer);

    }

    private enum PassType {
        DOWNSAMPLE,
        UPSAMPLE,
        GAUSSIAN_V,
        GAUSSIAN_H,
        BLUE_NOISE,
        BLIT,
        COMP
    }

    private void applyBlurPass(Framebuffer in, Framebuffer out, PassType pass) {

        out.setClearColor(0, 0, 0, 0);
        out.clear(true);
        out.beginWrite(true);
        overrideBuffer = true;
        overrideFramebuffer = out;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float w = (float) MinecraftClient.getInstance().getWindow().getWidth();
        float h = (float) MinecraftClient.getInstance().getWindow().getHeight();

        RenderSystem.setShaderTexture(0, in.getColorAttachment());

        var shader = switch (pass) {
            case DOWNSAMPLE -> blurShaderDown;
            case UPSAMPLE -> blurShaderUp;
            case GAUSSIAN_V -> gaussianV;
            case GAUSSIAN_H -> gaussianH;
            case BLUE_NOISE -> blueNoise;
            case BLIT -> blitShader;
            case COMP -> compositeShader;
        };

        RenderSystem.setShader(() -> shader);

        if (pass == PassType.BLUE_NOISE) {
            //shader.addSampler("Sampler1", blueNoiseTexture);
            RenderSystem.setShaderTexture(1, blueNoiseTexture);
            shader.getUniformOrDefault("texelSize").set(512f / w, 512f / h);
            //shader.getUniformOrDefault("GameTime").set(BeatCraftClient.random.nextFloat());
        } else if (pass == PassType.COMP) {
            shader.addSampler("Sampler1", secondaryBindTex);
            RenderSystem.setShaderTexture(1, secondaryBindTex);
        } else {
            shader.getUniformOrDefault("texelSize").set(radius / w, radius / h);
        }
        RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();

        float z = 0;
        buffer.vertex(new Vector3f(-1, -1, z)).texture(0, 0).color(0xFF020200);
        buffer.vertex(new Vector3f( 1, -1, z)).texture(1, 0).color(0xFF020200);
        buffer.vertex(new Vector3f( 1,  1, z)).texture(1, 1).color(0xFF020200);
        buffer.vertex(new Vector3f(-1,  1, z)).texture(0, 1).color(0xFF020200);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        out.endWrite();

        overrideBuffer = false;
        overrideFramebuffer = null;

    }

    private void applyColorCorrectionPass(Framebuffer in, Framebuffer out) {
        out.setClearColor(0, 0, 0, 0);
        out.clear(true);
        out.beginWrite(true);
        overrideBuffer = true;
        overrideFramebuffer = out;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float w = (float) MinecraftClient.getInstance().getWindow().getWidth();
        float h = (float) MinecraftClient.getInstance().getWindow().getHeight();
        float a2 = 0.02f * (h/w);
        float a =  0.02f;
        float precision = 7f/127f;

        RenderSystem.setShaderTexture(0, in.getColorAttachment());
        RenderSystem.setShader(() -> bloomfogColorFix);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float z = 0;
        buffer.vertex(new Vector3f(-1, -1, z)).texture(0, 0).color(0xFF020200).normal(a2, a, precision);
        buffer.vertex(new Vector3f( 1, -1, z)).texture(1, 0).color(0xFF020200).normal(a2, a, precision);
        buffer.vertex(new Vector3f( 1,  1, z)).texture(1, 1).color(0xFF020200).normal(a2, a, precision);
        buffer.vertex(new Vector3f(-1,  1, z)).texture(0, 1).color(0xFF020200).normal(a2, a, precision);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        out.endWrite();

        overrideBuffer = false;
        overrideFramebuffer = null;
    }

}