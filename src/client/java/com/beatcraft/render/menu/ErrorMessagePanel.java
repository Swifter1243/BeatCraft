package com.beatcraft.render.menu;

import com.beatcraft.menu.ErrorMessageMenu;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class ErrorMessagePanel extends MenuPanel<ErrorMessageMenu> {
    public ErrorMessagePanel(ErrorMessageMenu data) {
        super(data);

        position.set(0, 0, 5f);
        orientation.rotationX(80 * MathHelper.RADIANS_PER_DEGREE);
        size.set(800, 500);
        backgroundColor = 0;

        initLayout();
    }

    public void setContent(String content) {
        data.setContent(content);
        refresh = true;
    }

    public static boolean refresh = false;

    public boolean shouldDisplay() {
        return data.shouldDisplay();
    }

    public void close() {
        data.close();
    }

    @Override
    public void render(VertexConsumerProvider.Immediate immediate, Vector2f pointerPosition) {
        super.render(immediate, pointerPosition);
        if (refresh) {
            refresh = false;
            initLayout();
        }
    }

    private static final int ERR_COLOR = 0xFF8F2000;
    private static final Text CLOSE = Text.translatable("menu.beatcraft.error_panel.close");
    private void initLayout() {
        widgets.clear();
        if (data.shouldDisplay()) {
            widgets.addAll(List.of(
                SettingsMenuPanel.getButton(
                    new TextWidget(CLOSE, new Vector3f(0, -11, 0), 3),
                    () -> {
                        data.close();
                        refresh = true;
                    },
                    new Vector3f(0, 200, 0), new Vector2f(150, 50)
                ),
                new TextWidget(() -> data.getLine(0), new Vector3f(0, -60, 0), 4).withColor(ERR_COLOR),
                new TextWidget(() -> data.getLine(1), new Vector3f(0, -30, 0), 4).withColor(ERR_COLOR),
                new TextWidget(() -> data.getLine(2), new Vector3f(), 4).withColor(ERR_COLOR),
                new TextWidget(() -> data.getLine(3), new Vector3f(0, 30, 0), 4).withColor(ERR_COLOR),
                new TextWidget(() -> data.getLine(4), new Vector3f(0, 60, 0), 4).withColor(ERR_COLOR)
            ));

        }
    }
}
