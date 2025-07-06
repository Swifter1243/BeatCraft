package com.beatcraft.render.menu;

import com.beatcraft.menu.KeyboardMenu;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

public class KeyboardPanel extends MenuPanel<KeyboardMenu> {
    public KeyboardPanel(KeyboardMenu data) {
        super(data);

        position.set(0.25f, -0.25f, 4.5f);
        orientation.rotationX(60 * MathHelper.RADIANS_PER_DEGREE).normalize();
        size.set(900, 300);
        backgroundColor = 0;

        initLayout();
    }

    private static final float ROW_CHARS = 13;
    private static final float CHAR_WIDTH = 45;
    private static final float WIDTH = ROW_CHARS * CHAR_WIDTH + ((ROW_CHARS - 1) * 5);
    private static final float START = -(WIDTH/2f);

    private float keyX(int index) {
        return START + (index * CHAR_WIDTH) + ((index-1) * 5);
    }

    private void initLayout() {
        widgets.clear();


        widgets.addAll(List.of(
            getKey("`", "~", 0, keyX(0)),
            getKey("1", "!", 0, keyX(1)),
            getKey("2", "@", 0, keyX(2)),
            getKey("3", "#", 0, keyX(3)),
            getKey("4", "$", 0, keyX(4)),
            getKey("5", "%", 0, keyX(5)),
            getKey("6", "^", 0, keyX(6)),
            getKey("7", "&", 0, keyX(7)),
            getKey("8", "*", 0, keyX(8)),
            getKey("9", "(", 0, keyX(9)),
            getKey("0", ")", 0, keyX(10)),
            getKey("-", "_", 0, keyX(11)),
            getKey("=", "+", 0, keyX(12)),

            getKey("delete", "delete", 0, keyX(13), 95),

            getKey("q", 1, keyX(0)),
            getKey("w", 1, keyX(1)),
            getKey("e", 1, keyX(2)),
            getKey("r", 1, keyX(3)),
            getKey("t", 1, keyX(4)),
            getKey("y", 1, keyX(5)),
            getKey("u", 1, keyX(6)),
            getKey("i", 1, keyX(7)),
            getKey("o", 1, keyX(8)),
            getKey("p", 1, keyX(9)),
            getKey("[", "{", 1, keyX(10)),
            getKey("]", "}", 1, keyX(11)),
            getKey("\\", "|", 1, keyX(12)),

            getKey("a", 2, keyX(0)),
            getKey("s", 2, keyX(1)),
            getKey("d", 2, keyX(2)),
            getKey("f", 2, keyX(3)),
            getKey("g", 2, keyX(4)),
            getKey("h", 2, keyX(5)),
            getKey("j", 2, keyX(6)),
            getKey("k", 2, keyX(7)),
            getKey("l", 2, keyX(8)),
            getKey(";", ":", 2, keyX(9)),
            getKey("'", "\"", 2, keyX(10)),

            getKey("shift", "shift", 3, keyX(0), 95),
            getKey("z", "Z", 3, keyX(2)),
            getKey("x", "X", 3, keyX(3)),
            getKey("c", "C", 3, keyX(4)),
            getKey("v", "V", 3, keyX(5)),
            getKey("b", "B", 3, keyX(6)),
            getKey("n", "N", 3, keyX(7)),
            getKey("m", "M", 3, keyX(8)),
            getKey(",", "<", 3, keyX(9)),
            getKey(".", ">", 3, keyX(10)),
            getKey("/", "?", 3, keyX(11)),

            getKey(" ", " ", 4, keyX(4), (6 * 45) + 25)
        ));

    }

    private ButtonWidget getKey(String key, String upper, int row, float x) {
        return getKey(key, upper, row, x, 45);
    }

    private ButtonWidget getKey(String key, int row, float x) {
        return getKey(key, key.toUpperCase(), row, x, 45);
    }

    private ButtonWidget getKey(String key, String upper, int row, float x, float width) {

        Widget displayWidget;
        Runnable action;
        if (key.equals("delete") || key.equals(" ") || key.equals("shift")) {
            displayWidget = new TextWidget(key.equals("delete") ? "←--" : key.equals(" ") ? "└───┘" : key, new Vector3f(0, -11, 0.01f), 2);
        } else {
            displayWidget = new ContainerWidget(
                new Vector3f(0, 0, 0.01f),
                new Vector2f(width, 45),
                new TextWidget(() -> data.shift ? upper : key, new Vector3f(0, -11, 0), 2.5f),
                new TextWidget(() -> data.shift ? key : upper, new Vector3f(15, 2, 0), 1)
            );
        }

        if (key.equals("shift")) {
            action = () -> {
                data.shift = !data.shift;
            };
        } else {
            action = () -> {
                data.input.processInput(data.shift ? upper : key);
            };
        }

        return SettingsMenuPanel.getButton(
            displayWidget,
            action,
            new Vector3f(x + (width/2f), -size.y/2f + 25 + (row * 50), 0),
            new Vector2f(width, 45)
        );

    }

}
