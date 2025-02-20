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
        backgroundColor = 0;
        setData(data);
    }

    public void setData(EndScreenData data) {
        this.data = data;

        widgets.clear();
        widgets.addAll(List.of(
                new TextWidget("LEVEL CLEARED", new Vector3f(0, -80, 0), 5),
                new TextWidget("GOOD CUTS", new Vector3f(-280, 30, -0.01f), 2),
                new TextWidget("SCORE", new Vector3f(0, 30, -0.01f), 2),
                new TextWidget("RANK", new Vector3f(280, 30, -0.01f), 2),
                new TextWidget(data.goodCuts + "/" + data.totalNotes, new Vector3f(-280, 50, -0.01f), 5),
                new TextWidget(String.valueOf(data.score), new Vector3f(0, 50, -0.01f), 5),
                new TextWidget(String.valueOf(data.rank), new Vector3f(280, 50, -0.01f), 5),
                new TextWidget("MAX COMBO " + data.maxCombo, new Vector3f(-280, 90, -0.01f), 1.5f),
                new ButtonWidget(
                        new Vector3f(0, 180, -0.01f), new Vector2f(100, 20),
                        () -> {
                            HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
                        },
                        new TextWidget("CONTINUE", new Vector3f(), 5)
                )
        ));
    }
}
