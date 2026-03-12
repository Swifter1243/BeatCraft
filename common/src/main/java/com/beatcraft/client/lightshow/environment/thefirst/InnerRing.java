package com.beatcraft.client.lightshow.environment.thefirst;

import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.lightshow.lights.LightObject;
import com.beatcraft.client.lightshow.lights.LightState;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.instancing.lightshow.light_object.LightMeshInstance;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class InnerRing extends LightObject {


    private LightMeshInstance mesh;

    private static final ArrayList<InnerRing> rings = new ArrayList<>();

    public static void clearInstances() {
        rings.clear();
    }

    public static void reload() {
        for (var ring : rings) {
            ring.mesh = new LightMeshInstance(MeshLoader.TheFirst.INNER_RING);
        }
    }

    public InnerRing(BeatmapController map, Vector3f pos, Quaternionf ori) {
        super(map);
        rings.add(this);
        position = pos;
        orientation = ori;

        mesh = new LightMeshInstance(MeshLoader.TheFirst.INNER_RING);

    }


    @Override
    public LightObject cloneOffset(Vector3f offset) {
        return new InnerRing(
            mapController,
            position.add(offset, new Vector3f()),
            new Quaternionf(orientation)
        );
    }

    @Override
    public void render(PoseStack matrices, Camera camera, float alpha, Bloomfog bloomfog) {
        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
        var mat = createTransformMatrix(matrices.last().pose(), false, orientation, rotation, transformState, position, worldRotation, offset, cameraPos);
        mesh.transform.set(mat);

        mesh.draw(mapController.worldPosition);
    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public void setLightState(LightState state) {

    }
}
