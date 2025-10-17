package com.beatcraft.common.items.data;

import com.beatcraft.common.data.types.CycleStack;
import org.joml.Vector3f;
import org.vivecraft.api.client.data.RenderPass;
import oshi.util.tuples.Pair;

// RenderPass from vivecraft
public interface ItemStackWithSaberTrailStack {
    CycleStack<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash(RenderPass currentPass);
}
