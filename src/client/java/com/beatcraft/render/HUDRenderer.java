package com.beatcraft.render;


import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.Rank;
import com.beatcraft.memory.MemoryPool;
import com.beatcraft.menu.*;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.networking.c2s.SceneSyncC2SPayload;
import com.beatcraft.networking.s2c.SceneSyncS2CPayload;
import com.beatcraft.render.menu.*;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.beatcraft.render.particle.MenuPointerParticle;
import com.beatcraft.render.particle.ScoreDisplay;
import com.beatcraft.utils.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector2f;

import java.util.function.Function;

public class HUDRenderer {

    public enum MenuScene {
        InGame,
        SongSelect,
        MainMenu,
        Settings,
        Downloader,
        EndScreen,
        ConfirmSongDelete,
        Paused,
        SaberPreview,
    }

    public static MenuScene scene = MenuScene.SongSelect;
    public static NoteType pointerSaber = NoteType.BLUE;

    public static boolean triggerPressed = false;
    public static boolean triggerWasPressed = false;

    public static boolean showHUD = true;
    public static boolean advancedHUD = true;

    // In-game UI positioning
    private static final Vector3f leftHudPosition = new Vector3f(3, 1, 0);
    private static final Vector3f rightHudPosition = new Vector3f(-3, 1, 0);
    private static final Vector3f healthBarPosition = new Vector3f(0, -1.5f, 0);

    private static final Quaternionf leftHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static final Quaternionf rightHudOrientation = new Quaternionf().rotateZ((float) Math.PI);
    private static final Quaternionf healthBarOrientation = new Quaternionf().rotateZ((float) Math.PI);

    private static final Function<Float, Float> opacityEasing = Easing.getEasing("easeInExpo");

    private static final TextRenderer.TextLayerType TEXT_LAYER = TextRenderer.TextLayerType.NORMAL;

    public static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final int TEXT_LIGHT = 255;

    // Menu Panels
    public static final SongSelectMenu songSelectMenu = new SongSelectMenu();
    public static SongSelectMenuPanel songSelectMenuPanel = null;
    public static ErrorMessagePanel errorMessagePanel = new ErrorMessagePanel(new ErrorMessageMenu());

    public static final PauseScreenPanel pauseScreenPanel = new PauseScreenPanel();

    public static final ModifierMenu modifierMenu = new ModifierMenu();
    public static final ModifierMenuPanel modifierMenuPanel = new ModifierMenuPanel(modifierMenu);

    public static final EndScreenPanel endScreenPanel = new EndScreenPanel(new EndScreenData(0, Rank.A, 0, 0, 0, 0));

    public static ConfirmSongDeleteMenuPanel confirmSongDeleteMenuPanel = null;

    private static final SongDownloaderMenuPanel songDownloaderMenuPanel = new SongDownloaderMenuPanel();

    private static final SettingsMenuPanel settingsMenuPanel = new SettingsMenuPanel();

    private static final CreditsPanel creditsPanel = new CreditsPanel();

    public static boolean showKeyboard = false;
    private static final KeyboardMenu keyboardData = new KeyboardMenu(null);
    public static final KeyboardPanel keyboard = new KeyboardPanel(keyboardData);

    public static void sendSceneSync() {
        var s = (byte) scene.ordinal();
        ClientPlayNetworking.send(new SceneSyncC2SPayload(s));
    }

    public static void hookToKeyboard(TextInput input) {
        keyboardData.input = input;
        showKeyboard = true;
        errorMessagePanel.close();
    }

    public static void hideKeyboard() {
        showKeyboard = false;
    }

    public static void initSongSelectMenuPanel() {
        songSelectMenuPanel = new SongSelectMenuPanel(songSelectMenu);
    }

    public static void postScore(int score, Vector3f position, Vector3f endpoint, Quaternionf orientation) {
        if (!showHUD) return;
        BeatcraftParticleRenderer.addParticle(new ScoreDisplay(score, position, endpoint, orientation));
    }

    public static VertexConsumerProvider vertexConsumerProvider;

    public static void render(VertexConsumerProvider immediate) {
        vertexConsumerProvider = immediate;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        switch (scene) {
            case InGame -> {
                renderGameHud(immediate);
            }
            case SongSelect -> {
                renderSongSelectHud(immediate);
            }
            case Settings -> {
                renderSettings(immediate);
            }
            case MainMenu -> {
            }
            case Downloader -> {
                renderDownloader(immediate);
            }
            case EndScreen -> {
                renderEndScreen(immediate);
            }
            case ConfirmSongDelete -> {
                renderConfirmSongDelete(immediate);
            }
            case Paused -> {
                renderPauseScreen(immediate);
            }
            case SaberPreview -> {
                renderSaberPreviewScreen(immediate);
            }
        }

    }

    public static boolean isTriggerPressed() {
        boolean res = triggerPressed && !triggerWasPressed;
        HUDRenderer.triggerWasPressed = HUDRenderer.triggerPressed;
        return res;
    }

    public static void renderGameHud(VertexConsumerProvider immediate) {

        if ((!showHUD) || BeatCraftClient.playerConfig.isModifierActive("Zen Mode")) {
            renderTime(null, null, null, null, null);
            return;
        }


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        MatrixStack matrices = new MatrixStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        matrices.translate(0, 0, 7.99);
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

        BuiltBuffer buff = buffer.endNullable();
        if (buff == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        buff.sortQuads(((BufferBuilderAccessor) buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
        BufferRenderer.drawWithGlobalProgram(buff);
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();


        matrices.push();
        matrices.translate(healthBarPosition.x, healthBarPosition.y, healthBarPosition.z);
        matrices.multiply(healthBarOrientation);
        matrices.scale(1f/32f, 1f/32f, 1f/32f);
        renderPlayerHealth(matrices, textRenderer, cameraPos, immediate);
        matrices.pop();

    }

    private static void renderSongSelectHud(VertexConsumerProvider immediate) {
        if (songSelectMenuPanel == null) return;

        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;


        var pair = songSelectMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), songSelectMenuPanel.getNormal());

            local = pair.getRight();
        }

        songSelectMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        pair = modifierMenuPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), modifierMenuPanel.getNormal());
            local = pair.getRight();
        }

        modifierMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);


        pair = creditsPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), creditsPanel.getNormal());
            local = pair.getRight();
        }

        creditsPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);

            local = null;

            if (pair != null) {
                spawnMenuPointerParticle(pair.getLeft(), errorMessagePanel.getNormal());

                local = pair.getRight();
            }

            errorMessagePanel.render((VertexConsumerProvider.Immediate) immediate, local);
        }

    }

    private static void renderSettings(VertexConsumerProvider immediate) {
        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = settingsMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), settingsMenuPanel.getNormal());

            local = pair.getRight();
        }

        settingsMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);

            local = null;

            if (pair != null) {
                spawnMenuPointerParticle(pair.getLeft(), errorMessagePanel.getNormal());

                local = pair.getRight();
            }

            errorMessagePanel.render((VertexConsumerProvider.Immediate) immediate, local);
        }

        pair = modifierMenuPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), modifierMenuPanel.getNormal());
            local = pair.getRight();
        }

        modifierMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        pair = creditsPanel.raycast(saberPos, saberRot);

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), creditsPanel.getNormal());
            local = pair.getRight();
        }

        creditsPanel.render((VertexConsumerProvider.Immediate) immediate, local);


    }

    private static void renderDownloader(VertexConsumerProvider immediate) {
        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = songDownloaderMenuPanel.raycast(saberPos, saberRot);
        Vector2f local = null;
        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), songDownloaderMenuPanel.getNormal());
            local = pair.getRight();
        }
        songDownloaderMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        pair = modifierMenuPanel.raycast(saberPos, saberRot);
        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), modifierMenuPanel.getNormal());
            local = pair.getRight();
        }

        modifierMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);
        pair = creditsPanel.raycast(saberPos, saberRot);
        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), creditsPanel.getNormal());
            local = pair.getRight();
        }
        creditsPanel.render((VertexConsumerProvider.Immediate) immediate, local);

        if (errorMessagePanel.shouldDisplay()) {
            pair = errorMessagePanel.raycast(saberPos, saberRot);
            local = null;
            if (pair != null) {
                spawnMenuPointerParticle(pair.getLeft(), errorMessagePanel.getNormal());
                local = pair.getRight();
            }
            errorMessagePanel.render((VertexConsumerProvider.Immediate) immediate, local);
        } else if (showKeyboard) {
            pair = keyboard.raycast(saberPos, saberRot);
            local = null;
            if (pair != null) {
                spawnMenuPointerParticle(pair.getLeft(), keyboard.getNormal());
                local = pair.getRight();
            }
            keyboard.render((VertexConsumerProvider.Immediate) immediate, local);
        }

    }

    private static void renderEndScreen(VertexConsumerProvider immediate) {

        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = endScreenPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), endScreenPanel.getNormal());
            local = pair.getRight();
        }

        endScreenPanel.render((VertexConsumerProvider.Immediate) immediate, local);
    }

    private static void renderConfirmSongDelete(VertexConsumerProvider immediate) {
        if (confirmSongDeleteMenuPanel == null) return;
        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = confirmSongDeleteMenuPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), confirmSongDeleteMenuPanel.getNormal());
            local = pair.getRight();
        }

        confirmSongDeleteMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);
    }

    private static void renderPauseScreen(VertexConsumerProvider immediate) {
        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = pauseScreenPanel.raycast(saberPos, saberRot);

        Vector2f local = null;

        if (pair != null) {
            spawnMenuPointerParticle(pair.getLeft(), pauseScreenPanel.getNormal());
            local = pair.getRight();
        }

        pauseScreenPanel.render((VertexConsumerProvider.Immediate) immediate, local);
    }

    private static void renderSaberPreviewScreen(VertexConsumerProvider immediate) {
        var saberPos = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberPos : GameLogicHandler.leftSaberPos;
        var saberRot = pointerSaber == NoteType.BLUE ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation;

        var pair = modifierMenuPanel.raycast(saberPos, saberRot);

        Vector2f local;

        if (pair == null) {
            local = null;
        } else {
            spawnMenuPointerParticle(pair.getLeft(), modifierMenuPanel.getNormal());
            local = pair.getRight();
        }

        modifierMenuPanel.render((VertexConsumerProvider.Immediate) immediate, local);



    }

    private static void spawnMenuPointerParticle(Vector3f position, Vector3f normal) {
        BeatcraftParticleRenderer.addParticle(new MenuPointerParticle(position.add(normal.mul(0.01f, new Vector3f()), new Vector3f()), normal));
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
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.pop();

    }

    private static void renderCombo(MatrixStack matrices, TextRenderer textRenderer, BufferBuilder buffer, Vector3f cameraPos, VertexConsumerProvider immediate) {

        var txt = Text.translatable("hud.beatcraft.combo");
        int w = textRenderer.getWidth(txt.getString());

        textRenderer.draw(
            txt,
            -w/2f, -28, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TEXT_LAYER, 0, TEXT_LIGHT
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
            TEXT_LAYER, 0, TEXT_LIGHT
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
            TEXT_LAYER, 0, TEXT_LIGHT
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
            TEXT_LAYER, 0, TEXT_LIGHT
        );


        matrices.pop();

    }

    public static void drawArc(Vector3f center, Vector3f[] innerArc, BufferBuilder buffer, int color) {
        // ArrayList<Vector3f[]> quads = new ArrayList<>();

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
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.push();
        matrices.scale(2.5f, 2.5f, 2.5f);

        textRenderer.draw(
            Text.literal(mod),
            -1, -8, TEXT_COLOR, false,
            matrices.peek().getPositionMatrix(), immediate,
            TEXT_LAYER, 0, TEXT_LIGHT
        );

        matrices.pop();

        Vector3f center = matrices.peek().getPositionMatrix().getTranslation(new Vector3f()).add(0, 0.375f, 0);

        Vector3f normal = new Vector3f(0, 0, 1);
        float radius = 0.5f;

        float circleProgress = GameLogicHandler.getModifierPercentage();

        if (circleProgress > 0) {

            Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) (circleProgress * 20), center, 360*circleProgress, 180);

            drawArc(center, arcPoints, buffer, 0xFFFFFFFF);
        }

        if (circleProgress < 1) {
            Vector3f[] arcPoints = MathUtil.generateCircle(normal, radius, 2 + (int) ((1-circleProgress) * 20), center, 360*(1-circleProgress), 180+360*circleProgress);

            drawArc(center, arcPoints, buffer, 0x7F7F7F7F);
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

        if ((!showHUD) || BeatCraftClient.playerConfig.isModifierActive("Zen Mode")) return;

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
            TEXT_LAYER,
            0, TEXT_LIGHT
        );

        matrices.pop();
    }

    private static void renderPlayerHealth(MatrixStack matrices, TextRenderer textRenderer, Vector3f cameraPos, VertexConsumerProvider immediate) {
        float progress = GameLogicHandler.getHealthPercentage();

        switch (BeatCraftClient.playerConfig.getHealthStyle()) {
            case Classic -> {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);


                Vector3f leftPos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f()).add(1.35f, 0, 0);
                Vector3f rightPos = new Vector3f(leftPos.x - 2.7f, leftPos.y, leftPos.z);
                Vector3f midPos = MathUtil.lerpVector3(leftPos, rightPos, progress);

                buffer.vertex(leftPos.x, leftPos.y, leftPos.z).color(0xFFFFFFFF);
                buffer.vertex(leftPos.x, leftPos.y + 0.05f, leftPos.z).color(0xFFFFFFFF);
                buffer.vertex(midPos.x, midPos.y + 0.05f, midPos.z).color(0xFFFFFFFF);
                buffer.vertex(midPos.x, midPos.y, midPos.z).color(0xFFFFFFFF);

                buffer.vertex(midPos.x, midPos.y, midPos.z).color(0x7F7F7F7F);
                buffer.vertex(midPos.x, midPos.y + 0.05f, midPos.z).color(0x7F7F7F7F);
                buffer.vertex(rightPos.x, rightPos.y + 0.05f, rightPos.z).color(0x7F7F7F7F);
                buffer.vertex(rightPos.x, rightPos.y, rightPos.z).color(0x7F7F7F7F);


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
            case Hearts -> {
                // Style rules:
                // normal health amount:
                // - survival hearts / poison when in wall
                // - shake at <= 8 hp
                //
                // 4 lives:
                // - 4 hardcore hearts
                // - shake at 1 heart left
                //
                // 1 life:
                // - 1 hardcore heart, shaking

                var maxHp = GameLogicHandler.maxHealth;
                var hp = GameLogicHandler.health;
                Vector3f pos = matrices.peek().getPositionMatrix().getTranslation(new Vector3f());

                // color red channel is used to identify heart index
                var heart_buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                boolean doShaking = false;
                boolean inWall = GameLogicHandler.isInWall();

                switch (maxHp) {
                    case 1 -> {
                        doShaking = true;
                        if (hp == 1) {
                            drawHeart(heart_buffer, pos, 0, 1, 1, inWall);
                        }
                    }
                    case 4 -> {
                        if (hp <= 2) {
                            doShaking = true;
                        }
                        for (int i = 0; i < 4; i++) {
                            drawHeart(heart_buffer, pos, i, 4, progress, inWall);
                        }
                    }
                    default -> {
                        if (progress <= 0.275f) {
                            doShaking = true;
                        }
                        for (int i = 0; i < 10; i++) {
                            drawHeart(heart_buffer, pos, i, 10, progress, inWall);
                        }
                    }
                }

                var buff = heart_buffer.endNullable();
                if (buff != null) {

                    RenderSystem.setShader(() -> BeatCraftRenderer.heartHealthShader);
                    RenderSystem.setShaderTexture(0, heartsTexture);
                    BeatCraftRenderer.heartHealthShader.getUniformOrDefault("DoShaking").set(doShaking ? 1f : 0f);
                    BeatCraftRenderer.heartHealthShader.getUniformOrDefault("GameTime").set(System.nanoTime() / 1_000_000_000f);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableCull();
                    RenderSystem.enableDepthTest();
                    BufferRenderer.drawWithGlobalProgram(buff);
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableCull();
                    RenderSystem.disableBlend();
                }



            }
        }

    }

    private static final Vector2f HEART_SIZE = new Vector2f(0.25f);

    private static final Identifier heartsTexture = BeatCraft.id("textures/gui/hearts.png");

    private static final Vector2f emptyHeartUV = new Vector2f(18f/36f, 0);

    private static Vector2f getHeartUV(boolean hardcore, boolean halfHeart, boolean poisoned, boolean blinking) {
        float x = hardcore ? 9f : 0f;
        x += blinking ? 18f : 0f;

        float y = halfHeart ? 27f : 9f;
        y += poisoned ? 9f : 0f;

        return new Vector2f(x/36f, y/45f);
    }

    private static void drawHeart(BufferBuilder buffer, Vector3f pos, int heartId, int heartCount, float hpPercent, boolean poisoned) {
        float x = ((float) heartId) * HEART_SIZE.x - (((float) heartCount) * HEART_SIZE.x * 0.5f);

        var v0 = pos.add(-x, HEART_SIZE.y/2f, 0, MemoryPool.newVector3f());
        var v1 = pos.add(-x, -HEART_SIZE.y/2f, 0, MemoryPool.newVector3f());
        var v2 = pos.add(-x-HEART_SIZE.x, -HEART_SIZE.y/2f, 0, MemoryPool.newVector3f());
        var v3 = pos.add(-x-HEART_SIZE.x, HEART_SIZE.y/2f, 0, MemoryPool.newVector3f());

        float filled = hpPercent * heartCount * 2f;

        filled -= heartId * 2f;

        Vector2f uv;

        if (filled <= 0) {
            uv = emptyHeartUV;
        } else {
            uv = getHeartUV(heartCount <= 4, filled <= 1, poisoned, false);
        }

        buffer.vertex(v0).texture(uv.x, uv.y).color(heartId, 0, 0, 0);
        buffer.vertex(v1).texture(uv.x, uv.y + 9f/45f).color(heartId, 0, 0, 0);
        buffer.vertex(v2).texture(uv.x + 9f/36f, uv.y + 9f/45f).color(heartId, 0, 0, 0);
        buffer.vertex(v3).texture(uv.x + 9f/36f, uv.y).color(heartId, 0, 0, 0);

    }


}
