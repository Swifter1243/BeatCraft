package com.beatcraft.render.menu;

import com.beatcraft.menu.CustomColorSelectorMenu;

public class CustomColorSelectorMenuPanel extends MenuPanel<CustomColorSelectorMenu> {
    public CustomColorSelectorMenuPanel() {
        super(new CustomColorSelectorMenu());

        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);

        initLayout();
    }

    public void initLayout() {



    }

}
