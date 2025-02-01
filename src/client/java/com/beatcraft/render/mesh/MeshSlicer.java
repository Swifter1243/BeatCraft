package com.beatcraft.render.mesh;

import java.util.*;

import com.beatcraft.BeatCraft;
import com.beatcraft.utils.MathUtil;
import net.minecraft.util.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;


public class MeshSlicer {
    public static Pair<TriangleMesh, TriangleMesh> sliceMesh(Vector3f planeIncidentPoint, Vector3f planeNormal, QuadMesh mesh) {
        TriangleMesh leftMesh = new TriangleMesh();
        TriangleMesh rightMesh = new TriangleMesh();


        for (Quad quad : mesh.quads) {
            List<Pair<Vector3f, Vector2f>> left = new ArrayList<>();
            List<Pair<Vector3f, Vector2f>> right = new ArrayList<>();

            List<Pair<Vector3f, Vector2f>> vertices = mesh.getQuadVerticesAndUvs(quad);
            for (Pair<Vector3f, Vector2f> vertex_uv : vertices) {
                if (isAbovePlane(vertex_uv.getLeft(), planeIncidentPoint, planeNormal)) {
                    left.add(vertex_uv);
                    right.add(null);
                } else {
                    left.add(null);
                    right.add(vertex_uv);
                }
            }

            if (!left.contains(null)) {
                leftMesh.addTris(quad.toTriangles(mesh, leftMesh));
            } else if (!right.contains(null)) {
                rightMesh.addTris(quad.toTriangles(mesh, rightMesh));
            } else {

                List<Pair<Vector3f, Vector2f>> intersections = getIntersections(vertices, planeIncidentPoint, planeNormal);

                var leftTris = reconstruct(leftMesh.vertices, left, intersections);
                var rightTris = reconstruct(rightMesh.vertices, right, intersections);

                leftMesh.tris.addAll(leftTris);

                rightMesh.tris.addAll(rightTris);
            }
        }

        return new Pair<>(leftMesh, rightMesh);
    }

    private static int getNull(List<Pair<Vector3f, Vector2f>> ls) {
        return (
            (ls.getFirst() != null ? 8 : 0) +
                (ls.get(1) != null ? 4 : 0) +
                (ls.get(2) != null ? 2 : 0) +
                (ls.get(3) != null ? 1 : 0)
        );
    }

    private static final HashMap<Integer, int[][]> flagMap = new HashMap<>();

    static {
        // cut straight across (quad) [2 tris]
        flagMap.put(0b1001_1010, new int[][]{{0, 4, 6}, {6, 0, 3}});
        flagMap.put(0b1100_0101, new int[][]{{0, 1, 5}, {5, 0, 7}});
        flagMap.put(0b0110_1010, new int[][]{{4, 1, 2}, {2, 4, 6}});
        flagMap.put(0b0011_0101, new int[][]{{7, 5, 2}, {2, 7, 3}});

        // cut on immediate corners [1 tri]
        flagMap.put(0b0100_1100, new int[][]{{1, 4, 5}});
        flagMap.put(0b0010_0110, new int[][]{{2, 5, 6}});
        flagMap.put(0b0001_0011, new int[][]{{3, 6, 7}});
        flagMap.put(0b1000_1001, new int[][]{{0, 7, 4}});

        // cut on far corners (pentagon) [3 tris]
        flagMap.put(0b1110_0011, new int[][]{{0, 2, 6}, {0, 1, 2}, {6, 0, 7}});
        flagMap.put(0b0111_1001, new int[][]{{1, 3, 7}, {1, 2, 3}, {7, 1, 4}});
        flagMap.put(0b1011_1100, new int[][]{{0, 2, 5}, {2, 3, 0}, {5, 0, 4}});
        flagMap.put(0b1101_0110, new int[][]{{1, 3, 6}, {3, 0, 1}, {6, 1, 5}});
    }

    private static List<Triangle> reconstruct(ArrayList<Vector3f> vertices, List<Pair<Vector3f, Vector2f>> square, List<Pair<Vector3f, Vector2f>> intersects) {
        int sFlag = getNull(square);
        int iFlag = getNull(intersects);

        List<Pair<Vector3f, Vector2f>> combined = new ArrayList<>(square);
        combined.addAll(intersects);

        int[][] indices = flagMap.get((sFlag << 4) | iFlag);

        if (indices == null) {
            return List.of();
        } else {
            List<List<Pair<Vector3f, Vector2f>>> out = new ArrayList<>();

            for (int[] tri : indices) {
                out.add(List.of(
                    combined.get(tri[0]),
                    combined.get(tri[1]),
                    combined.get(tri[2])
                ));
            }

            return Triangle.fromList(vertices, out);
        }
    }

    private static boolean isAbovePlane(Vector3f point, Vector3f planePoint, Vector3f planeNormal) {
        return planeNormal.dot(new Vector3f(point).sub(planePoint)) > 0;
    }

    private static List<Pair<Vector3f, Vector2f>> getIntersections(List<Pair<Vector3f, Vector2f>> quad, Vector3f planePoint, Vector3f planeNormal) {
        ArrayList<Pair<Vector3f, Vector2f>> intersections = new ArrayList<>();
        for (int i = 0; i < quad.size(); i++) {
            Pair<Vector3f, Vector2f> v1 = quad.get(i);
            Pair<Vector3f, Vector2f> v2 = quad.get((i + 1) % quad.size());
            if (isAbovePlane(v1.getLeft(), planePoint, planeNormal) != isAbovePlane(v2.getLeft(), planePoint, planeNormal)) {
                intersections.add(getIntersect(v1, v2, planePoint, planeNormal));
            } else {
                intersections.add(null);
            }
        }
        return intersections;
    }

    private static Pair<Vector3f, Vector2f> getIntersect(Pair<Vector3f, Vector2f> v1, Pair<Vector3f, Vector2f> v2, Vector3f planePoint, Vector3f planeNormal) {
        Vector3f edge = new Vector3f(v2.getLeft()).sub(v1.getLeft());
        float t = (planeNormal.dot(new Vector3f(planePoint).sub(v1.getLeft()))) / planeNormal.dot(edge);

        Vector3f pos = new Vector3f(v1.getLeft()).add(edge.mul(t));

        float f = MathUtil.inverseLerpVector3(v1.getLeft(), v2.getLeft(), pos);

        Vector2f uv = MathUtil.lerpVector2(v1.getRight(), v2.getRight(), f);

        return new Pair<>(pos, uv);
    }

}
