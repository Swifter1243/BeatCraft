package com.beatcraft.mixin;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.resource.ResourceFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mixin(GameRenderer.class)
@Debug(export = true)
public class GameRendererMixin {

    @Shadow @Final private Map<String, ShaderProgram> programs;

    @Shadow @Nullable private static ShaderProgram renderTypeSolidProgram;

    @Shadow @Nullable private static ShaderProgram renderTypeCutoutProgram;

    @Shadow @Nullable private static ShaderProgram renderTypeTranslucentProgram;

    @Inject(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Matrix4f;<init>()V"
        )
    )
    public void overridePlayerCameraPos(RenderTickCounter tickCounter, CallbackInfo ci, @Local Camera camera) {
        camera.pos = camera.pos.add(BeatCraftClient.playerCameraPosition).add(BeatCraftClient.playerGlobalPosition);
        camera.blockPos.set(camera.pos.x, camera.pos.y, camera.pos.z);
        camera.getRotation().mul(BeatCraftClient.playerCameraRotation).mul(BeatCraftClient.playerGlobalRotation).normalize();
    }

    @WrapOperation(
        method = "loadPrograms",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 7 // 7 = solid, 8 = cutout mipped, 9 = cutout, 10 = translucent
        )
    )
    private<E> boolean loadBloomfogShaders(List<E> instance, E e, Operation<Boolean> original, @Local(argsOnly = true) ResourceFactory factory) {
        try {
            Bloomfog.bloomfog_solid_shader = new ShaderProgram(factory, "rendertype_solid_bloomfog", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

    @Inject(
        method = "loadPrograms",
        at = @At("TAIL")
    )
    private void addPrograms(ResourceFactory factory, CallbackInfo ci) {
        this.programs.put("rendertype_solid", Bloomfog.bloomfog_solid_shader);
        renderTypeSolidProgram = Bloomfog.bloomfog_solid_shader;
    }

    @Inject(
        method = "getRenderTypeSolidProgram",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void overrideRendertypeSolid(CallbackInfoReturnable<ShaderProgram> cir) {
        cir.setReturnValue(Bloomfog.bloomfog_solid_shader);
        cir.cancel();
    }

}
