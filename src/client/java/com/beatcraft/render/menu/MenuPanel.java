package com.beatcraft.render.menu;

import blue.endless.jankson.annotation.Nullable;
import com.beatcraft.menu.Menu;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import com.beatcraft.utils.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class MenuPanel<T extends Menu> {

    protected Vector3f position = new Vector3f();
    protected Quaternionf orientation = new Quaternionf();
    protected Vector2f size = new Vector2f();
    protected int backgroundColor = 0x90000000;
    protected T data;
    protected ArrayList<Widget> widgets = new ArrayList<>();

    public MenuPanel(T data) {
        this.data = data;
    }


    // Widget Base -----------------------------------------------------------------

    protected abstract static class Widget {
        protected Vector3f position = new Vector3f();
        protected Vector2f size = new Vector2f();
        protected List<Widget> children = new ArrayList<>();

        protected void draw(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.push();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));
            render(context, p);
            children.forEach(w -> w.draw(context, p));
            context.pop();
        }

        protected abstract void render(DrawContext context, @Nullable Vector2f pointerPosition);
    }

    // Widget definitions ----------------------------------------------------------

    protected static class ButtonWidget extends Widget {
        protected Runnable onClickHandler;

        protected ButtonWidget(Vector3f position, Vector2f size, Runnable onClickHandler, Widget... children) {
            this.position = position;
            this.size = size;
            this.onClickHandler = onClickHandler;
            this.children = Arrays.stream(children).toList();
        }

        @Override
        protected void render(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.translate(-position.x, -position.y, -position.z);

            // Handle collision
            if (pointerPosition != null && MathUtil.check2DPointCollision(pointerPosition, new Vector2f(), this.size)) {

            }
        }
    }

    protected static class TextWidget extends Widget {
        protected String text;
        protected float scale = 1;
        public int color = 0xFFFFFFFF;

        protected TextWidget(String text, Vector3f position, float scale) {
            this.text = text;
            this.position = position;
            this.scale = scale;
        }

        protected TextWidget(String text, Vector3f position) {
            this.text = text;
            this.position = position;
        }

        protected TextWidget withColor(int color) {
            this.color = color;
            return this;
        }

        @Override
        protected void render(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.translate(-position.x, -position.y, -position.z);
            context.scale(-scale, -scale, -scale);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(text), 0, 0, color);
            if (pointerPosition != null) {
                pointerPosition.mul(scale);
            }
        }
    }

    protected static class TextureWidget extends Widget {
        protected Identifier texture = null;
        protected DynamicTexture dynamicTexture = null;

        protected TextureWidget(Identifier texture, Vector3f position) {
            this.texture = texture;
            this.position = position;
        }

        protected TextureWidget(DynamicTexture texture, Vector3f position) {
            this.position = position;
            this.dynamicTexture = texture;
            this.texture = texture.id();
        }

        @Override
        protected void render(DrawContext context, @Nullable Vector2f pointerPosition) {}
    }

    protected static class ToggleWidget extends Widget {
        protected List<Widget> childrenB;
        protected Consumer<Boolean> changeHandler;
        protected boolean state = true;

        /// position and size refer to the hitbox size.
        /// childrenA are rendered while the toggle is in the `true` position
        /// childrenB are rendered in the `false` position
        /// toggleHandler is called and passed the new toggle state when the toggle is clicked
        protected ToggleWidget(Vector3f position, Vector2f size, List<Widget> childrenA, List<Widget> childrenB, Consumer<Boolean> toggleHandler) {
            this.children = childrenA;
            this.childrenB = childrenB;
            this.position = position;
            this.size = size;
            this.changeHandler = toggleHandler;
        }

        @Override
        protected void draw(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.push();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));
            render(context, p);
            if (state) {
                children.forEach(w -> w.draw(context, p));
            } else {
                childrenB.forEach(w -> w.draw(context, p));
            }
            context.pop();
        }

        @Override
        protected void render(DrawContext context, @Nullable Vector2f pointerPosition) {

        }
    }


    protected static class HoverWidget extends Widget {
        protected List<Widget> childrenB;
        protected boolean hovered = false;

        /// childrenA are rendered when not hovered.
        /// childrenB are rendered when hovered
        protected HoverWidget(Vector3f position, Vector2f size, List<Widget> childrenA, List<Widget> childrenB) {
            this.position = position;
            this.size = size;
            this.children = childrenA;
            this.childrenB = childrenB;
        }

        @Override
        protected void draw(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.push();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));
            render(context, p);
            if (hovered) {
                childrenB.forEach(w -> w.draw(context, p));
            } else {
                children.forEach(w -> w.draw(context, p));
            }
            context.pop();
        }

        @Override
        protected void render(DrawContext context, @Nullable Vector2f pointerPosition) {
            context.translate(-position.x, -position.y, -position.z);

            // Handle collision
            if (pointerPosition != null) {
                hovered = MathUtil.check2DPointCollision(pointerPosition, new Vector2f(), this.size);
            }
        }
    }

    protected static class GradientWidget extends Widget {

        private int col1;
        private int col2;

        protected GradientWidget(Vector3f position, Vector2f size, int col1, int col2) {
            this.position = position;
            this.size = size;
            this.col1 = col1;
            this.col2 = col2;
        }

        @Override
        protected void render(DrawContext context, Vector2f pointerPosition) {
            context.translate(-position.x, -position.y, -position.z);

            context.fillGradient((int) -size.x/2, (int) -size.y/2, (int) size.x/2, (int) size.y/2, col1, col2);
        }
    }

    // End widget definitions ------------------------------------------------------

    public Pair<Vector3f, Vector2f> raycast(Vector3f position, Quaternionf orientation) {
        return MathUtil.raycastPlane(position, orientation, this.position, this.orientation, this.size.div(128, new Vector2f()));
    }

    public Vector3f getNormal() {
        return new Vector3f(0, 0, -1).rotate(orientation);
    }

    public void render(VertexConsumerProvider.Immediate immediate, @Nullable Vector2f pointerPosition) {

        DrawContext context = new DrawContext(MinecraftClient.getInstance(), immediate);

        Vector3f camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        context.translate(-camPos.x, -camPos.y, -camPos.z);
        context.translate(position.x, position.y, position.z);

        context.multiply(orientation);
        context.scale(1/128f, 1/128f, 1/128f);
        context.push();

        context.fill((int) (-size.x/2f), (int) (-size.y/2f), (int) (size.x/2f), (int) (size.y/2f), backgroundColor);

        widgets.forEach(w -> w.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f())));

        context.draw();
        context.pop();
    }

}
