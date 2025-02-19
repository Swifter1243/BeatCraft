package com.beatcraft.render.menu;

import com.beatcraft.menu.SongSelectMenu;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public class SongSelectMenuPanel extends MenuPanel<SongSelectMenu> {

    public SongSelectMenuPanel(SongSelectMenu data) {
        super(data);
        position.set(0, 2f, 6);
        size.set(800, 500);
    }
}
