package com.beatcraft.mixin;


import com.beatcraft.data.types.CycleStack;
import com.beatcraft.items.SaberItem;
import com.beatcraft.items.data.ItemStackWithSaberTrailStash;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
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

import java.util.HashMap;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, FabricItemStack, ItemStackWithSaberTrailStash {

    @Shadow
    @Final
    @Deprecated @Nullable
    private Item item;

    @Unique
    private HashMap<RenderPass, CycleStack<Pair<Vector3f, Vector3f>>> stashes = new HashMap<>();

    @Unique
    public void initStash() {
        this.stashes = new HashMap<>();
        stashes.put(RenderPass.LEFT, new CycleStack<>(CycleStack.getTrailSize(), true));
        stashes.put(RenderPass.RIGHT, new CycleStack<>(CycleStack.getTrailSize(), true));
        stashes.put(RenderPass.CENTER, new CycleStack<>(CycleStack.getTrailSize(), true));
    }

    @Unique
    public CycleStack<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash(RenderPass currentPass) {
        if (!stashes.containsKey(currentPass)) {
            stashes.put(currentPass, new CycleStack<>(CycleStack.getTrailSize(), true));
        }
        var stash = stashes.get(currentPass);

        return stash == null ? new CycleStack<>(2, false) : stash;
    }


    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
    public void init(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        if (this.item instanceof SaberItem) {
            this.initStash();
        }
    }

}

