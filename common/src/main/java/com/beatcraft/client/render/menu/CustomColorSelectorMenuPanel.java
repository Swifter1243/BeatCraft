package com.beatcraft.client.render.menu;

import com.beatcraft.client.menu.CustomColorSelectorMenu;
import com.beatcraft.client.render.HUDRenderer;

public class CustomColorSelectorMenuPanel extends MenuPanel<CustomColorSelectorMenu> {
    public CustomColorSelectorMenuPanel(HUDRenderer hudRenderer) {
        super(new CustomColorSelectorMenu(hudRenderer));

        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);

        initLayout();
    }

    public void initLayout() {



    }

}
