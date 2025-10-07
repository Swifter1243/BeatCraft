package com.beatcraft.client.render.menu;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.menu.CreditsMenu;
import com.beatcraft.client.render.HUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class CreditsPanel extends MenuPanel<CreditsMenu> {
    public CreditsPanel(HUDRenderer hudRenderer) {
        super(new CreditsMenu(hudRenderer));
        float angle = -60 * Mth.DEG_TO_RAD;
        position.set(0, 1, 6.4f);
        position.rotateY(angle);
        orientation.set(new Quaternionf().rotateY(angle));
        size.set(800, 500);
        backgroundColor = 0;

        initLayout();
    }

    private static final Component CREDITS = Component.translatable("credits.beatcraft.credits");
    private static final Component DEVS = Component.translatable("credits.beatcraft.developers");
    private static final Component DEV = Component.translatable("credits.beatcraft.developer");
    private static final Component CONTRIBUTORS = Component.translatable("credits.beatcraft.contributors");
    private static final Component DISCORD = Component.translatable("credits.beatcraft.discord");

    private void initLayout() {
        widgets.clear();

        widgets.addAll(List.of(
            new TextWidget(CREDITS, new Vector3f(0, -250, 0), 3),

            new TextWidget(DEVS, new Vector3f(0, -200, 0), 3),
            getDeveloperWidget("textures/credits_menu/westbot.png", "Westbot", DEV, "https://ko-fi.com/westbot", new Vector3f(0, -115, 0)),
            getDeveloperWidget("textures/credits_menu/swifter.png", "Swifter", DEV, "https://ko-fi.com/swifter", new Vector3f(0, -25, 0)),

            SettingsMenuPanel.getButton(
                new TextWidget(CONTRIBUTORS, new Vector3f(0, -11, 0.05f), 2),
                () -> {},//Minecraft.getInstance().setScreen(new ContributorsScreen()),
                new Vector3f(-120, 60, 0),
                new Vector2f(230, 50)
            ),

            SettingsMenuPanel.getButton(
                new TextWidget(DISCORD, new Vector3f(0, -11, 0.05f), 2),
                () -> ConfirmLinkScreen.confirmLink(null, "https://discord.gg/eQH4pbHptM"),
                new Vector3f(120, 60, 0),
                new Vector2f(230, 50)
            )

        ));

    }

    private static final int ICON_OFFSET = -220;
    private static final int TEXT_OFFSET = ICON_OFFSET + 55;
    private static final int KOFI_BUTTON_OFFSET = 210;
    private static final int NAME_POS = -25;
    private static final int ROLE_POS = 10;


    private Widget getDeveloperWidget(String iconName, String name, Component role, String kofi, Vector3f position) {

        return new ContainerWidget(position, new Vector2f(),
            new TextureWidget(Beatcraft.id(iconName), new Vector3f(ICON_OFFSET, 0, 0), new Vector2f(80, 80)),
            new TextWidget(name, new Vector3f(TEXT_OFFSET, NAME_POS, -0.01f), 4).alignedLeft(),
            new TextWidget(role, new Vector3f(TEXT_OFFSET, ROLE_POS, -0.01f), 2).alignedLeft(),
            SettingsMenuPanel.getButton(
                new TextureWidget(Beatcraft.id("textures/credits_menu/minecraft_kofi_logo.png"), new Vector3f(0, 0, 0.05f), new Vector2f(984, 269)).withScale(0.1f),
                () -> ConfirmLinkScreen.confirmLink(null, kofi),
                new Vector3f(KOFI_BUTTON_OFFSET, 0, 0), new Vector2f(120, 60)
            )
        );

    }


}
