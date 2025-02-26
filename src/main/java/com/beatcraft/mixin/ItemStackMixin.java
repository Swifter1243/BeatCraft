package com.beatcraft.mixin;


import com.beatcraft.data.types.Stash;
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


@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, FabricItemStack, ItemStackWithSaberTrailStash {

    @Shadow
    @Final
    @Deprecated @Nullable
    private Item item;
    @Unique
    public Stash<Pair<Vector3f, Vector3f>> stash = null;

    @Unique
    public void initStash(int size) {
        this.stash = new Stash<>(size, true);
    }

    @Unique
    public Stash<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash() {
        return stash == null ? new Stash<>(2, false) : stash;
    }


    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
    public void init(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        if (this.item instanceof SaberItem) {
            this.initStash(30);
        }
    }

}

