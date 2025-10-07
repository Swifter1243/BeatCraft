package com.beatcraft.client.render.menu;

import com.beatcraft.client.menu.Menu;
import com.beatcraft.client.render.dynamic_loader.DynamicTexture;
import com.beatcraft.common.utils.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
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
        protected ArrayList<Widget> children = new ArrayList<>();

        protected void draw(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().popPose();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));

            render(context, p, triggerPressed);
            children.forEach(w -> w.draw(context, p, triggerPressed));
            context.pose().popPose();
        }

        protected abstract void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed);
    }

    // Widget definitions ----------------------------------------------------------

    protected static class ButtonWidget extends Widget {
        protected Runnable onClickHandler;

        protected ButtonWidget(Vector3f position, Vector2f size, Runnable onClickHandler, Widget... children) {
            this.position = position;
            this.size = size;
            this.onClickHandler = onClickHandler;
            this.children = new ArrayList<>(Arrays.stream(children).toList());
        }

        @Override
        protected void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);

            // Handle collision
            if (pointerPosition != null && MathUtil.check2DPointCollision(pointerPosition, new Vector2f(), this.size)) {
                if (triggerPressed) {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.2f, 1);
                    onClickHandler.run();
                }
            }
        }
    }

    protected static class TextWidget extends Widget {
        protected String text;
        protected float scale = 1;
        public int color = 0xFFFFFFFF;
        private int alignment = 1;
        private int wrapWidth = 0;
        private boolean doDynamicScaling = false;
        private int scalingWidth = 0;
        private boolean doDynamicUpdating = false;
        private Callable<String> textGetter = null;

        protected TextWidget(Callable<String> textProvider, Vector3f position, float scale) {
            try {
                this.text = textProvider.call();
                this.doDynamicUpdating = true;
                this.textGetter = textProvider;
                this.position = position;
                this.scale = scale;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected TextWidget(Callable<String> textProvider, Vector3f position) {
            try {
                this.text = textProvider.call();
                this.doDynamicUpdating = true;
                this.textGetter = textProvider;
                this.position = position;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected TextWidget(String text, Vector3f position, float scale) {
            this.text = text;
            this.position = position;
            this.scale = scale;
        }

        protected TextWidget(String text, Vector3f position) {
            this.text = text;
            this.position = position;
        }

        protected TextWidget(Component text, Vector3f position) {
            this(text::getString, position);
        }

        protected TextWidget(Component text, Vector3f position, float scale) {
            this(text::getString, position, scale);
        }

        protected TextWidget alignedLeft() {
            alignment = 0;
            return this;
        }

        protected TextWidget withWrapWidth(int width) {
            wrapWidth = width;
            alignment = -1;
            return this;
        }

        protected TextWidget withColor(int color) {
            this.color = color;
            return this;
        }

        protected TextWidget withScale(float scale) {
            this.scale = scale;
            return this;
        }

        protected TextWidget withDynamicScaling(int maxWidth) {
            this.scalingWidth = maxWidth;
            this.doDynamicScaling = true;
            return this;
        }

        @Override
        protected void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);

            context.pose().scale(-scale, -scale, -scale);

            if (doDynamicUpdating) {
                try {
                    text = textGetter.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if (doDynamicScaling) {
                int currentWidth = Minecraft.getInstance().font.width(text);
                if (currentWidth > scalingWidth) {
                    float rescale = ((float) scalingWidth) / ((float) currentWidth);
                    context.pose().pushPose();
                    context.pose().scale(rescale, rescale, rescale);
                }
            }
            if (alignment == 0) {
                context.drawString(Minecraft.getInstance().font, Component.literal(text), 0, 0, color, false);
            } else if (alignment == 1) {
                context.drawString(Minecraft.getInstance().font, Component.literal(text), -Minecraft.getInstance().font.width(text)/2, 0, color, false);
            } else if (alignment == -1) {
                context.drawWordWrap(Minecraft.getInstance().font, Component.literal(text), -wrapWidth/2, 0, wrapWidth, color);
            }
            if (pointerPosition != null) {
                pointerPosition.mul(scale);
            }
            // undo scaling
            if (doDynamicScaling) {
                int currentWidth = Minecraft.getInstance().font.width(text);
                if (currentWidth > scalingWidth) {
                    context.pose().popPose();
                }
            }
            context.pose().scale(-1/scale, -1/scale, -1/scale);

        }
    }

    protected static class TextureWidget extends Widget {
        protected ResourceLocation texture = null;
        protected DynamicTexture dynamicTexture = null;
        protected float scaleX = 1;
        protected float scaleY = 1;

        protected TextureWidget(ResourceLocation texture, Vector3f position, Vector2f size) {
            this.texture = texture;
            this.position = position;
            this.size = size;
        }

        protected TextureWidget(DynamicTexture texture, Vector3f position) {
            this.position = position;
            this.dynamicTexture = texture;
            this.texture = texture.id();
        }

        protected TextureWidget withScale(float scale) {
            this.scaleX = scale;
            this.scaleY = scale;
            return this;
        }

        protected TextureWidget withScale(float scaleX, float scaleY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            return this;
        }

        @Override
        protected void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);
            context.pose().scale(-scaleX, -scaleY, -1);
            context.blit(this.texture, -(int) (this.size.x/2), -(int) (this.size.y/2), (int) this.size.x, (int) this.size.y, (int) this.size.x, (int) this.size.y, (int) this.size.x, (int) this.size.y);
        }
    }

    protected static class ToggleWidget extends Widget {
        protected ArrayList<Widget> childrenB;
        protected Consumer<Boolean> changeHandler;
        protected boolean state = false;

        /// position and size refer to the hitbox size.
        /// childrenA are rendered while the toggle is in the `true` position
        /// childrenB are rendered in the `false` position
        /// toggleHandler is called and passed the new toggle state when the toggle is clicked
        protected ToggleWidget(Vector3f position, Vector2f size, List<Widget> childrenA, List<Widget> childrenB, Consumer<Boolean> toggleHandler) {
            this.children = new ArrayList<>(childrenA);
            this.childrenB = new ArrayList<>(childrenB);
            this.position = position;
            this.size = size;
            this.changeHandler = toggleHandler;
        }

        public void setState(boolean state) {
            if (state == this.state) return;
            this.state = state;
            changeHandler.accept(state);
        }

        @Override
        protected void draw(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().pushPose();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));
            render(context, p, triggerPressed);
            if (state) {
                children.forEach(w -> w.draw(context, p, triggerPressed));
            } else {
                childrenB.forEach(w -> w.draw(context, p, triggerPressed));
            }
            context.pose().popPose();
        }

        @Override
        protected void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);

            // Handle collision
            if (pointerPosition != null) {
                if (MathUtil.check2DPointCollision(pointerPosition, new Vector2f(), this.size)) {
                    if (triggerPressed) {
                        assert Minecraft.getInstance().player != null;
                        Minecraft.getInstance().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.2f, 1);
                        state = !state;
                        changeHandler.accept(state);
                    }
                }
            }
        }
    }


    protected static class HoverWidget extends Widget {
        protected ArrayList<Widget> childrenB;
        protected boolean hovered = false;

        /// childrenA are rendered when not hovered.
        /// childrenB are rendered when hovered
        protected HoverWidget(Vector3f position, Vector2f size, List<Widget> childrenA, List<Widget> childrenB) {
            this.position = position;
            this.size = size;
            this.children = new ArrayList<>(childrenA);
            this.childrenB = new ArrayList<>(childrenB);
        }

        @Override
        protected void draw(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().pushPose();
            var p = pointerPosition == null ? null : new Vector2f(pointerPosition).sub(new Vector2f(position.x, position.y));
            render(context, p, triggerPressed);
            if (hovered) {
                childrenB.forEach(w -> w.draw(context, p, triggerPressed));
            } else {
                children.forEach(w -> w.draw(context, p, triggerPressed));
            }
            context.pose().popPose();
        }

        @Override
        protected void render(GuiGraphics context, @Nullable Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);

            // Handle collision
            if (pointerPosition != null) {
                hovered = MathUtil.check2DPointCollision(pointerPosition, new Vector2f(), this.size);
            } else {
                hovered = false;
            }
        }
    }

    protected static class GradientWidget extends Widget {

        private final int col1;
        private final int col2;
        private final float angle;

        protected GradientWidget(Vector3f position, Vector2f size, int col1, int col2, float angle) {
            this.position = position;
            this.size = size;
            this.col1 = col1;
            this.col2 = col2;
            this.angle = angle;
        }

        @Override
        protected void render(GuiGraphics context, Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);

            context.fillGradient((int) -size.x/2, (int) -size.y/2, (int) size.x/2, (int) size.y/2, col1, col2);
        }
    }

    protected static class DynamicGradientWidget extends Widget {
        private final Callable<Integer> col1;
        private final Callable<Integer> col2;
        private final float angle;

        protected DynamicGradientWidget(Vector3f position, Vector2f size, Callable<Integer> col1, Callable<Integer> col2, float angle) {
            this.position = position;
            this.size = size;
            this.col1 = col1;
            this.col2 = col2;
            this.angle = angle;
        }

        @Override
        protected void render(GuiGraphics context, Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);
            try {
                context.fillGradient((int) -size.x / 2, (int) -size.y / 2, (int) size.x / 2, (int) size.y / 2, col1.call(), col2.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static class ContainerWidget extends Widget {

        protected ContainerWidget(Vector3f position, Vector2f size, Widget... children) {
            this.position = position;
            this.size = size;
            this.children = new ArrayList<>(Arrays.stream(children).toList());
        }

        @Override
        protected void render(GuiGraphics context, Vector2f pointerPosition, boolean triggerPressed) {
            context.pose().translate(-position.x, -position.y, -position.z);
        }
    }

    // End widget definitions ------------------------------------------------------

    public Pair<Vector3f, Vector2f> raycast(Vector3f position, Quaternionf orientation) {
        return MathUtil.raycastPlane(position, orientation, this.position, this.orientation, this.size.div(128, new Vector2f()));
    }

    public Vector3f getNormal() {
        return new Vector3f(0, 0, -1).rotate(orientation);
    }

    public void render(MultiBufferSource.BufferSource immediate, @Nullable Vector2f pointerPosition, boolean triggerPressed) {

        GuiGraphics context = new GuiGraphics(Minecraft.getInstance(), immediate);

        Vector3f camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        context.pose().translate(-camPos.x, -camPos.y, -camPos.z);
        context.pose().translate(position.x, position.y, position.z);

        context.pose().mulPose(orientation);
        context.pose().scale(1/128f, 1/128f, 1/128f);
        context.pose().pushPose();

        context.fill((int) (-size.x/2f), (int) (-size.y/2f), (int) (size.x/2f), (int) (size.y/2f), backgroundColor);

        widgets.forEach(w -> w.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed));

        context.flush();
        context.pose().popPose();
    }

}
