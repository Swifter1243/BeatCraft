package com.beatcraft.render.effect;

import com.beatcraft.data.components.ModComponents;
import com.beatcraft.data.types.Stash;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.data.ItemStackWithSaberTrailStash;
import com.beatcraft.mixin_utils.BufferBuilderAccessible;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SaberTrailRenderer {

    private static final ArrayList<Function<BufferBuilder, Void>> render_calls = new ArrayList<>();

    // Called from ItemEntityRendererMixin
    public static void renderItemEntityTrail(ItemEntity entity, float tickDelta, BakedModel bakedModel) {
        boolean depth = bakedModel.hasDepth();

        ItemStack stack = entity.getStack();
        if (stack.isOf(ModItems.SABER_ITEM)) {

            MatrixStack matrix = new MatrixStack();

            float j = MathHelper.sin(((float)entity.getItemAge() + tickDelta) / 10.0F + entity.uniqueOffset) * 0.1F + 0.1F;
            float k = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
            matrix.translate(0.0F, j + 0.25F * k, 0.0F);
            float l = entity.getRotation(tickDelta);
            matrix.multiply(RotationAxis.POSITIVE_Y.rotation(l));

            float dx = bakedModel.getTransformation().ground.scale.x();
            float dy = bakedModel.getTransformation().ground.scale.y();
            float dz = bakedModel.getTransformation().ground.scale.z();
            if (!depth) {
                float fx = -0.0F * (float) (0) * 0.5F * dx;
                float fy = -0.0F * (float) (0) * 0.5F * dy;
                float fz = -0.09375F * (float) (0) * 0.5F * dz;
                matrix.translate(fx, fy, fz);
            }
            matrix.push();
            bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).apply(false, matrix);

            matrix.push();
            matrix.translate(0, (7/8f) * 0.6, 0);
            Vector3f blade_base = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
            matrix.pop();

            matrix.push();
            matrix.translate(0, (38/8f) * 0.6, 0);
            Vector3f blade_tip = matrix.peek().getPositionMatrix().getTranslation(new Vector3f());
            matrix.pop();

            Vec3d pos = entity.getLerpedPos(tickDelta);
            blade_base = blade_base.add((float) pos.x, (float) pos.y, (float) pos.z);
            blade_tip = blade_tip.add((float) pos.x, (float) pos.y, (float) pos.z);
            Stash<Pair<Vector3f, Vector3f>> stash = ((ItemStackWithSaberTrailStash) ((Object) stack)).beatcraft$getTrailStash();
            var col = stack.getOrDefault(ModComponents.SABER_COLOR_COMPONENT, 0);

            if (stash != null) {
                SaberTrailRenderer.queueRender(blade_base, blade_tip, stash, col);
            }
        }
    }

    public static void queueRender(Vector3f blade_base, Vector3f blade_tip, Stash<Pair<Vector3f, Vector3f>> stash, int col) {
        Function<BufferBuilder, Void> callable = (trail_buffer) -> {
            SaberTrailRenderer.render(blade_base, blade_tip, stash, col, trail_buffer);
            return null;
        };
        render_calls.add(callable);
    }

    public static void renderAll() {

        if (render_calls.isEmpty()) return;

        var tessellator = Tessellator.getInstance();
        var trail_buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Function<BufferBuilder, Void> runnable : render_calls) {
            runnable.apply(trail_buffer);
        }

        BuiltBuffer buffer = trail_buffer.endNullable();

        if (buffer == null) return;

        ShaderProgram oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        buffer.sortQuads(((BufferBuilderAccessible) trail_buffer).beatcraft$getAllocator(), VertexSorter.BY_DISTANCE);
        BufferRenderer.drawWithGlobalProgram(buffer);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);

        render_calls.clear();

    }

    public static void render(Vector3f blade_base, Vector3f blade_tip, Stash<Pair<Vector3f, Vector3f>> stash, int col, BufferBuilder trail_buffer) {

        Vector3f current_base = blade_base;
        Vector3f current_tip = blade_tip;
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        if (!stash.isEmpty()) {
            //
            int opacity = 0;
            float step = 0x7F / (float) stash.getSize();
            for (Pair<Vector3f, Vector3f> ab : stash) {
                Vector3f a = ab.getLeft();
                Vector3f b = ab.getRight();

                int op = (0x7F - ((int)(step * opacity))) << 24;
                opacity++;

                trail_buffer.vertex((float) (current_base.x - cam.x), (float) (current_base.y - cam.y), (float) (current_base.z - cam.z)).color(col + op);
                trail_buffer.vertex((float) (a.x - cam.x), (float) (a.y - cam.y), (float) (a.z - cam.z)).color(col + op);
                trail_buffer.vertex((float) (b.x - cam.x), (float) (b.y - cam.y), (float) (b.z - cam.z)).color(col + op);
                trail_buffer.vertex((float) (current_tip.x - cam.x), (float) (current_tip.y - cam.y), (float) (current_tip.z - cam.z)).color(col + op);

                current_base = a;
                current_tip = b;

            }

        }

        stash.push(new Pair<>(blade_base, blade_tip));

    }


}
