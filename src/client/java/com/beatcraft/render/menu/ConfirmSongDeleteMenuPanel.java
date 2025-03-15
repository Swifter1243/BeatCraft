package com.beatcraft.render.menu;

import com.beatcraft.menu.ConfirmSongDeleteMenu;
import com.beatcraft.render.HUDRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class ConfirmSongDeleteMenuPanel extends MenuPanel<ConfirmSongDeleteMenu> {
    public ConfirmSongDeleteMenuPanel(ConfirmSongDeleteMenu data) {
        super(data);
        backgroundColor = 0;
        position.set(0, 1, 6);
        size.set(800, 500);

        widgets.addAll(List.of(
            new TextWidget("Are you sure you want to delete this map?", new Vector3f(0, -260, 0)).withScale(5).withColor(0xFF9A2222),
            new TextWidget(data.songData.getTitle(), new Vector3f(0, -200, 0)).withScale(6),
            new TextWidget(data.songData.getAuthor(), new Vector3f(0, -130, 0)).withScale(4),
            new TextWidget("["+ String.join(", ", data.songData.getMappers()) + "]", new Vector3f(0, -80, 0)).withScale(2),
            new ButtonWidget(new Vector3f(-70, 0, -0.05f), new Vector2f(130, 50),
                () -> HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect,
                new HoverWidget(new Vector3f(), new Vector2f(130, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7F7f7f7f, 0x7F7f7f7f, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget("CANCEL", new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(new Vector3f(70, 0, -0.05f), new Vector2f(130, 50), data::deleteSong,
                new HoverWidget(new Vector3f(), new Vector2f(130, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FAf2f2f, 0x7FAf2f2f, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FF03030, 0x7FF03030, 0)
                )),
                new TextWidget("DELETE", new Vector3f(0, -11, 0.05f)).withScale(3)
            )
        ));

    }
}
