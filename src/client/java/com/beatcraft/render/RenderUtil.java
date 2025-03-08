package com.beatcraft.render;

import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class RenderUtil {
    public static Quaternionf getBlockRenderOrientation(Direction face) {
        Quaternionf q = new Quaternionf();

        switch (face) {
            case DOWN -> q.identity();
            case UP -> q.rotationX((float) Math.PI);
            case NORTH -> q.rotationX((float) Math.PI / 2);
            case SOUTH -> q.rotationX((float) -Math.PI / 2);
            case EAST -> q.rotationZ((float) Math.PI / 2);
            case WEST -> q.rotationZ((float) -Math.PI / 2);
        }
        return q;
    }

    public static Quaternionf getBlockRenderOrientation(Direction face, Direction rotation) {
        Quaternionf q = new Quaternionf();

        switch (face) {
            case DOWN -> q.identity();
            case UP -> q.rotationX((float) Math.PI);
            case NORTH -> q.rotationX((float) Math.PI / 2);
            case SOUTH -> q.rotationX((float) -Math.PI / 2);
            case EAST -> q.rotationZ((float) Math.PI / 2);
            case WEST -> q.rotationZ((float) -Math.PI / 2);
        }

        var local = new Vector3f(0, 0, -1);
        var target = rotation.getUnitVector().rotate(q.conjugate(new Quaternionf()));

        return q.mul(rotationBetween(local, target));
    }

    public static Quaternionf rotationBetween(Vector3f local, Vector3f target) {
        Vector3f axis = new Vector3f();
        local.cross(target, axis);
        float dot = local.dot(target);
        float angle = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, dot)));
        if (axis.lengthSquared() < 0.0001f) {
            if (dot > 0) {
                return new Quaternionf().identity();
            } else {
                axis = findPerpendicular(local).normalize();
                return new Quaternionf().rotationAxis((float) Math.PI, axis);
            }
        }

        return new Quaternionf().rotationAxis(angle, axis.normalize());
    }

    private static Vector3f findPerpendicular(Vector3f v) {
        return Math.abs(v.x) > Math.abs(v.z) ? new Vector3f(-v.y, v.x, 0) : new Vector3f(0, -v.z, v.y);
    }

    public static List<Vector3f[]> chopEdge(Vector3f a, Vector3f b) {
        return chopEdge(a, b, 5);
    }

    public static List<Vector3f[]> chopEdge(Vector3f a, Vector3f b, float subsection_size) {
        ArrayList<Vector3f[]> segments = new ArrayList<>();

        Vector3f direction = b.sub(a, new Vector3f());
        direction.normalize();
        direction.mul(subsection_size);
        Vector3f c = a;
        while (a.distance(b) > subsection_size) {
            c = new Vector3f(a).add(direction);
            segments.add(new Vector3f[]{a, c});
            a = c;
        }
        segments.add(new Vector3f[]{c, b});

        return segments;
    }

    public static List<Vector3f[]> sliceQuad(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, float subsection_size) {
        List<Vector3f[]> slicedQuads = new ArrayList<>();

        // Step 1: Chop the left and right edges into segments
        List<Vector3f[]> leftSegments = chopEdge(v0, v3, subsection_size);
        List<Vector3f[]> rightSegments = chopEdge(v1, v2, subsection_size);

        // Ensure we have at least two segments to form a quad
        int numRows = Math.min(leftSegments.size(), rightSegments.size());
        if (numRows < 1) return slicedQuads;

        // Step 2: Process each horizontal row
        for (int i = 0; i < numRows; i++) {
            Vector3f leftTop = leftSegments.get(i)[0];   // Start of left segment
            Vector3f leftBottom = leftSegments.get(i)[1]; // End of left segment

            Vector3f rightTop = rightSegments.get(i)[0];   // Start of right segment
            Vector3f rightBottom = rightSegments.get(i)[1]; // End of right segment

            // Chop horizontal edges
            List<Vector3f[]> topSegments = chopEdge(leftTop, rightTop, subsection_size);
            List<Vector3f[]> bottomSegments = chopEdge(leftBottom, rightBottom, subsection_size);

            int numCols = Math.min(topSegments.size(), bottomSegments.size());
            if (numCols < 1) continue; // Need at least one valid segment to form a quad

            // Step 3: Generate quads
            for (int j = 0; j < numCols; j++) {
                Vector3f tL = topSegments.get(j)[1];   // Top-left (end of segment)
                Vector3f tR = topSegments.get(j)[0];   // Top-right (start of segment)
                Vector3f bL = bottomSegments.get(j)[1]; // Bottom-left (end of segment)
                Vector3f bR = bottomSegments.get(j)[0]; // Bottom-right (start of segment)

                slicedQuads.add(new Vector3f[]{tL, tR, bR, bL});
            }
        }

        return slicedQuads;
    }

}
