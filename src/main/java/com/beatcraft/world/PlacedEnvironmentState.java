package com.beatcraft.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class PlacedEnvironmentState extends PersistentState {
    private String environment = "Default";

    public String getPlacedEnvironment() {
        return environment;
    }

    public void setPlacedEnvironment(String env) {
        environment = env;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("beatcraftPlacedEnvironment", environment);
        return nbt;
    }

    public static PlacedEnvironmentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        var state = new PlacedEnvironmentState();
        state.environment = nbt.getString("beatcraftPlacedEnvironment");
        return state;
    }

}
