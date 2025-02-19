package com.beatcraft.render.menu;

import com.beatcraft.menu.ModifierMenu;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public class ModifierMenuPanel extends MenuPanel<ModifierMenu> {

    public ModifierMenuPanel(ModifierMenu data) {
        super(data);
        float angle = 60 * MathHelper.RADIANS_PER_DEGREE;
        position.set(0, 2, 6);
        position.rotateY(angle);
        orientation.set(new Quaternionf().rotateY(angle));
        size.set(800, 500);
    }
}
