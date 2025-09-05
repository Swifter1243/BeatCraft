package com.beatcraft.mixin;

import com.beatcraft.common.data.types.CycleStack;
import com.beatcraft.common.items.SaberItem;
import com.beatcraft.common.items.data.ItemStackWithSaberTrailStack;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.render.RenderPass;
import oshi.util.tuples.Pair;

import java.util.HashMap;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackWithSaberTrailStack {


    @Shadow
    @Final
    @Deprecated @Nullable
    private Item item;

    @Unique
    private HashMap<RenderPass, CycleStack<Pair<Vector3f, Vector3f>>> beatcraft$stacks = new HashMap<>();

    @Unique
    public void beatcraft$initStack() {
        this.beatcraft$stacks = new HashMap<>();
        beatcraft$stacks.put(RenderPass.LEFT, new CycleStack<>(CycleStack.getTrailSize(), true));
        beatcraft$stacks.put(RenderPass.RIGHT, new CycleStack<>(CycleStack.getTrailSize(), true));
        beatcraft$stacks.put(RenderPass.CENTER, new CycleStack<>(CycleStack.getTrailSize(), true));
    }

    @Unique
    @Override
    public CycleStack<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash(RenderPass currentPass) {
        if (!beatcraft$stacks.containsKey(currentPass)) {
            beatcraft$stacks.put(currentPass, new CycleStack<>(CycleStack.getTrailSize(), true));
        }
        var stash = beatcraft$stacks.get(currentPass);

        return stash == null ? new CycleStack<>(2, false) : stash;
    }


    @Inject(
        method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
        at = @At("TAIL")
    )
    public void init(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (this.item instanceof SaberItem) {
            this.beatcraft$initStack();
        }
    }

}
