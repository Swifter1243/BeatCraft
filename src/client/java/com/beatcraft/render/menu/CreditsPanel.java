package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.menu.CreditsMenu;
import com.beatcraft.screen.ContributorsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class CreditsPanel extends MenuPanel<CreditsMenu> {
    public CreditsPanel() {
        super(new CreditsMenu());
        float angle = -60 * MathHelper.RADIANS_PER_DEGREE;
        position.set(0, 1, 6.4f);
        position.rotateY(angle);
        orientation.set(new Quaternionf().rotateY(angle));
        size.set(800, 500);
        backgroundColor = 0;

        initLayout();
    }


    private void initLayout() {

        widgets.addAll(List.of(
            new TextWidget("CREDITS", new Vector3f(0, -250, 0), 3),

            new TextWidget("Developers", new Vector3f(0, -200, 0), 3),
            getDeveloperWidget("textures/credits_menu/westbot.png", "Westbot", "Developer", "https://ko-fi.com/westbot", new Vector3f(0, -115, 0)),
            getDeveloperWidget("textures/credits_menu/swifter.png", "Swifter", "Developer", "https://ko-fi.com/swifter", new Vector3f(0, -25, 0)),

            SettingsMenuPanel.getButton(
                new TextWidget("All Contributors", new Vector3f(0, -11, 0.05f), 2),
                () -> MinecraftClient.getInstance().setScreen(new ContributorsScreen()),
                new Vector3f(0, 70, 0),
                new Vector2f(230, 50)
            )
        ));

    }

    private static final int ICON_OFFSET = -220;
    private static final int TEXT_OFFSET = ICON_OFFSET + 55;
    private static final int KOFI_BUTTON_OFFSET = 210;
    private static final int NAME_POS = -25;
    private static final int ROLE_POS = 10;


    private Widget getDeveloperWidget(String iconName, String name, String role, String kofi, Vector3f position) {

        return new ContainerWidget(position, new Vector2f(),
            new TextureWidget(BeatCraft.id(iconName), new Vector3f(ICON_OFFSET, 0, 0), new Vector2f(80, 80)),
            new TextWidget(name, new Vector3f(TEXT_OFFSET, NAME_POS, -0.01f), 4).alignedLeft(),
            new TextWidget(role, new Vector3f(TEXT_OFFSET, ROLE_POS, -0.01f), 2).alignedLeft(),
            SettingsMenuPanel.getButton(
                new TextureWidget(BeatCraft.id("textures/credits_menu/kofi_logo.png"), new Vector3f(0, 0, 0.05f), new Vector2f(984, 269)).withScale(0.1f),
                () -> {
                    ConfirmLinkScreen.open(null, kofi);
                },
                new Vector3f(KOFI_BUTTON_OFFSET, 0, 0), new Vector2f(120, 60)
            )
        );

    }


}
