package com.beatcraft.render.effect;

import com.beatcraft.BeatCraft;
import com.beatcraft.render.BeatCraftRenderer;
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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;

public class Bloomfog {

    @FunctionalInterface
    public interface QuadConsumer<T, U, S, V> {
        void accept(T t, U u, S s, V v);
    }

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
    private final ArrayList<QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> renderCalls = new ArrayList<>();
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

    public static ShaderProgram blurShaderUp;
    public static ShaderProgram blurShaderDown;
    public static ShaderProgram gaussianV;
    public static ShaderProgram gaussianH;
    public static ShaderProgram bloomfogSolidShader;
    public static ShaderProgram bloomfogLineShader;
    public static ShaderProgram bloomfogPositionColor;
    public static ShaderProgram bloomfogColorFix;
    public static ShaderProgram blueNoise;

    public static ShaderProgram blitShader;
    public static ShaderProgram compositeShader;

    private static final Identifier blueNoiseTexture = BeatCraft.id("textures/noise/blue_noise.png");

    private static final float radius = 10;

    private static final int LAYERS = 5;

    private Identifier[] pyramidTexIds = new Identifier[LAYERS];
    private SimpleFramebuffer[] pyramidBuffers = new SimpleFramebuffer[LAYERS];
    private SimpleFramebuffer[] pyramidBuffers2 = new SimpleFramebuffer[LAYERS];
    private BloomfogTex[] pyramidTextures = new BloomfogTex[LAYERS];

    public static void initShaders() {
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
            bloomfogSolidShader = new ShaderProgram(MinecraftClient.getInstance().getResourceManager(), "bloomfog_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// DO NOT CALL: use Bloomfog.create()
    /// The exception is MirrorHandler, which needs a second instance of bloomfog
    public Bloomfog(boolean initMirror) {

        if (initMirror) MirrorHandler.init();

        if (!initMirror) initShaders();

        framebuffer = new SimpleFramebuffer(1920, 1080, true, MinecraftClient.IS_SYSTEM_MAC);
        //pingPongBuffers[0] = new SimpleFramebuffer(1920, 1080, true, true);
        //pingPongBuffers[1] = new SimpleFramebuffer(1920, 1080, true, true);
        blurredBuffer = new SimpleFramebuffer(1920, 1080, true, MinecraftClient.IS_SYSTEM_MAC);
        extraBuffer = new SimpleFramebuffer(1920, 1080, true, MinecraftClient.IS_SYSTEM_MAC);

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
        for (i = 0; i < LAYERS; i++) {
            pyramidBuffers[i] = new SimpleFramebuffer(512, 512, false, MinecraftClient.IS_SYSTEM_MAC);
            pyramidBuffers2[i] = new SimpleFramebuffer(512, 512, false, MinecraftClient.IS_SYSTEM_MAC);
            //pyramidTexIds[i] = BeatCraft.id("bloomfog/pyramid" + i);
            //pyramidTextures[i] = new BloomfogTex(pyramidBuffers[i]);
            //texManager.registerTexture(pyramidTexIds[i], pyramidTextures[i]);
        }

    }


    private static Bloomfog INSTANCE = null;
    public static Bloomfog create() {
        if (INSTANCE == null) {
            INSTANCE = new Bloomfog(true);
        }
        return INSTANCE;
    }

    private float layers = 1;
    public void resize(int width, int height, boolean resizeMirror) {
        framebuffer.resize(width*2, height*2, true);
        blurredBuffer.resize(width*2, height*2, true);
        extraBuffer.resize(width*2, height*2, true);

        //double num = (Math.log((float) Math.max(width, height)) / Math.log(2f)) + Math.min(radius, 10f) - 10f;
        //int num2 = (int) Math.floor(num);
        layers = 4;//Math.clamp(num2, 1, 16);

        float mod = 2;
        for (int l = 0; l < layers; l++) {
            if ((int) (width / mod) > 0 && (int) (height / mod) > 0) {
                pyramidBuffers[l].resize((int) (width * 2 / mod), (int) (height * 2 / mod), MinecraftClient.IS_SYSTEM_MAC);
                pyramidBuffers2[l].resize((int) (width * 2 / mod), (int) (height * 2 / mod), MinecraftClient.IS_SYSTEM_MAC);
            }
            mod *= 2;
        }

        if (resizeMirror) MirrorHandler.resize();

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

    public void record(QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        renderCalls.add(call);
    }

    public static float calculateRoll(Vector3f up, Vector3f left) {
        return (float) Math.atan2(left.y, up.y);
    }

    private int[] lastSize = new int[]{1, 1};
    public void render(boolean isMirror, float tickDelta) {

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(true);

        MinecraftClient client = MinecraftClient.getInstance();
        var window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        if (window.getWidth() != lastSize[0] || window.getHeight() != lastSize[1]) {
            lastSize = new int[]{Math.max(1, window.getWidth()), Math.max(1, window.getHeight())};
            resize(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), true);
        }

        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = framebuffer;
        framebuffer.beginWrite(true);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        assert MinecraftClient.getInstance().player != null;
        float pitch = MinecraftClient.getInstance().player.getPitch(tickDelta);

        Vector3f up = camera.getVerticalPlane();
        Vector3f left = camera.getDiagonalPlane();

        float roll = Math.abs(pitch) >= 90 ? 0 : calculateRoll(up, left);

        var invCameraRotation = camera.getRotation().conjugate(new Quaternionf());

        Quaternionf rollQuat = new Quaternionf().rotationAxis(roll, new Vector3f(0, 0, 1));

        rollQuat.mul(invCameraRotation, invCameraRotation);

        SkyFogController.render(buffer, cameraPos, invCameraRotation);

        for (var call : renderCalls) {
            call.accept(buffer, cameraPos, invCameraRotation, false);
            MirrorHandler.recordMirrorLightDraw(call);
        }

        renderCalls.clear();

        var buff = buffer.endNullable();
        if (buff != null) {
            RenderSystem.setShader(() -> bloomfogLineShader);
            RenderSystem.lineWidth(window.getWidth()/225f);
            BufferRenderer.drawWithGlobalProgram(buff);
        }

        framebuffer.endWrite();
        BeatCraftRenderer.bloomfog.overrideBuffer = isMirror;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = isMirror ? overrideFramebuffer : null;

        MirrorHandler.invCameraRotation = invCameraRotation;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);

        applyPyramidBlur(isMirror);

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        float z = 0;
        buffer.vertex(-2, -2, z).texture(0.0f, 0.0f);
        buffer.vertex( 2, -2, z).texture(1.0f, 0.0f);
        buffer.vertex( 2,  2, z).texture(1.0f, 1.0f);
        buffer.vertex(-2,  2, z).texture(0.0f, 1.0f);


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

    public int getBloomfogColorAttachment() {
        return blurredBuffer.getColorAttachment();
    }

    private int secondaryBindTex = 0;

    private void applyPyramidBlur(boolean isMirror) {
        var current = framebuffer;
        int l;
        for (l = 0; l < layers; l++) {
            applyBlurPass(isMirror, current, pyramidBuffers[l], PassType.DOWNSAMPLE);
            applyBlurPass(isMirror, pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V);
            applyBlurPass(isMirror, pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H);
            //applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V);
            //applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H);
            //if (l > 0) {
            //    secondaryBindTex = pyramidBuffers[0].getColorAttachment();
            //    applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.COMP);
            //    applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.BLIT);
            //}

            current = pyramidBuffers[l];

        }

        applyBlurPass(isMirror, current, blurredBuffer, PassType.UPSAMPLE);
        applyBlurPass(isMirror, blurredBuffer, framebuffer, PassType.GAUSSIAN_V);
        //applyBlurPass(framebuffer, blurredBuffer, PassType.GAUSSIAN_H);
        //applyBlurPass(blurredBuffer, framebuffer, PassType.GAUSSIAN_V);
        applyBlurPass(isMirror, framebuffer, extraBuffer, PassType.GAUSSIAN_H);
        applyBlurPass(isMirror, extraBuffer, blurredBuffer, PassType.BLUE_NOISE);
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

    private void applyBlurPass(boolean isMirror, Framebuffer in, Framebuffer out, PassType pass) {

        out.setClearColor(0, 0, 0, 0);
        out.clear(true);
        out.beginWrite(true);
        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = out;

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

        BeatCraftRenderer.bloomfog.overrideBuffer = isMirror;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = isMirror ? overrideFramebuffer : null;

    }

    private void applyColorCorrectionPass(boolean isMirror, Framebuffer in, Framebuffer out) {
        out.setClearColor(0, 0, 0, 0);
        out.clear(true);
        out.beginWrite(true);
        BeatCraftRenderer.bloomfog.overrideBuffer = true;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = out;
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

        BeatCraftRenderer.bloomfog.overrideBuffer = isMirror;
        BeatCraftRenderer.bloomfog.overrideFramebuffer = isMirror ? overrideFramebuffer : null;
    }

}