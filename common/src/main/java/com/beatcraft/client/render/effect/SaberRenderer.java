package com.beatcraft.client.render.effect;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.logic.PhysicsTransform;
import com.beatcraft.client.render.BeatcraftRenderer;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.common.data.ControllerProfile;
import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.data.types.CycleStack;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.common.items.data.ItemStackWithSaberTrailStack;
import com.beatcraft.common.replay.PlayFrame;
import com.beatcraft.mixin_utils.BufferBuilderAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public class SaberRenderer {

    public static final HashMap<UUID, PlayFrame> otherPlayerSabers = new HashMap<>();

    private static final ArrayList<Function<BufferBuilder, Void>> render_calls = new ArrayList<>();

    private static void renderOtherPlayerSabers() {
        otherPlayerSabers.forEach((uuid, playFrame) -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world == null) return;
            Player player = world.getPlayerByUUID(uuid);
            if (player == null) return;

            // boolean tracked = GameLogicHandler.isTracking(uuid);

            ItemStack stack = player.getMainHandItem();
            ItemStack stack2 = player.getOffhandItem();

            if (player.getMainArm() == HumanoidArm.LEFT) {
                var s3 = stack;
                stack = stack2;
                stack2 = s3;
            }

            if (stack.is(ModItems.SABER_ITEM)) {
                renderReplaySaber(stack, playFrame.rightSaberPosition(), playFrame.rightSaberRotation());
                // if (tracked) {
                //     GameLogicHandler.updateRightSaber(playFrame.rightSaberPosition(), playFrame.rightSaberRotation());
                // }
            }
            if (stack2.is(ModItems.SABER_ITEM)) {
                renderReplaySaber(stack2, playFrame.leftSaberPosition(), playFrame.leftSaberRotation());
                // if (tracked) {
                //     GameLogicHandler.updateLeftSaber(playFrame.leftSaberPosition(), playFrame.leftSaberRotation());
                // }
            }
        });
    }

    public static void renderReplaySaber(ItemStack item, Vector3f position, Quaternionf orientation) {
        renderReplayTrail(item, position, orientation);

        PoseStack matrices = new PoseStack();
        var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.translate(position.x, position.y, position.z);
        matrices.mulPose(orientation);
        matrices.scale(0.3333f, 0.3333f, 0.3333f);

        BeatcraftRenderer.recordSaberRenderCall(() -> {
            Minecraft.getInstance().getItemRenderer().renderStatic(
                item, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 255, 0,
                matrices, HUDRenderer.buffers, Minecraft.getInstance().level, 0
            );
        });
    }

    public static void renderSaber(ItemStack item, PoseStack matrices, MultiBufferSource vertexConsumerProvider, InteractionHand hand, AbstractClientPlayer player, float tickDelta) {
        matrices.pushPose();



        Vector3f worldPos = matrices.last().pose()
            .getTranslation(new Vector3f());
            // .add(BeatcraftClient.playerGlobalPosition.toVector3f())
            // .add(BeatcraftClient.playerSaberPosition.toVector3f())
            // .sub(BeatcraftClient.playerCameraPosition.toVector3f());

        Quaternionf worldRotation = matrices.last().pose().getNormalizedRotation(new Quaternionf());

        // PoseStack matrixStack = new PoseStack();
        // matrixStack.translate(worldPos.x, worldPos.y, worldPos.z);
        // matrixStack.scale(0.3333f, 0.3333f, 0.3333f);
        // matrixStack.mulPose(worldRotation);
        // matrixStack.pushPose();
        renderTrail(true, matrices, hand.equals(InteractionHand.MAIN_HAND), player, tickDelta, item);
        // matrixStack.popPose();

        Minecraft.getInstance().getItemRenderer().renderStatic(
            item, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 255, 0,
            matrices, vertexConsumerProvider, null, 0
        );

        var uuid = player.getUUID();

        if (!BeatcraftClient.controllerTransforms.containsKey(uuid)) {
            BeatcraftClient.controllerTransforms.put(uuid, new Triplet<>(
                new PhysicsTransform(-0.4f, 0, 0),
                new PhysicsTransform(0, 0, 0),
                new PhysicsTransform(0.4f, 0, 0)
            ));
        }

        var isRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        var right = isRightHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        var sabers = BeatcraftClient.controllerTransforms.get(uuid);

        var camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var mat = new Matrix4f().translate(camPos).mul(matrices.last().pose());
        if (hand == right) {
            sabers.getC().update(mat);
        } else {
            sabers.getA().update(mat);
        }


        matrices.popPose();

        // ClientPlayNetworking.send(new SaberSyncC2SPayload(GameLogicHandler.leftSaberPos, GameLogicHandler.leftSaberRotation, GameLogicHandler.rightSaberPos, GameLogicHandler.rightSaberRotation, GameLogicHandler.headPos, GameLogicHandler.headRot));

    }

    public static void renderReplayTrail(ItemStack stack, Vector3f basePos, Quaternionf rotation) {
        Vector3f hiltPos = new Vector3f(0, (7/8f)*0.2f, 0).rotate(rotation).add(basePos);
        Vector3f tipPos = new Vector3f(0, (41/8f)*0.2f, 0).rotate(rotation).add(basePos);
        CycleStack<Pair<Vector3f, Vector3f>> cycleStack = ((ItemStackWithSaberTrailStack) ((Object) stack)).beatcraft$getTrailStash(ClientDataHolderVR.getInstance().currentPass);
        int color;

        int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR.get(), -1);

        if (sync == -1 || !BeatmapManager.hasNearbyActiveBeatmap(basePos)) {
            color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT.get(), 0) + 0xFF000000;
        } else if (sync == 0) {
            color = BeatmapManager.nearestActiveBeatmap(basePos).difficulty.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
        } else {
            color = BeatmapManager.nearestActiveBeatmap(basePos).difficulty.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
        }
        queueRender(hiltPos, tipPos, cycleStack, color);
    }

    public static void renderTrail(boolean doCollisionCheck, PoseStack matrix, boolean mainHand, AbstractClientPlayer player, float tickDelta, ItemStack stack) {
        if (stack.is(ModItems.SABER_ITEM)) {

            // Step 1: initial transform to get saber into the default position.
            matrix.scale(0.3333f, 0.3333f, 0.3333f);
            matrix.translate(0, -0.25, 0.35);
            matrix.mulPose((new Quaternionf()).rotationXYZ(-45 * Mth.DEG_TO_RAD, 0, 0));


            // Step 2: modify rotation and translation based on user settings
            Vector3f translation;
            Quaternionf rotation;
            ControllerProfile profile = BeatcraftClient.playerConfig.controller.activeProfile();
            boolean rightHand;
            if ((player.getMainArm() == HumanoidArm.RIGHT && mainHand) || (player.getMainArm() == HumanoidArm.LEFT && !mainHand)) {
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
            matrix.mulPose(rotation);

            // Step 2.5: use result to do collisions


            // Step 3: use result to also render trail
            if (doCollisionCheck) {
                matrix.pushPose();
                matrix.translate(0, (7 / 8f) * 0.6, 0);
                Vector3f blade_base = matrix.last().pose().getTranslation(new Vector3f());
                matrix.popPose();

                matrix.pushPose();
                matrix.translate(0, (41 / 8f) * 0.6, 0);
                Vector3f blade_tip = matrix.last().pose().getTranslation(new Vector3f());
                matrix.popPose();

                var playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

                // if (GameLogicHandler.isTrackingClient()) {
                //     if (rightHand) {
                //         GameLogicHandler.updateRightSaber(saberPos, saberRot);
                //     } else {
                //         GameLogicHandler.updateLeftSaber(saberPos, saberRot);
                //     }
                // }
                blade_tip.add(playerPos);
                blade_base.add(playerPos);
                CycleStack<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStack) ((Object) stack)).beatcraft$getTrailStash(ClientDataHolderVR.getInstance().currentPass);
                int color;

                int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR.get(), -1);

                if (sync == -1 || !BeatmapManager.hasNearbyActiveBeatmap(blade_base)) {
                    color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT.get(), 0) + 0xFF000000;
                } else if (sync == 0) {
                    color = BeatmapManager.nearestActiveBeatmap(blade_base).difficulty.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
                } else {
                    color = BeatmapManager.nearestActiveBeatmap(blade_base).difficulty.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
                }

                if (stash != null) {
                    SaberRenderer.queueRender(blade_base, blade_tip, stash, color);
                }
            }


        }
    }

    // Called from ItemEntityRendererMixin
    public static void renderItemEntityTrail(ItemEntity entity, float tickDelta, BakedModel bakedModel) {

        ItemStack stack = entity.getItem();
        if (stack.is(ModItems.SABER_ITEM)) {

            PoseStack matrix = new PoseStack();

            float j = Mth.sin(((float)entity.getAge() + tickDelta) / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
            float k = bakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
            matrix.translate(0.0F, j + 0.25F * k, 0.0F);
            float l = entity.getSpin(tickDelta);
            matrix.mulPose(new Quaternionf().rotateAxis(l, 0, 1, 0));

            matrix.pushPose();
            bakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).apply(false, matrix);

            matrix.pushPose();
            matrix.translate(0, (14/8f) * 0.6, 0);
            Vector3f blade_base = matrix.last().pose().getTranslation(new Vector3f());
            matrix.popPose();

            matrix.pushPose();
            matrix.translate(0, (41/8f) * 0.6, 0);
            Vector3f blade_tip = matrix.last().pose().getTranslation(new Vector3f());
            matrix.popPose();

            var pos = entity.getPosition(tickDelta).toVector3f();
            blade_base.add(pos);
            blade_tip.add(pos);
            CycleStack<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStack) ((Object) stack)).beatcraft$getTrailStash(ClientDataHolderVR.getInstance().currentPass);
            int color;

            int sync = stack.getOrDefault(ModComponents.AUTO_SYNC_COLOR.get(), -1);

            if (sync == -1 || !BeatmapManager.hasNearbyActiveBeatmap(blade_base)) {
                color = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT.get(), 0) + 0xFF000000;
            } else if (sync == 0) {
                color = BeatmapManager.nearestActiveBeatmap(blade_base).difficulty.getSetDifficulty().getColorScheme().getNoteLeftColor().toARGB();
            } else {
                color = BeatmapManager.nearestActiveBeatmap(blade_base).difficulty.getSetDifficulty().getColorScheme().getNoteRightColor().toARGB();
            }

            if (stash != null) {
                SaberRenderer.queueRender(blade_base, blade_tip, stash, color);
            }
        }
    }

    public static void queueRender(Vector3f blade_base, Vector3f blade_tip, CycleStack<Pair<Vector3f, Vector3f>> cycleStack, int col) {
        Function<BufferBuilder, Void> callable = (trail_buffer) -> {
            SaberRenderer.render(blade_base, blade_tip, cycleStack, col, trail_buffer);
            return null;
        };
        render_calls.add(callable);
    }

    public static void renderAll() {

        renderOtherPlayerSabers();

        if (render_calls.isEmpty()) return;

        var tesselator = Tesselator.getInstance();
        var trail_buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);


        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        //RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (Function<BufferBuilder, Void> runnable : render_calls) {
            runnable.apply(trail_buffer);
        }

        var buffer = trail_buffer.build();
        render_calls.clear();

        if (buffer != null) {
            buffer.sortQuads(((BufferBuilderAccessor) trail_buffer).beatcraft$getAllocator(), VertexSorting.ORTHOGRAPHIC_Z);
            BufferUploader.drawWithShader(buffer);
        }
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();


    }

    public static void render(Vector3f blade_base, Vector3f blade_tip, CycleStack<Pair<Vector3f, Vector3f>> cycleStack, int col, BufferBuilder trail_buffer) {
        if (cycleStack.getSize() <= 3) return;
        Vector3f current_base = blade_base;
        Vector3f current_tip = blade_tip;
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        if (!cycleStack.isEmpty()) {
            int opacity = 0;
            float step = 0x7F / (float) cycleStack.getSize();
            for (Pair<Vector3f, Vector3f> ab : cycleStack) {
                Vector3f a = ab.getA();
                Vector3f b = ab.getB();

                int op = (0x7F - ((int)(step * opacity))) << 24;
                int op2 = (0x7F - ((int)(step * (opacity+1)))) << 24;
                opacity++;

                if (op == 0 || op2 == 0) continue;

                trail_buffer.addVertex((float) (current_base.x - cam.x), (float) (current_base.y - cam.y), (float) (current_base.z - cam.z)).setColor(col + op);
                trail_buffer.addVertex((float) (a.x - cam.x), (float) (a.y - cam.y), (float) (a.z - cam.z)).setColor(col + op2);
                trail_buffer.addVertex((float) (b.x - cam.x), (float) (b.y - cam.y), (float) (b.z - cam.z)).setColor(col + op2);
                trail_buffer.addVertex((float) (current_tip.x - cam.x), (float) (current_tip.y - cam.y), (float) (current_tip.z - cam.z)).setColor(col + op);

                current_base = a;
                current_tip = b;

            }

        }

        cycleStack.push(new Pair<>(blade_base, blade_tip));

    }


}
