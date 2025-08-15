package com.beatcraft.client.render.effect;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.gl.GlUtil;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMesh;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.client.services.VivecraftClientInterface;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL31;
//import org.vivecraft.client_vr.ClientDataHolderVR;
//import org.vivecraft.client_vr.render.RenderPass;
//import org.vivecraft.client_vr.render.helpers.RenderHelper;

import java.io.IOException;
import java.util.ArrayList;

public class Bloomfog {

    @FunctionalInterface
    public interface QuadConsumer<T, U, S, V> {
        void accept(T t, U u, S s, V v);
    }

    public static class BloomfogTex extends AbstractTexture {

        private final TextureTarget buffer;
        protected BloomfogTex(TextureTarget buffer) {
            this.buffer = buffer;
        }

        @Override
        public int getId() {
            return buffer.getColorTextureId();
        }

        @Override
        public void load(ResourceManager manager) throws IOException {

        }
    }

    public boolean overrideBuffer = false;
    public RenderTarget overrideFramebuffer = null;

    public TextureTarget framebuffer;
    private final ArrayList<QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> renderCalls = new ArrayList<>(); // Line renders
    private final ArrayList<QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean>> renderCalls2 = new ArrayList<>(); // Quad renders
    private final ResourceLocation textureId = Beatcraft.id("bloomfog/main");
    private final BloomfogTex tex;

    //private final SimpleRenderTarget[] pingPongBuffers = new SimpleRenderTarget[2];
    //private final ResourceLocation[] pingPongTexIds = new ResourceLocation[]{
    //    ResourceLocation.of(Beatcraft.MOD_ID, "bloomfog/ping_pong_0"),
    //    ResourceLocation.of(Beatcraft.MOD_ID, "bloomfog/ping_pong_1")
    //};
    //private final BloomfogTex[] pingPongTextures = new BloomfogTex[2];

    public final TextureTarget extraBuffer;
    public final TextureTarget blurredBuffer;
    private final ResourceLocation blurredTexId = Beatcraft.id("bloomfog/blurred");
    private BloomfogTex blurredTex;

    public static ShaderInstance blurShaderUp;
    public static ShaderInstance blurShaderDown;
    public static ShaderInstance gaussianV;
    public static ShaderInstance gaussianH;
    //public static ShaderInstance bloomfogSolidShader; //
    public static ShaderInstance bloomfogLineShader;
    public static ShaderInstance bloomfogPositionColor; //
    public static ShaderInstance bloomfogColorFix;
    public static ShaderInstance blueNoise;
    //public static ShaderInstance lightsPositionColorShader; //
    public static ShaderInstance backlightsPositionColorShader; //

    public static ShaderInstance blitShader;
    public static ShaderInstance compositeShader;

    public static ShaderInstance bloomMaskLightShader; //
    public static ShaderInstance bloomMaskLightTextureShader;

    private static final ResourceLocation blueNoiseTexture = Beatcraft.id("textures/noise/blue_noise.png");

    public ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> bloomCalls = new ArrayList<>();
    public ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> noteBloomCalls = new ArrayList<>();
    public ArrayList<TriConsumer<BufferBuilder, Vector3f, Quaternionf>> arrowBloomCalls = new ArrayList<>();
    public ArrayList<TriConsumer<Vector3f, Quaternionf, Integer>> miscBloomCalls = new ArrayList<>();
    public static TextureTarget bloomInput;
    private static TextureTarget bloomSwap;
    public static TextureTarget bloomOutput;

    public static TextureTarget lightDepth;

    private static float radius = 11;

    private static final int LAYERS = 10;

    private ResourceLocation[] pyramidTexIds = new ResourceLocation[LAYERS];
    private TextureTarget[] pyramidBuffers = new TextureTarget[LAYERS];
    private TextureTarget[] pyramidBuffers2 = new TextureTarget[LAYERS];
    private BloomfogTex[] pyramidTextures = new BloomfogTex[LAYERS];

    private static int arrowShaderProgram = 0;

    public static void initShaders() {
        try {
            blurShaderUp = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "bloomfog_upsample", DefaultVertexFormat.POSITION_TEX_COLOR);
            blurShaderDown = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "bloomfog_downsample", DefaultVertexFormat.POSITION_TEX_COLOR);
            gaussianV = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "gaussian_v", DefaultVertexFormat.POSITION_TEX_COLOR);
            gaussianH = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "gaussian_h", DefaultVertexFormat.POSITION_TEX_COLOR);
            blueNoise = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "blue_noise", DefaultVertexFormat.POSITION_TEX_COLOR);
            blitShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "Beatcraft_blit", DefaultVertexFormat.POSITION_TEX_COLOR);
            compositeShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "composite", DefaultVertexFormat.POSITION_TEX_COLOR);
            /**/bloomfogPositionColor = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "col_bloomfog", DefaultVertexFormat.POSITION_COLOR);
            // /**/lightsPositionColorShader = new ShaderProgram(Minecraft.getInstance().getResourceManager(), "lights_position_color", VertexFormats.POSITION_COLOR);
            /**/backlightsPositionColorShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "backlights_position_color", DefaultVertexFormat.POSITION_COLOR);
            bloomfogLineShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "bloomfog_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL);
            bloomfogColorFix = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "bloomfog_colorfix", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            // /**/bloomfogSolidShader = new ShaderProgram(Minecraft.getInstance().getResourceManager(), "bloomfog_solid", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            /**/bloomMaskLightShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "position_color_bloom_mask", DefaultVertexFormat.POSITION_COLOR);
            bloomMaskLightTextureShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "position_color_texture_bloom_mask", DefaultVertexFormat.POSITION_TEX_COLOR);

            var vertexShaderLoc = Beatcraft.id("shaders/instanced/arrow.vsh");
            var fragmentShaderLoc = Beatcraft.id("shaders/instanced/arrow_bloom_mask.fsh");

            arrowShaderProgram = GlUtil.createShaderProgram(vertexShaderLoc, fragmentShaderLoc);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static float[] getFogHeights() {
        return BeatmapManager.getAverageFogHeight();
    }

    /// DO NOT CALL: use Bloomfog.create()
    /// The exception is MirrorHandler, which needs a second instance of bloomfog
    public Bloomfog(boolean initMirror) {

        if (initMirror) MirrorHandler.init();

        if (!initMirror) initShaders();

        framebuffer = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);
        //pingPongBuffers[0] = new SimpleRenderTarget(1920, 1080, true, true);
        //pingPongBuffers[1] = new SimpleRenderTarget(1920, 1080, true, true);
        blurredBuffer = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);
        extraBuffer = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);
        bloomInput = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);
        bloomSwap = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);
        bloomOutput = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);

        lightDepth = new TextureTarget(1920, 1080, true, Minecraft.ON_OSX);

        tex = new BloomfogTex(framebuffer);
        //pingPongTextures[0] = new BloomfogTex(pingPongBuffers[0]);
        //pingPongTextures[1] = new BloomfogTex(pingPongBuffers[1]);
        blurredTex = new BloomfogTex(blurredBuffer);
        var texManager = Minecraft.getInstance().getTextureManager();

        texManager.register(textureId, tex);
        //Minecraft.getInstance().getTextureManager().registerTexture(pingPongTexIds[0], pingPongTextures[0]);
        //Minecraft.getInstance().getTextureManager().registerTexture(pingPongTexIds[1], pingPongTextures[1]);
        texManager.register(blurredTexId, blurredTex);

        int i;
        for (i = 0; i < LAYERS; i++) {
            pyramidBuffers[i] = new TextureTarget(512, 512, false, Minecraft.ON_OSX);
            pyramidBuffers2[i] = new TextureTarget(512, 512, false, Minecraft.ON_OSX);
            //pyramidTexIds[i] = Beatcraft.id("bloomfog/pyramid" + i);
            //pyramidTextures[i] = new BloomfogTex(pyramidBuffers[i]);
            //texManager.registerTexture(pyramidTexIds[i], pyramidTextures[i]);
        }

        LightMesh.buildMeshes();

    }


    private static Bloomfog INSTANCE = null;
    public static Bloomfog create() {
        if (INSTANCE == null) {
            INSTANCE = new Bloomfog(true);
        }
        return INSTANCE;
    }

    public static boolean bloomfog_resize = false;

    private float layers = 1;
    public void resize(int width, int height, boolean resizeMirror) {
        bloomfog_resize = true;
        framebuffer.resize(width*2, height*2, Minecraft.ON_OSX);
        blurredBuffer.resize(width*2, height*2, Minecraft.ON_OSX);
        extraBuffer.resize(width*2, height*2, Minecraft.ON_OSX);

        bloomInput.resize(width, height, Minecraft.ON_OSX);
        bloomSwap.resize(width, height, Minecraft.ON_OSX);
        bloomOutput.resize(width, height, Minecraft.ON_OSX);

        lightDepth.resize(width, height, Minecraft.ON_OSX);

        //double num = (Math.log((float) Math.max(width, height)) / Math.log(2f)) + Math.min(radius, 10f) - 10f;
        //int num2 = (int) Math.floor(num);
        layers = 7;//Math.clamp(num2, 1, 10);
        //Beatcraft.LOGGER.info("Layers: {}", layers);

        float mod = 2;
        for (int l = 0; l < layers; l++) {
            if ((int) (width / mod) > 0 && (int) (height / mod) > 0) {
                pyramidBuffers[l].resize((int) (width * 2 / mod), (int) (height * 2 / mod), Minecraft.ON_OSX);
                pyramidBuffers2[l].resize((int) (width * 2 / mod), (int) (height * 2 / mod), Minecraft.ON_OSX);
            }
            mod *= 2;
        }

        if (resizeMirror) MirrorHandler.resize();
        bloomfog_resize = false;
    }

    public void unload() {
        Minecraft.getInstance().getTextureManager().release(textureId);
        framebuffer.destroyBuffers();
    }

    public int getTexture() {
        return framebuffer.getColorTextureId();
    }

    public ResourceLocation getId() {
        return blurredTexId;
    }

    public void record(QuadConsumer<BufferBuilder, Vector3f, Quaternionf, Boolean> call) {
        renderCalls.add(call);
    }

    public static float calculateRoll(Vector3f up, Vector3f left) {
        return (float) Math.atan2(left.y, up.y);
    }

    private Quaternionf invCameraRotation = new Quaternionf();

    private int[] lastSize = new int[]{1, 1};
    public void render(boolean isMirror, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        var window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        Vector3f cameraPos = camera.getPosition().toVector3f();
        if (VivecraftClientInterface.isVRNonNull()) {
            var rotationMatrix = VivecraftClientInterface.getVRModelView();
            invCameraRotation = rotationMatrix.getUnnormalizedRotation(new Quaternionf());
        } else {
            invCameraRotation = camera.rotation().conjugate(new Quaternionf());
            float pitch = camera.getXRot();
            Vector3f up = camera.getUpVector();
            Vector3f left = camera.getLeftVector();
            float roll = Math.abs(pitch) >= 90 ? 0 : calculateRoll(up, left);
            Quaternionf rollQuat = new Quaternionf().rotationAxis(roll, new Vector3f(0, 0, 1));
            rollQuat.mul(invCameraRotation, invCameraRotation);
        }

        if (!BeatcraftClient.playerConfig.quality.doBloomfog) {
            return;
        }

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(Minecraft.ON_OSX);


        if (width != lastSize[0] || height != lastSize[1]) {
            lastSize = new int[]{Math.max(1, width), Math.max(1, height)};
            resize(Math.max(1, width), Math.max(1, height), true);
        }

        Minecraft.getInstance().getMainRenderTarget().unbindWrite();
        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = framebuffer;
        framebuffer.bindWrite(true);


        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (var call : renderCalls) {
            call.accept(buffer, cameraPos, invCameraRotation, false);
            //MirrorHandler.recordMirrorLightDraw(call);
        }
        renderCalls.clear();
        var buff = buffer.build();
        if (buff != null) {
            RenderSystem.setShader(() -> bloomfogLineShader);
            RenderSystem.lineWidth(window.getWidth()/225f);
            BufferUploader.drawWithShader(buff);
        }

        LightMesh.renderAllBloomfog();


        framebuffer.unbindWrite();

        BeatcraftRenderer.bloomfog.overrideBuffer = isMirror;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = isMirror ? overrideFramebuffer : null;

        MirrorHandler.invCameraRotation = invCameraRotation;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);


        applyPyramidBlur(isMirror);

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float z = 0;
        buffer.addVertex(-2, -2, z).setUv(0.0f, 0.0f);
        buffer.addVertex( 2, -2, z).setUv(1.0f, 0.0f);
        buffer.addVertex( 2,  2, z).setUv(1.0f, 1.0f);
        buffer.addVertex(-2,  2, z).setUv(0.0f, 1.0f);


        var oldProjMat = RenderSystem.getProjectionMatrix();
        var oldVertexSort = RenderSystem.getVertexSorting();
        var orthoMatrix = new Matrix4f().ortho(0, width, height, 0, -1000, 1000);
        RenderSystem.setProjectionMatrix(orthoMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.setShaderTexture(0, blurredTexId);
        RenderSystem.enableBlend();

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.setProjectionMatrix(oldProjMat, oldVertexSort);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    public void loadTex() {
        RenderSystem.setShaderTexture(0, blurredBuffer.getColorTextureId());
    }

    public void loadTexSecondary() {
        RenderSystem.setShaderTexture(1, blurredBuffer.getColorTextureId());
    }

    public int getBloomfogColorAttachment() {
        return blurredBuffer.getColorTextureId();
    }

    private int secondaryBindTex = 0;

    private void applyPyramidBlur(boolean isMirror) {
        var current = framebuffer;
        int l;
        for (l = 0; l < layers; l++) {
            applyEffectPass(isMirror, current, pyramidBuffers[l], PassType.DOWNSAMPLE, true);
            //if (l == layers-1) {
            //    applyEffectPass(isMirror, pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V, true);
            //    applyEffectPass(isMirror, pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H, true);
            //}
            //applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.GAUSSIAN_V);
            //applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.GAUSSIAN_H);
            //if (l > 0) {
            //    secondaryBindTex = pyramidBuffers[0].getColorAttachment();
            //    applyBlurPass(pyramidBuffers[l], pyramidBuffers2[l], PassType.COMP);
            //    applyBlurPass(pyramidBuffers2[l], pyramidBuffers[l], PassType.BLIT);
            //}

            current = pyramidBuffers[l];

        }

        applyEffectPass(isMirror, current, extraBuffer, PassType.UPSAMPLE, true);
        applyEffectPass(isMirror, extraBuffer, framebuffer, PassType.GAUSSIAN_V, true);
        applyEffectPass(isMirror, framebuffer, extraBuffer, PassType.GAUSSIAN_H, true);
        applyEffectPass(isMirror, extraBuffer, blurredBuffer, PassType.BLUE_NOISE, false);



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

    private void applyEffectPass(boolean isMirror, RenderTarget in, RenderTarget out, PassType pass) {
        applyEffectPass(isMirror, in, out, pass, false);
    }

    private void applyEffectPass(boolean isMirror, RenderTarget in, RenderTarget out, PassType pass, boolean overrideSampleMode) {

        out.setClearColor(0, 0, 0, 0);
        out.clear(Minecraft.ON_OSX);
        out.bindWrite(true);
        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = out;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float w = (float) Minecraft.getInstance().getWindow().getWidth();
        float h = (float) Minecraft.getInstance().getWindow().getHeight();

        RenderSystem.setShaderTexture(0, in.getColorTextureId());

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

        GlUtil.useProgram(shader.getId());
        GlUtil.setTex(shader.getId(), "Sampler0", 0, in.getColorTextureId());
        if (overrideSampleMode) {
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);
        } else {
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_NEAREST);
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_NEAREST);

        }
        if (pass == PassType.BLUE_NOISE) {
            //shader.addSampler("Sampler1", blueNoiseTexture);
            RenderSystem.setShaderTexture(1, blueNoiseTexture);
            shader.safeGetUniform("texelSize").set(512f / w, 512f / h);
            //shader.getUniformOrDefault("GameTime").set(BeatcraftClient.random.nextFloat());
        } else if (pass == PassType.COMP) {
            shader.setSampler("Sampler1", secondaryBindTex);
            RenderSystem.setShaderTexture(1, secondaryBindTex);
        } else {
            shader.safeGetUniform("texelSize").set(radius / w, radius / h);
        }
        RenderSystem.enableBlend();
        //RenderSystem.defaultBlendFunc();

        float z = 0;
        buffer.addVertex(new Vector3f(-1, -1, z)).setUv(0, 0).setColor(0xFF020200);
        buffer.addVertex(new Vector3f( 1, -1, z)).setUv(1, 0).setColor(0xFF020200);
        buffer.addVertex(new Vector3f( 1,  1, z)).setUv(1, 1).setColor(0xFF020200);
        buffer.addVertex(new Vector3f(-1,  1, z)).setUv(0, 1).setColor(0xFF020200);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        out.unbindWrite();

        BeatcraftRenderer.bloomfog.overrideBuffer = isMirror;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = isMirror ? overrideFramebuffer : null;

    }

    /// This passes a triangle:position-color buffer
    public void recordBloomCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        if (!BeatcraftClient.playerConfig.quality.doBloom) return;
        bloomCalls.add(call);
    }

    public void recordMiscBloomCall(TriConsumer<Vector3f, Quaternionf, Integer> call) {
        if (!BeatcraftClient.playerConfig.quality.doBloom) return;
        miscBloomCalls.add(call);
    }


    public void recordNoteBloomCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        if (!BeatcraftClient.playerConfig.quality.doBloom) return;
        noteBloomCalls.add(call);
    }

    public void recordArrowBloomCall(TriConsumer<BufferBuilder, Vector3f, Quaternionf> call) {
        if (!BeatcraftClient.playerConfig.quality.doBloom) return;
        arrowBloomCalls.add(call);
    }

    public static int sceneDepthBuffer;
    public void renderBloom() {

        if (!BeatcraftClient.playerConfig.quality.doBloom) {
            bloomCalls.clear();
            noteBloomCalls.clear();
            arrowBloomCalls.clear();
            miscBloomCalls.clear();

            MeshLoader.COLOR_NOTE_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.BOMB_NOTE_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.NOTE_ARROW_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.NOTE_DOT_INSTANCED_MESH.cancelBloomCalls();
            MeshLoader.CHAIN_DOT_INSTANCED_MESH.cancelBloomCalls();

            LightMesh.cancelBloomDraws();

            bloomOutput.setClearColor(0, 0, 0, 0);
            bloomOutput.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            return;
        }



        var old = new Matrix4f(RenderSystem.getModelViewMatrix());
        RenderSystem.getModelViewMatrix().identity();
        RenderSystem.disableCull();

        bloomInput.setClearColor(0, 0, 0, 0);
        bloomInput.clear(Minecraft.ON_OSX);
        sceneDepthBuffer = Minecraft.getInstance().getMainRenderTarget().getDepthTextureId();
        Minecraft.getInstance().getMainRenderTarget().unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindRead();
        BeatcraftRenderer.bloomfog.overrideBuffer = true;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = bloomInput;
        bloomInput.bindWrite(true);

        RenderSystem.defaultBlendFunc();

        Tesselator tessellator = Tesselator.getInstance();

        var window = Minecraft.getInstance().getWindow();

        var width = window.getWidth();
        var height = window.getHeight();

        Vector3f cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        // untextured renders
        var buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (var call : bloomCalls) {
            call.accept(buffer, cameraPos, invCameraRotation);
        }
        bloomCalls.clear();
        var buff = buffer.build();
        RenderSystem.disableDepthTest();

        if (buff != null) {
            Matrix4f worldTransform = new Matrix4f();
            worldTransform.translate(cameraPos);
            worldTransform.rotate(invCameraRotation.conjugate(new Quaternionf()));

            RenderSystem.setShader(() -> bloomMaskLightShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderTexture(0, sceneDepthBuffer);
            bloomMaskLightShader.setSampler("Sampler0", sceneDepthBuffer);
            bloomMaskLightShader.safeGetUniform("WorldTransform").set(worldTransform);
            bloomMaskLightShader.safeGetUniform("u_fog").set(getFogHeights());
            BufferUploader.drawWithShader(buff);
            RenderSystem.enableDepthTest();
        }

        // note-textured renders
        buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (var call : noteBloomCalls) {
            call.accept(buffer, cameraPos, invCameraRotation);
        }
        noteBloomCalls.clear();
        buff = buffer.build();
        if (buff != null) {
            RenderSystem.setShader(() -> bloomMaskLightTextureShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderTexture(0, MeshLoader.NOTE_TEXTURE);
            RenderSystem.setShaderTexture(1, sceneDepthBuffer);
            bloomMaskLightShader.setSampler("Sampler1", sceneDepthBuffer);
            bloomMaskLightShader.safeGetUniform("u_fog").set(getFogHeights());
            BufferUploader.drawWithShader(buff);
            RenderSystem.enableDepthTest();
        }

        // arrow-textured renders
        buffer = tessellator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (var call : arrowBloomCalls) {
            call.accept(buffer, cameraPos, invCameraRotation);
        }
        arrowBloomCalls.clear();
        buff = buffer.build();
        if (buff != null) {
            RenderSystem.setShader(() -> bloomMaskLightTextureShader);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            RenderSystem.setShaderTexture(0, MeshLoader.ARROW_TEXTURE);
            RenderSystem.setShaderTexture(1, sceneDepthBuffer);
            bloomMaskLightShader.setSampler("Sampler1", sceneDepthBuffer);
            bloomMaskLightShader.safeGetUniform("u_fog").set(getFogHeights());
            BufferUploader.drawWithShader(buff);
            RenderSystem.enableDepthTest();
        }

        for (var call : miscBloomCalls) {
            call.accept(cameraPos, invCameraRotation, sceneDepthBuffer);
        }
        miscBloomCalls.clear();


        MeshLoader.COLOR_NOTE_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.BOMB_NOTE_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.NOTE_ARROW_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.NOTE_DOT_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        MeshLoader.CHAIN_DOT_INSTANCED_MESH.render(cameraPos, invCameraRotation, arrowShaderProgram, sceneDepthBuffer);
        LightMesh.renderAllBloom(sceneDepthBuffer);

        bloomInput.unbindWrite();
        BeatcraftRenderer.bloomfog.overrideBuffer = false;
        BeatcraftRenderer.bloomfog.overrideFramebuffer = null;
        Minecraft.getInstance().getMainRenderTarget().unbindRead();

        var r = radius;
        radius = 3.25f;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

        applyEffectPass(false, bloomInput, bloomSwap, PassType.GAUSSIAN_H, true);
        applyEffectPass(false, bloomSwap, bloomOutput, PassType.GAUSSIAN_V, true);

        //applyEffectPass(false, bloomInput, bloomOutput, PassType.BLIT);

        radius = r;

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        RenderSystem.depthMask(false);

        buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);


        var cr = invCameraRotation.conjugate(new Quaternionf());
        float z = 0;
        buffer.addVertex(new Vector3f(-1, -1, z)).setUv(0.0f, 0.0f).setColor(0xFF020200);
        buffer.addVertex(new Vector3f( 1, -1, z)).setUv(1.0f, 0.0f).setColor(0xFF020200);
        buffer.addVertex(new Vector3f( 1,  1, z)).setUv(1.0f, 1.0f).setColor(0xFF020200);
        buffer.addVertex(new Vector3f(-1,  1, z)).setUv(0.0f, 1.0f).setColor(0xFF020200);


        RenderSystem.setShader(() -> blitShader);
        RenderSystem.setShaderTexture(0, bloomOutput.getColorTextureId());
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        var oldProjMat = RenderSystem.getProjectionMatrix();
        var oldVertexSort = RenderSystem.getVertexSorting();
        var orthoMatrix = new Matrix4f().ortho(0, width, height, 0, -1000, 1000);
        RenderSystem.setProjectionMatrix(orthoMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setProjectionMatrix(oldProjMat, oldVertexSort);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();

        RenderSystem.getModelViewMatrix().set(old);
    }

}