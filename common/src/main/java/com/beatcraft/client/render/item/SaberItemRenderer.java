package com.beatcraft.client.render.item;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class SaberItemRenderer {

    public static class TrailRenderer {
        private boolean initialized = false;
        private int size;
        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;
        private int head = 0;
        private ByteBuffer ring = null;
        private final float[] stage = new float[10];

        public TrailRenderer(int size) {
            this.size = size;
            setup();
        }

        public void resize(int newSize) {
            size = newSize;
            reset();
        }

        public void setup() {
            if (initialized) return;
            reset();
        }

        public void writeNext(Vector3f pos1, Vector2f uv1, Vector3f pos2, Vector2f uv2) {
            // write old data at old head position now that draw is done with what was there before
            ring.position(head * 5 * Float.BYTES * 2); // step by 2 vertices
            ring.asFloatBuffer().put(stage);

            // store the current data to be written to the head position next frame
            stage[0] = pos1.x;
            stage[1] = pos1.y;
            stage[2] = pos1.z;
            stage[3] = uv1.x;
            stage[4] = uv1.y;
            stage[5] = pos2.x;
            stage[6] = pos2.y;
            stage[7] = pos2.z;
            stage[8] = uv2.x;
            stage[9] = uv2.y;

            // write current data just after the current window
            ring.position((head + size) * 5 * Float.BYTES * 2);
            ring.asFloatBuffer().put(stage);

            // slide window forward
            head = (head + 1) % size;
        }

        public void reset() {
            initialized = true;
            if (vbo != 0) {
                GL15.glDeleteBuffers(vbo);
                vbo = 0;
            }
            if (ebo != 0) {
                GL15.glDeleteBuffers(ebo);
                ebo = 0;
            }

            // size * 2 vertices * 2 regions * stride (3: pos, 2: uv)
            var stride = 5 * Float.BYTES;
            var totalBufferBytes = size * 2 * 2 * stride;

            if (vao == 0) {
                vao = GL30.glGenVertexArrays();
            }
            GL30.glBindVertexArray(vao);
            if (vbo == 0) {
                vbo = GL15.glGenBuffers();
            }
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            int flags = GL44.GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT | GL44.GL_MAP_COHERENT_BIT;
            GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, totalBufferBytes, flags);
            ring = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, totalBufferBytes, flags);

            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
            GL20.glEnableVertexAttribArray(1);

            if (ebo == 0) {
                ebo = GL15.glGenBuffers();
            }
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
            int[] indices = new int[size * 2 * 6]; // size * 2 vertices * 6 vertex indices
            for (int i = 0; i < size * 2; ++i) {
                int b = i * 2;
                indices[i*6] = b;
                indices[i*6+1] = b+1;
                indices[i*6+2] = b+2;
                indices[i*6+3] = b+2;
                indices[i*6+4] = b+1;
                indices[i*6+5] = b+3;
            }
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
            GL30.glBindVertexArray(0);
        }

        public void cleanup() {
            if (vbo != 0) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
                GL15.glDeleteBuffers(vbo); vbo = 0;
            }
            if (ebo != 0) {
                GL15.glDeleteBuffers(ebo); ebo = 0;
            }
            if (vao != 0) {
                GL30.glDeleteVertexArrays(vao);
                vao = 0;
            }
            initialized = false;
            ring = null;
            head = 0;
        }

        public void render() {
            if (!initialized) return;

            GL30.glBindVertexArray(vao);

            GL32.glDrawElementsBaseVertex(
                GL11.GL_TRIANGLES,
                size * 6,
                GL11.GL_UNSIGNED_INT,
                0,
                head * 2
            );

        }

    }

}
