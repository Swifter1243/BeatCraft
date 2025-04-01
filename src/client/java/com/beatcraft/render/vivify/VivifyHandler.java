package com.beatcraft.render.vivify;

import com.beatcraft.BeatCraft;
import com.beatcraft.render.SpawnQuaternionPool;
import com.beatcraft.render.particle.BeatcraftParticleRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VivifyHandler {

    private static Identifier id(String name) {
        return BeatCraft.id("textures/vivify/" + name + ".png");
    }

    private static final List<Triple<Identifier, Integer, Integer>> textures = List.of(
        Triple.of(id("blue_noise1"), 726, 885),
        Triple.of(id("blue_noise2"), 640, 557),
        Triple.of(id("dust1"), 1125, 1084),
        Triple.of(id("dust2"), 719, 690),
        Triple.of(id("dust3"), 308, 218),
        Triple.of(id("dust4"), 1080, 972),
        Triple.of(id("green_blue_gradient"), 640, 636),
        Triple.of(id("height_map1"), 517, 640),
        Triple.of(id("height_map2"), 750, 483),
        Triple.of(id("light_map"), 1125, 1122),
        Triple.of(id("mask1"), 643, 667),
        Triple.of(id("mask2"), 828, 712),
        Triple.of(id("normal_map"), 720, 890),
        Triple.of(id("perlin1"), 680, 631),
        Triple.of(id("perlin2"), 640, 477),
        Triple.of(id("perlin3"), 460, 568),
        Triple.of(id("red_blue_gradient"), 728, 425),
        Triple.of(id("red_green_gradient"), 478, 626),
        Triple.of(id("vignette"), 640, 683),
        Triple.of(id("white_noise"), 735, 897)
    );

    private static boolean spawnNew = true;

    protected static Random random = Random.create();

    private static class Img {
        private final Identifier tex;
        private Vector2f texSize;
        private final float spawnTime;
        private Quaternionf ori;
        private Vector3f pos;

        private Vector3f drift;

        public Img(Identifier tex, Vector2f texSize, float spawnTime) {
            this.tex = tex;
            this.texSize = texSize;
            this.spawnTime = spawnTime;
            ori = SpawnQuaternionPool.getRandomQuaternion();
            var x = random.nextBetween(5, 20) * (random.nextBoolean() ? 1 : -1);
            var y = random.nextBetween(-5, 10);
            var z = random.nextBetween(-15, 75);
            pos = new Vector3f(x, y, z);
            drift = BeatcraftParticleRenderer.randomDirection(random.nextBetween(0, 5)/100f);
        }

        public void render() {
            pos.add(drift);

            var tess = Tessellator.getInstance();
            var buffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

            var half = new Vector2f(texSize.x/200f, texSize.y/200f);

            var c0 = new Vector3f(-half.x, half.y, 0).rotate(ori).add(pos).sub(cameraPos);
            var c1 = new Vector3f(-half.x, -half.y, 0).rotate(ori).add(pos).sub(cameraPos);
            var c2 = new Vector3f(half.x, -half.y, 0).rotate(ori).add(pos).sub(cameraPos);
            var c3 = new Vector3f(half.x, half.y, 0).rotate(ori).add(pos).sub(cameraPos);

            buffer.vertex(c0).texture(1, 0).color(0xFFFFFFFF);
            buffer.vertex(c1).texture(1, 1).color(0xFFFFFFFF);
            buffer.vertex(c2).texture(0, 1).color(0xFFFFFFFF);
            buffer.vertex(c3).texture(0, 0).color(0xFFFFFFFF);

            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.setShaderTexture(0, tex);
            RenderSystem.enableDepthTest();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();

        }

        public boolean shouldRemove() {
            return (System.nanoTime() / 1_000_000_000f) - spawnTime > 16f;
        }

    }

    private static final ArrayList<Img> current = new ArrayList<>();

    public static void render() {

        var t = System.nanoTime() / 1_000_000_000f;

        t = t % 3;

        if (t <= 1 && spawnNew) {
            spawnNew = false;

            var c = random.nextBetween(0, textures.size()-1);

            var data = textures.get(c);

            var i = new Img(data.getLeft(), new Vector2f(data.getMiddle(), data.getRight()), System.nanoTime() / 1_000_000_000f);

            current.add(i);

        } else if (t > 1 && !spawnNew) {
            spawnNew = true;
        }

        current.removeIf(Img::shouldRemove);


        for (var i : current) {
            i.render();
        }

    }


}
