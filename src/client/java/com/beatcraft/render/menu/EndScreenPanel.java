package com.beatcraft.render.menu;

import com.beatcraft.menu.EndScreenData;
import com.beatcraft.render.HUDRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class EndScreenPanel extends MenuPanel<EndScreenData> {
    public EndScreenPanel(EndScreenData data) {
        super(data);
        position.set(0, 2f, 6);
        size.set(800, 500);

        setData(data);
    }

    public void setData(EndScreenData data) {
        this.data = data;

        widgets.clear();
        widgets.addAll(List.of(
                new TextWidget(String.valueOf(data.score), new Vector3f(0, -200, 0)),
                new TextWidget(String.valueOf(data.accuracy), new Vector3f(0, 0, 0)),
                new TextWidget("MAX COMBO", new Vector3f(-300, -100, 0), 0.35f),
                new TextWidget(String.valueOf(data.maxCombo), new Vector3f(-300, -50, 0)),
                new TextWidget(data.goodCuts + "/" + data.totalNotes, new Vector3f(-300, 0, 0)),
                new ButtonWidget(
                        new Vector3f(0, 200, 0), new Vector2f(300, 50),
                        () -> {
                            HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                        },
                        new TextWidget("CONTINUE", new Vector3f()
                        )
                )
        ));
    }
}
