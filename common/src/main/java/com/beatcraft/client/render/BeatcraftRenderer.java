package com.beatcraft.client.render;

import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.data.ColorScheme;
import com.beatcraft.client.render.effect.Bloomfog;
import com.beatcraft.client.render.effect.SaberRenderer;
import com.beatcraft.client.render.instancing.ArrowInstanceData;
import com.beatcraft.client.render.instancing.ColorNoteInstanceData;
import com.beatcraft.client.render.instancing.HeadsetInstanceData;
import com.beatcraft.client.render.mesh.MeshLoader;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.memory.MemoryPool;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeatcraftRenderer {

    public static Bloomfog bloomfog;

    public static ShaderInstance noteShader;
    public static ShaderInstance arrowShader;
    public static ShaderInstance heartHealthShader;
    public static ShaderInstance BCPosTexColShader;

    private static final ArrayList<Runnable> saberRenderCalls = new ArrayList<>();

    public static void init() {
        bloomfog = Bloomfog.create();

        try {
            noteShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "note_shader", DefaultVertexFormat.POSITION_TEX_COLOR);
            arrowShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "arrow_shader", DefaultVertexFormat.POSITION_TEX_COLOR);
            heartHealthShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "health_hearts", DefaultVertexFormat.POSITION_TEX_COLOR);
            BCPosTexColShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "bc_tex_col", DefaultVertexFormat.POSITION_TEX_COLOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void renderSky(Camera camera, float tickDelta) {
        BeatmapManager.updateMaps();
    }

    public static void renderBloomfog(float tickDelta) {
        BeatcraftRenderer.bloomfog.render(false, tickDelta);
    }

    public static void renderMirror() {

    }

    public static void renderHUD() {

    }

    public static void renderBeatmap(Camera camera) {

        BeatmapManager.preRenderMaps();
        var cameraPos = camera.getPosition().toVector3f();

        MeshLoader.COLOR_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_HEAD_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_LINK_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.BOMB_NOTE_INSTANCED_MESH.render(cameraPos);
        MeshLoader.NOTE_ARROW_INSTANCED_MESH.render(cameraPos);
        MeshLoader.NOTE_DOT_INSTANCED_MESH.render(cameraPos);
        MeshLoader.CHAIN_DOT_INSTANCED_MESH.render(cameraPos);
        MeshLoader.HEADSET_INSTANCED_MESH.render(cameraPos);

        BeatmapManager.renderMaps();

    }

    public static void renderParticles() {

    }

    public static void renderDebug(Vector3f cameraPos) {

        if (BeatcraftClient.playerConfig.debug.beatmap.renderBeatmapPosition()) {
            MeshLoader.MATRIX_LOCATOR_MESH.render(cameraPos);
        } else {
            MeshLoader.MATRIX_LOCATOR_MESH.cancelDraws();
        }

    }

    public static void recordSaberRenderCall(Runnable call) {
        saberRenderCalls.add(call);
    }

    public static void renderSabers() {

        for (var call : saberRenderCalls) {
            call.run();
        }
        saberRenderCalls.clear();

        SaberRenderer.renderAll();

    }

    public static void renderSmoke() {

    }

    public static void renderBloom() {
        BeatcraftRenderer.bloomfog.renderBloom();
    }


    // v HELPER FUNCTIONS v //

    public static List<Vector3f[]> getGlowingQuadAsTris(Vector2f quadSize, float glowSpread) {
        List<Vector3f[]> tris = new ArrayList<>();

        float halfWidth = quadSize.x / 2f;
        float halfHeight = quadSize.y / 2f;
        float outerHalfWidth = halfWidth + glowSpread;
        float outerHalfHeight = halfHeight + glowSpread;

        // Outer corners (alpha = 0)
        Vector3f topLeftOuter     = new Vector3f(-outerHalfWidth,  outerHalfHeight, 0f);
        Vector3f topRightOuter    = new Vector3f( outerHalfWidth,  outerHalfHeight, 0f);
        Vector3f bottomLeftOuter  = new Vector3f(-outerHalfWidth, -outerHalfHeight, 0f);
        Vector3f bottomRightOuter = new Vector3f( outerHalfWidth, -outerHalfHeight, 0f);

        // Inner corners (alpha = 1)
        Vector3f topLeftInner     = new Vector3f(-halfWidth,  halfHeight, 1f);
        Vector3f topRightInner    = new Vector3f( halfWidth,  halfHeight, 1f);
        Vector3f bottomLeftInner  = new Vector3f(-halfWidth, -halfHeight, 1f);
        Vector3f bottomRightInner = new Vector3f( halfWidth, -halfHeight, 1f);

        // Center quad
        tris.add(new Vector3f[] { topLeftInner, bottomLeftInner, bottomRightInner });
        tris.add(new Vector3f[] { topLeftInner, bottomRightInner, topRightInner });

        // Top glow
        tris.add(new Vector3f[] { topLeftOuter, topLeftInner, topRightInner });
        tris.add(new Vector3f[] { topLeftOuter, topRightInner, topRightOuter });

        // Bottom glow
        tris.add(new Vector3f[] { bottomLeftInner, bottomLeftOuter, bottomRightOuter });
        tris.add(new Vector3f[] { bottomLeftInner, bottomRightOuter, bottomRightInner });

        // Left glow
        tris.add(new Vector3f[] { topLeftInner, topLeftOuter, bottomLeftOuter });
        tris.add(new Vector3f[] { topLeftInner, bottomLeftOuter, bottomLeftInner });

        // Right glow
        tris.add(new Vector3f[] { bottomRightInner, bottomRightOuter, topRightOuter });
        tris.add(new Vector3f[] { bottomRightInner, topRightOuter, topRightInner });

        return tris;
    }


    public static List<Vector3f[]> getCubeFaces(
        Vector3f vxyz, Vector3f vxyZ, Vector3f vXyZ, Vector3f vXyz,
        Vector3f vxYz, Vector3f vxYZ, Vector3f vXYZ, Vector3f vXYz,
        boolean includeBottomFace
    ) {
        var faces = new ArrayList<Vector3f[]>();

        faces.add(new Vector3f[] {
            vXYz, vXYZ, vxYZ, vxYz
        });
        faces.add(new Vector3f[] {
            vxyZ, vXyZ, vXYZ, vxYZ
        });
        faces.add(new Vector3f[] {
            vXyz, vxyz, vxYz, vXYz
        });
        faces.add(new Vector3f[] {
            vxyz, vxyZ, vxYZ, vxYz
        });
        faces.add(new Vector3f[] {
            vXyZ, vXyz, vXYz, vXYZ
        });
        if (includeBottomFace) {
            faces.add(new Vector3f[] {
                vxyz, vXyz, vXyZ, vxyZ
            });
        }

        return faces;
    }

    public static List<Vector3f[]> getCubeEdges(Vector3f minPos, Vector3f maxPos) {
        List<Vector3f[]> edges = new ArrayList<>();

        Vector3f[] corners = new Vector3f[] {
            MemoryPool.newVector3f(minPos.x, minPos.y, minPos.z),
            MemoryPool.newVector3f(maxPos.x, minPos.y, minPos.z),
            MemoryPool.newVector3f(maxPos.x, maxPos.y, minPos.z),
            MemoryPool.newVector3f(minPos.x, maxPos.y, minPos.z),
            MemoryPool.newVector3f(minPos.x, minPos.y, maxPos.z),
            MemoryPool.newVector3f(maxPos.x, minPos.y, maxPos.z),
            MemoryPool.newVector3f(maxPos.x, maxPos.y, maxPos.z),
            MemoryPool.newVector3f(minPos.x, maxPos.y, maxPos.z)
        };

        int[][] edgeIndices = new int[][] {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] pair : edgeIndices) {
            edges.add(new Vector3f[]{
                corners[pair[0]],
                corners[pair[1]]
            });
        }

        return edges;
    }


    public static List<Vector3f[]> getCubeFaces(Vector3f minPos, Vector3f maxPos) {
        List<Vector3f[]> faces = new ArrayList<>();

        Vector3f[] corners = new Vector3f[] {
            new Vector3f(minPos.x, minPos.y, minPos.z),
            new Vector3f(maxPos.x, minPos.y, minPos.z),
            new Vector3f(maxPos.x, maxPos.y, minPos.z),
            new Vector3f(minPos.x, maxPos.y, minPos.z),
            new Vector3f(minPos.x, minPos.y, maxPos.z),
            new Vector3f(maxPos.x, minPos.y, maxPos.z),
            new Vector3f(maxPos.x, maxPos.y, maxPos.z),
            new Vector3f(minPos.x, maxPos.y, maxPos.z)
        };


        int[][] faceIndices = new int[][] {
            {3, 2, 1, 0}, // F
            {4, 5, 6, 7}, // B
            {4, 7, 3, 0}, // L
            {2, 6, 5, 1}, // R
            {7, 6, 2, 3}, // T
            {1, 5, 4, 0}  // D
        };

        for (int[] pair : faceIndices) {
            faces.add(new Vector3f[]{
                corners[pair[0]],
                corners[pair[1]],
                corners[pair[2]],
                corners[pair[3]]
            });
        }

        return faces;
    }


}
