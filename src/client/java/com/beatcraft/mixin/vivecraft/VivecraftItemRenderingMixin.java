package com.beatcraft.mixin.vivecraft;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.ControllerProfile;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.data.types.Stash;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.data.ItemStackWithSaberTrailStash;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.render.effect.SaberTrailRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.VivecraftItemRendering;

@Mixin(VivecraftItemRendering.class)
public abstract class VivecraftItemRenderingMixin {

    @Inject(
        method = "applyFirstPersonItemTransforms",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Quaternionf;mul(Lorg/joml/Quaternionfc;)Lorg/joml/Quaternionf;",
            ordinal = 0
        ),
        cancellable = true
    )
    private static void saberTrailRenderInject(MatrixStack matrix, VivecraftItemRendering.VivecraftItemTransformType renderType, boolean mainHand, AbstractClientPlayerEntity player, float equippedProgress, float tickDelta, ItemStack stack, Hand hand, CallbackInfo ci) {
        renderTrailCommon(true, matrix, mainHand, player, tickDelta, stack, ci);
    }

    @Unique
    private static void renderTrailCommon(boolean doCollisionCheck, MatrixStack matrix, boolean mainHand, AbstractClientPlayerEntity player, float tickDelta, ItemStack stack, CallbackInfo ci) {
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

                if (rightHand) {
                    GameLogicHandler.updateRightSaber(saberPos, saberRot);
                } else {
                    GameLogicHandler.updateLeftSaber(saberPos, saberRot);
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
                    SaberTrailRenderer.queueRender(blade_base, blade_tip, stash, color);
                }
            }

            ci.cancel();
        }
    }
}
