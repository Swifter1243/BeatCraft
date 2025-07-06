package com.beatcraft.items.data;

import com.beatcraft.data.types.CycleStack;
import net.minecraft.util.Pair;
import org.joml.Vector3f;
import org.vivecraft.client_vr.render.RenderPass;

public interface ItemStackWithSaberTrailStash {
    CycleStack<Pair<Vector3f, Vector3f>> beatcraft$getTrailStash(RenderPass currentPass);
}
