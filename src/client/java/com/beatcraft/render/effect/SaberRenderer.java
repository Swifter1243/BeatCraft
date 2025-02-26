package com.beatcraft.render.effect;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.ControllerProfile;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.data.types.Stash;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.data.ItemStackWithSaberTrailStash;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.beatcraft.networking.c2s.BeatSyncC2SPayload;
import com.beatcraft.networking.c2s.SaberSyncC2SPayload;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.replay.PlayFrame;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public class SaberRenderer {

    public static final HashMap<UUID, PlayFrame> otherPlayerSabers = new HashMap<>();

    private static final ArrayList<Function<BufferBuilder, Void>> render_calls = new ArrayList<>();

    private static void renderOtherPlayerSabers() {
        otherPlayerSabers.forEach((uuid, playFrame) -> {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world == null) return;
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player == null) return;

            boolean tracked = GameLogicHandler.isTracking(uuid);

            ItemStack stack = player.getMainHandStack();
            ItemStack stack2 = player.getOffHandStack();

            if (player.getMainArm() == Arm.LEFT) {
                var s3 = stack;
                stack = stack2;
                stack2 = s3;
            }

            if (stack.isOf(ModItems.SABER_ITEM)) {
                renderReplaySaber(stack, playFrame.rightSaberPosition(), playFrame.rightSaberRotation());
                if (tracked) {
                    GameLogicHandler.updateRightSaber(playFrame.rightSaberPosition(), playFrame.rightSaberRotation());
                }
            }
            if (stack2.isOf(ModItems.SABER_ITEM)) {
                renderReplaySaber(stack2, playFrame.leftSaberPosition(), playFrame.leftSaberRotation());
                if (tracked) {
                    GameLogicHandler.updateLeftSaber(playFrame.leftSaberPosition(), playFrame.leftSaberRotation());
                }
            }
        });
    }

    public static void renderReplaySaber(ItemStack item, Vector3f position, Quaternionf orientation) {
        renderReplayTrail(item, position, orientation);

        MatrixStack matrices = new MatrixStack();
        var cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.translate(position.x, position.y, position.z);
        matrices.multiply(orientation);
        matrices.scale(0.3333f, 0.3333f, 0.3333f);

        BeatcraftRenderer.recordRenderCall(() -> {
            MinecraftClient.getInstance().getItemRenderer().renderItem(
                item, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, 255, 0,
                matrices, HUDRenderer.vertexConsumerProvider, MinecraftClient.getInstance().world, 0
            );
        });
    }

    public static void renderSaber(ItemStack item, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumerProvider, Hand hand, AbstractClientPlayerEntity player, float tickDelta) {
        matrices.push();
        renderTrail(true, matrices, hand == Hand.MAIN_HAND, player, tickDelta, item);

        Vector3f worldPos = matrices.peek().getPositionMatrix()
            .getTranslation(new Vector3f())
            .add(BeatCraftClient.playerGlobalPosition.toVector3f())
            .add(BeatCraftClient.playerSaberPosition.toVector3f())
            .sub(BeatCraftClient.playerCameraPosition.toVector3f());
            //.sub(player.getPos().toVector3f());
        Quaternionf worldRotation = (hand == Hand.MAIN_HAND ? GameLogicHandler.rightSaberRotation : GameLogicHandler.leftSaberRotation);
            //.add(BeatCraftClient.playerGlobalRotation, new Quaternionf())
            //.add(BeatCraftClient.playerSaberRotation);


        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(worldPos.x, worldPos.y, worldPos.z);
        matrixStack.multiply(worldRotation);
        matrixStack.scale(0.3333f, 0.3333f, 0.3333f);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
            item, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, 255, 0,
            matrixStack, vertexConsumerProvider, null, 0
        );


        matrices.pop();

        ClientPlayNetworking.send(new SaberSyncC2SPayload(GameLogicHandler.leftSaberPos, GameLogicHandler.leftSaberRotation, GameLogicHandler.rightSaberPos, GameLogicHandler.rightSaberRotation));
        if (GameLogicHandler.isTrackingClient() && BeatmapPlayer.isPlaying()) {
            ClientPlayNetworking.send(new BeatSyncC2SPayload(BeatmapPlayer.getCurrentBeat()));
        }
    }

    public static void renderReplayTrail(ItemStack stack, Vector3f basePos, Quaternionf rotation) {
        Vector3f hiltPos = new Vector3f(0, (7/8f)*0.2f, 0).rotate(rotation).add(basePos);
        Vector3f tipPos = new Vector3f(0, (41/8f)*0.2f, 0).rotate(rotation).add(basePos);
        Stash<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStash) ((Object) stack)).beatcraft$getTrailStash();
        int color;

        int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR, -1);

        if (sync == -1 || BeatmapPlayer.currentBeatmap == null) {
            color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0) + 0xFF000000;
        } else if (sync == 0) {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
        } else {
            color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        }
        queueRender(hiltPos, tipPos, stash, color);
    }

    public static void renderTrail(boolean doCollisionCheck, MatrixStack matrix, boolean mainHand, AbstractClientPlayerEntity player, float tickDelta, ItemStack stack) {
        if (stack.isOf(ModItems.SABER_ITEM)) {

            // Step 1: initial transform to get saber into the default position.
            matrix.scale(0.3333f, 0.3333f, 0.3333f);
            matrix.translate(0, -0.25, 0.25);
            matrix.multiply((new Quaternionf()).rotationXYZ(-38 * MathHelper.RADIANS_PER_DEGREE, 0, 0));

            // Step 2: modify rotation and translation based on user settings
            Vector3f translation;
            Quaternionf rotation;
            ControllerProfile profile = BeatCraftClient.playerConfig.getActiveControllerProfile();
            boolean rightHand;
            if ((player.getMainArm() == Arm.RIGHT && mainHand) || (player.getMainArm() == Arm.LEFT && !mainHand)) {
                // right hand
                translation = profile.getRightTranslation();
                rotation = profile.getRightRotation();
                rightHand = true;
            } else {
                // left hand
                translation = profile.getLeftTranslation();
                rotation = profile.getLeftRotation();
                rightHand = false;
            }

            matrix.translate(translation.x, translation.y, translation.z);
            matrix.multiply(rotation);

            // Step 2.5: use result to do collisions


            // Step 3: use result to also render trail
            if (doCollisionCheck) {
                matrix.push();
                matrix.translate(0, (7 / 8f) * 0.6, 0);
                Vector3f blade_base = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
                matrix.pop();

                matrix.push();
                matrix.translate(0, (41 / 8f) * 0.6, 0);
                Vector3f blade_tip = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
                matrix.pop();

                matrix.push();
                matrix.translate(0, 0.25 * 0.6, 0);
                MatrixStack.Entry entry = matrix.peek();
                matrix.pop();

                Vec3d playerPos = player.getLerpedPos(tickDelta);
                Vector3f saberPos = entry.getPositionMatrix().getTranslation(new Vector3f());
                saberPos.add((float) playerPos.x, (float) playerPos.y, (float) playerPos.z);
                Quaternionf saberRot = entry.getPositionMatrix().getUnnormalizedRotation(new Quaternionf());

                if (GameLogicHandler.isTrackingClient()) {
                    if (rightHand) {
                        GameLogicHandler.updateRightSaber(saberPos, saberRot);
                    } else {
                        GameLogicHandler.updateLeftSaber(saberPos, saberRot);
                    }
                }
                blade_tip = blade_tip.add((float) playerPos.x, (float) playerPos.y, (float) playerPos.z);
                blade_base = blade_base.add((float) playerPos.x, (float) playerPos.y, (float) playerPos.z);
                Stash<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStash) ((Object) stack)).beatcraft$getTrailStash();
                int color;

                int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR, -1);

                if (sync == -1 || BeatmapPlayer.currentBeatmap == null) {
                    color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0) + 0xFF000000;
                } else if (sync == 0) {
                    color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
                } else {
                    color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
                }

                if (stash != null) {
                    SaberRenderer.queueRender(blade_base, blade_tip, stash, color);
                }
            }
        }
    }

    // Called from ItemEntityRendererMixin
    public static void renderItemEntityTrail(ItemEntity entity, float tickDelta, BakedModel bakedModel) {

        ItemStack stack = entity.getStack();
        if (stack.isOf(ModItems.SABER_ITEM)) {

            MatrixStack matrix = new MatrixStack();

            float j = MathHelper.sin(((float)entity.getItemAge() + tickDelta) / 10.0F + entity.uniqueOffset) * 0.1F + 0.1F;
            float k = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
            matrix.translate(0.0F, j + 0.25F * k, 0.0F);
            float l = entity.getRotation(tickDelta);
            matrix.multiply(RotationAxis.POSITIVE_Y.rotation(l));

            matrix.push();
            bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).apply(false, matrix);

            matrix.push();
            matrix.translate(0, (7/8f) * 0.6, 0);
            Vector3f blade_base = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
            matrix.pop();

            matrix.push();
            matrix.translate(0, (41/8f) * 0.6, 0);
            Vector3f blade_tip = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
            matrix.pop();

            Vec3d pos = entity.getLerpedPos(tickDelta);
            blade_base = blade_base.add((float) pos.x, (float) pos.y, (float) pos.z);
            blade_tip = blade_tip.add((float) pos.x, (float) pos.y, (float) pos.z);
            Stash<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStash) ((Object) stack)).beatcraft$getTrailStash();
            int color;

            int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR, -1);

            if (sync == -1 || BeatmapPlayer.currentBeatmap == null) {
                color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0) + 0xFF000000;
            } else if (sync == 0) {
                color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
            } else {
                color = BeatmapPlayer.currentBeatmap.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
            }

            if (stash != null) {
                SaberRenderer.queueRender(blade_base, blade_tip, stash, color);
            }
        }
    }

    public static void queueRender(Vector3f blade_base, Vector3f blade_tip, Stash<Pair<Vector3f, Vector3f>> stash, int col) {
        Function<BufferBuilder, Void> callable = (trail_buffer) -> {
            SaberRenderer.render(blade_base, blade_tip, stash, col, trail_buffer);
            return null;
        };
        render_calls.add(callable);
    }

    public static void renderAll() {

        renderOtherPlayerSabers();

        if (render_calls.isEmpty()) return;

        var tessellator = Tessellator.getInstance();
        var trail_buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Function<BufferBuilder, Void> runnable : render_calls) {
            runnable.apply(trail_buffer);
        }

        BuiltBuffer buffer = trail_buffer.endNullable();
        render_calls.clear();

        if (buffer == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        buffer.sortQuads(((BufferBuilderAccessor) trail_buffer).beatcraft$getAllocator(), VertexSorter.BY_Z);
        BufferRenderer.drawWithGlobalProgram(buffer);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);


    }

    public static void render(Vector3f blade_base, Vector3f blade_tip, Stash<Pair<Vector3f, Vector3f>> stash, int col, BufferBuilder trail_buffer) {

        Vector3f current_base = blade_base;
        Vector3f current_tip = blade_tip;
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        if (!stash.isEmpty()) {
            int opacity = 0;
            float step = 0x7F / (float) stash.getSize();
            for (Pair<Vector3f, Vector3f> ab : stash) {
                Vector3f a = ab.getLeft();
                Vector3f b = ab.getRight();

                int op = (0x7F - ((int)(step * opacity))) << 24;
                int op2 = (0x7F - ((int)(step * (opacity+1)))) << 24;
                opacity++;

                if (op == 0 || op2 == 0) continue;

                trail_buffer.vertex((float) (current_base.x - cam.x), (float) (current_base.y - cam.y), (float) (current_base.z - cam.z)).color(col + op);
                trail_buffer.vertex((float) (a.x - cam.x), (float) (a.y - cam.y), (float) (a.z - cam.z)).color(col + op2);
                trail_buffer.vertex((float) (b.x - cam.x), (float) (b.y - cam.y), (float) (b.z - cam.z)).color(col + op2);
                trail_buffer.vertex((float) (current_tip.x - cam.x), (float) (current_tip.y - cam.y), (float) (current_tip.z - cam.z)).color(col + op);

                current_base = a;
                current_tip = b;

            }

        }

        stash.push(new Pair<>(blade_base, blade_tip));

    }


}
