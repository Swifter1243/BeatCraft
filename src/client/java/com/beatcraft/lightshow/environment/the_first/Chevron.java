package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.BeatCraft;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.logic.Hitbox;
import com.beatcraft.render.effect.Bloomfog;
import com.beatcraft.render.lights.GlowingCuboid;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Chevron extends LightObject {

    private static final GlowingCuboid arm1 = new GlowingCuboid(
        new Hitbox(
            new Vector3f(-0.06f, -1.5f, -0.06f),
            new Vector3f(0.06f, 0.03f, 0.06f)
        ),
        new Vector3f(),
        new Quaternionf().rotationZ(55 * MathHelper.RADIANS_PER_DEGREE)
    );

    private static final GlowingCuboid arm2 = new GlowingCuboid(
        new Hitbox(
            new Vector3f(-0.06f, -1.5f, -0.06f),
            new Vector3f(0.06f, 0.03f, 0.06f)
        ),
        new Vector3f(),
        new Quaternionf().rotationZ(-55 * MathHelper.RADIANS_PER_DEGREE)
    );

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {

        arm1.setOffset(position);
        arm2.setOffset(position);
        arm1.setRotation(orientation);
        arm2.setRotation(orientation);
        arm1.setLightState(lightState);
        arm2.setLightState(lightState);

        arm1.render(matrices, camera, bloomfog);
        arm2.render(matrices, camera, bloomfog);
    }

    @Override
    public void setValue(float value) {
        arm1.setValue(value);
        arm2.setValue(value);
    }

    @Override
    public void setColor(int color) {
        arm1.setColor(color);
        arm2.setColor(color);
    }
}
