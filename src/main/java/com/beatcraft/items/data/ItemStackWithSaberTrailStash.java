package com.beatcraft.items.data;

import com.beatcraft.data.types.Stash;
import net.minecraft.util.Pair;
import org.joml.Vector3f;

public interface ItemStackWithSaberTrailStash {
    Stash<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash();
}
