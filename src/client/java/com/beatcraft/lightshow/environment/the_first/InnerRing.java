package com.beatcraft.lightshow.environment.the_first;

import com.beatcraft.BeatCraft;
import com.beatcraft.lightshow.lights.LightObject;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.effect.Bloomfog;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class InnerRing extends LightObject {


    private InnerRing() {
    }

    private static InnerRing INSTANCE;
    public static InnerRing getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InnerRing();
        }
        return INSTANCE;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, Bloomfog bloomfog) {
        BeatcraftRenderer.recordRenderCall(() -> _render(new Vector3f(position), new Vector3f(offset), new Quaternionf(orientation), new Quaternionf(rotation)));
    }

    private Vector3f processVertex(Vector3f base, Vector3f pos, Vector3f off, Quaternionf ori, Quaternionf rot, Vector3f camera) {
        return new Vector3f(base)
            .rotate(ori).rotate(rot)
            .add(pos).add(off).sub(camera);
    }

    private void _render(Vector3f position, Vector3f offset, Quaternionf orientation, Quaternionf rotation) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vector3f cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        // manual mesh building since loading json doesn't work >:(

        float ringRadius = 10;
        float ringWidth = 0.3f;
        float ringDepth = 0.1f;
        float ringGap = 4;

        buffer.vertex(processVertex(new Vector3f(-1, -1, 0), position, offset, orientation, rotation, cameraPos)).color(0xFF000000);
        buffer.vertex(processVertex(new Vector3f(-1,  1, 0), position, offset, orientation, rotation, cameraPos)).color(0xFF000000);
        buffer.vertex(processVertex(new Vector3f( 1,  1, 0), position, offset, orientation, rotation, cameraPos)).color(0xFF000000);
        buffer.vertex(processVertex(new Vector3f( 1, -1, 0), position, offset, orientation, rotation, cameraPos)).color(0xFF000000);

        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
    }

    @Override
    public void setBrightness(float value) {

    }

    @Override
    public void setColor(int color) {

    }
}
