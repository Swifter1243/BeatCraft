package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.menu.ConfirmSongDeleteMenu;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.replay.ReplayHandler;
import com.beatcraft.replay.ReplayInfo;
import net.minecraft.text.Text;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfirmSongDeleteMenuPanel extends MenuPanel<ConfirmSongDeleteMenu> {

    private static final Text CONFIRM = Text.translatable("menu.beatcraft.delete.confirm");
    private static final Text CONFIRM_REPLAY = Text.translatable("menu.beatcraft.delete.confirm_replay");
    private static final Text DELETE = Text.translatable("menu.beatcraft.song_select.delete");
    private static final Text CANCEL = Text.translatable("menu.beatcraft.delete.cancel");


    public ConfirmSongDeleteMenuPanel(ConfirmSongDeleteMenu data) {
        super(data);
        backgroundColor = 0;
        position.set(0, 1, 6);
        size.set(800, 500);

        widgets.addAll(List.of(
            new TextWidget(CONFIRM, new Vector3f(0, -260, 0)).withScale(5).withColor(0xFF9A2222),
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
                new TextWidget(CANCEL, new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(new Vector3f(70, 0, -0.05f), new Vector2f(130, 50), data::deleteSong,
                new HoverWidget(new Vector3f(), new Vector2f(130, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FAf2f2f, 0x7FAf2f2f, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FF03030, 0x7FF03030, 0)
                )),
                new TextWidget(DELETE, new Vector3f(0, -11, 0.05f)).withScale(3)
            )
        ));

    }

    public ConfirmSongDeleteMenuPanel(ReplayInfo info) {
        super(null); // I could make a unique screen but why do that when one already exists?

        backgroundColor = 0;
        position.set(0, 1, 6);
        size.set(800, 500);

        widgets.addAll(List.of(
            new TextWidget(CONFIRM_REPLAY, new Vector3f(0, -260, 0)).withScale(5).withColor(0xFF9A2222),
            new TextWidget(String.format("%s (%s)", info.name(), info.mapID()), new Vector3f(0, -200, 0)).withScale(6),
            new TextWidget(String.format("%s - %s", info.set(), info.diff()), new Vector3f(0, -130, 0)).withScale(4),
            new ButtonWidget(new Vector3f(-70, 0, -0.05f), new Vector2f(130, 50),
                () -> HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect,
                new HoverWidget(new Vector3f(), new Vector2f(130, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7F7f7f7f, 0x7F7f7f7f, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FA0A0A0, 0x7FA0A0A0, 0)
                )),
                new TextWidget(CANCEL, new Vector3f(0, -11, 0.05f)).withScale(3)
            ),
            new ButtonWidget(new Vector3f(70, 0, -0.05f), new Vector2f(130, 50), () -> {
                ReplayHandler.delete(info);
                HUDRenderer.modifierMenuPanel.refreshReplays = true;
                HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
            },
                new HoverWidget(new Vector3f(), new Vector2f(130, 50), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FAf2f2f, 0x7FAf2f2f, 0)
                ), List.of(
                    new GradientWidget(new Vector3f(), new Vector2f(130, 50), 0x7FF03030, 0x7FF03030, 0)
                )),
                new TextWidget(DELETE, new Vector3f(0, -11, 0.05f)).withScale(3)
            )
        ));

    }

}
