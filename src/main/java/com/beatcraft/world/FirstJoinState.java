package com.beatcraft.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class FirstJoinState extends PersistentState {

    private boolean hasJoined = false;

    public boolean hasJoined() {
        return hasJoined;
    }

    public void markJoin() {
        hasJoined = true;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean("beatcraftFirstJoin", hasJoined);
        return nbt;
    }

    public static FirstJoinState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        FirstJoinState state = new FirstJoinState();
        state.hasJoined = nbt.getBoolean("beatcraftFirstJoin");
        return state;
    }

}
