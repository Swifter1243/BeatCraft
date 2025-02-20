package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.menu.Menu;
import com.beatcraft.render.BeatcraftRenderer;
import com.beatcraft.render.DebugRenderer;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.dynamic_loader.DynamicTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

        protected void draw(DrawContext context) {
            context.push();
            render(context);
            children.forEach(w -> w.draw(context));
            context.pop();
        }

        protected abstract void render(DrawContext context);
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
        protected void render(DrawContext context) {
            context.translate(-position.x, -position.y, -position.z);

            // Handle collision
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
        protected void render(DrawContext context) {
            context.translate(-position.x, -position.y, -position.z);
            context.scale(-scale, -scale, -scale);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(text), 0, 0, color);
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
        protected void render(DrawContext context) {}
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
        protected void draw(DrawContext context) {
            context.push();
            render(context);
            if (state) {
                children.forEach(w -> w.draw(context));
            } else {
                childrenB.forEach(w -> w.draw(context));
            }
            context.pop();
        }

        @Override
        protected void render(DrawContext context) {

        }
    }


    protected static class HoverWidget extends Widget {
        protected List<Widget> childrenB;
        protected boolean hovered = false;

        /// childrenA are rendered when not hovered.
        /// childrenB are rendered when hovered
        protected HoverWidget(Vector3f position, List<Widget> childrenA, List<Widget> childrenB) {
            this.position = position;
            this.children = childrenA;
            this.childrenB = childrenB;
        }

        @Override
        protected void draw(DrawContext context) {
            context.push();
            render(context);
            if (hovered) {
                childrenB.forEach(w -> w.draw(context));
            } else {
                children.forEach(w -> w.draw(context));
            }
            context.pop();
        }

        @Override
        protected void render(DrawContext context) {

        }
    }

    // End widget definitions ------------------------------------------------------


    public void render(VertexConsumerProvider.Immediate immediate) {

        DrawContext context = new DrawContext(MinecraftClient.getInstance(), immediate);

        Vector3f camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        context.translate(-camPos.x, -camPos.y, -camPos.z);
        context.translate(position.x, position.y, position.z);

        context.multiply(orientation);
        context.scale(1/128f, 1/128f, 1/128f);
        context.push();

        context.fill((int) (-size.x/2f), (int) (-size.y/2f), (int) (size.x/2f), (int) (size.y/2f), backgroundColor);

        widgets.forEach(w -> w.draw(context));

        context.draw();
        context.pop();
    }

}
