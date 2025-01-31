package com.beatcraft.data.types;

import java.util.*;
import net.minecraft.util.Pair;
import org.joml.Vector3f;


public class MeshSlicer {
    public static Pair<List<Vector3f[]>, List<Vector3f[]>> sliceMesh(Vector3f planeIncidentPoint, Vector3f planeNormal, List<Vector3f[]> quads) {
        List<Vector3f[]> leftMesh = new ArrayList<>();
        List<Vector3f[]> rightMesh = new ArrayList<>();

        for (Vector3f[] quad : quads) {
            List<Vector3f> left = new ArrayList<>();
            List<Vector3f> right = new ArrayList<>();

            for (Vector3f vertex : quad) {
                if (isAbovePlane(vertex, planeIncidentPoint, planeNormal)) {
                    left.add(vertex);
                    right.add(null);
                } else {
                    left.add(null);
                    right.add(vertex);
                }
            }

            if (!left.contains(null)) {
                leftMesh.add(quad);
            } else if (!right.contains(null)) {
                rightMesh.add(quad);
            } else {

                List<Vector3f> intersections = getIntersections(quad, planeIncidentPoint, planeNormal);

                var leftQuads = reconstruct(left, intersections);
                var rightQuads = reconstruct(right, intersections);

                for (var leftQuad : leftQuads) {
                    if (!leftQuad.isEmpty()) leftMesh.add(leftQuad.toArray(new Vector3f[0]));
                }

                for (var rightQuad : rightQuads) {
                    if (!rightQuad.isEmpty()) rightMesh.add(rightQuad.toArray(new Vector3f[0]));
                }
            }
        }

        return new Pair<>(leftMesh, rightMesh);
    }

    private static int getNull(List<Vector3f> ls) {
        return (
            (ls.getFirst() != null ? 8 : 0) +
                (ls.get(1) != null ? 4 : 0) +
                (ls.get(2) != null ? 2 : 0) +
                (ls.get(3) != null ? 1 : 0)
        );
    }

    private static final HashMap<Integer, int[][]> flagMap = new HashMap<>();

    static {
        // cut straight across (quad)
        flagMap.put(0b1001_1010, new int[][]{{0, 4, 6, 3}});
        flagMap.put(0b1100_0101, new int[][]{{0, 1, 5, 7}});
        flagMap.put(0b0110_1010, new int[][]{{4, 1, 2, 6}});
        flagMap.put(0b0011_0101, new int[][]{{7, 5, 2, 3}});

        // cut on immediate corners (triangle)
        flagMap.put(0b0100_1100, new int[][]{{1, 1, 4, 5}});
        flagMap.put(0b0010_0110, new int[][]{{2, 2, 5, 6}});
        flagMap.put(0b0001_0011, new int[][]{{3, 3, 6, 7}});
        flagMap.put(0b1000_1001, new int[][]{{0, 0, 7, 4}});

        // cut on far corners (pentagon) [quad + tri]
        flagMap.put(0b1110_0011, new int[][]{{0, 2, 6, 7}, {0, 0, 1, 2}});
        flagMap.put(0b0111_1001, new int[][]{{1, 3, 7, 4}, {1, 1, 2, 3}});
        flagMap.put(0b1011_1100, new int[][]{{0, 2, 5, 4}, {2, 2, 3, 0}});
        flagMap.put(0b1101_0110, new int[][]{{1, 3, 6, 5}, {3, 3, 0, 1}});
    }

    private static List<List<Vector3f>> reconstruct(List<Vector3f> square, List<Vector3f> intersects) {
        int sFlag = getNull(square);
        int iFlag = getNull(intersects);

        List<Vector3f> combined = new ArrayList<>(square);
        combined.addAll(intersects);

        int[][] indices = flagMap.get((sFlag << 4) | iFlag);

        if (indices == null) {
            return List.of(square);
        } else {
            List<List<Vector3f>> out = new ArrayList<>();

            for (int[] quad : indices) {
                out.add(List.of(
                    combined.get(quad[0]),
                    combined.get(quad[1]),
                    combined.get(quad[2]),
                    combined.get(quad[3])
                ));
            }

            return out;
        }
    }

    private static boolean isAbovePlane(Vector3f point, Vector3f planePoint, Vector3f planeNormal) {
        return planeNormal.dot(new Vector3f(point).sub(planePoint)) > 0;
    }

    private static List<Vector3f> getIntersections(Vector3f[] quad, Vector3f planePoint, Vector3f planeNormal) {
        ArrayList<Vector3f> intersections = new ArrayList<>();
        for (int i = 0; i < quad.length; i++) {
            Vector3f v1 = quad[i];
            Vector3f v2 = quad[(i + 1) % quad.length];
            if (isAbovePlane(v1, planePoint, planeNormal) != isAbovePlane(v2, planePoint, planeNormal)) {
                intersections.add(getIntersect(v1, v2, planePoint, planeNormal));
            } else {
                intersections.add(null);
            }
        }
        return intersections;
    }

    private static Vector3f getIntersect(Vector3f v1, Vector3f v2, Vector3f planePoint, Vector3f planeNormal) {
        Vector3f edge = new Vector3f(v2).sub(v1);
        float t = (planeNormal.dot(new Vector3f(planePoint).sub(v1))) / planeNormal.dot(edge);
        return new Vector3f(v1).add(edge.mul(t));
    }

}
