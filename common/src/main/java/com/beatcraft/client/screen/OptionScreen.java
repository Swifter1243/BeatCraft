package com.beatcraft.client.screen;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OptionScreen extends Screen {

    private final Screen parent;
    private final OptionList options;

    private final Button backButton;
    private final Button nextButton;
    private final Button saveButton;
    private final Button closeButton;
    private boolean saved = true;

    protected OptionScreen(Screen parent, Component component, OptionList options) {
        super(component);
        this.parent = parent;
        this.options = options;

        backButton = Button
            .builder(Component.translatable("gui.beatcraft.back"), this::buttonBack)
            .pos(-215, 400)
            .bounds(0,0, 150, 40)
            .createNarration((x) -> Component.translatable("gui.beatcraft.back"))
            .build();

        nextButton = Button
            .builder(Component.translatable("gui.beatcraft.next"), this::buttonNext)
            .pos(165, 400)
            .bounds(0,0, 150, 40)
            .createNarration((x) -> Component.translatable("gui.beatcraft.next"))
            .build();

        saveButton = Button
            .builder(Component.translatable("gui.beatcraft.save"), this::buttonSave)
            .pos(-155, 400)
            .bounds(0, 0, 150, 40)
            .createNarration((x) -> Component.translatable("gui.beatcraft.save"))
            .build();

        closeButton = Button
            .builder(Component.translatable("screen.beatcraft.close"), this::buttonClose)
            .pos(5, 400)
            .bounds(0, 0, 150, 40)
            .createNarration((x) -> Component.translatable("screen.beatcraft.close"))
            .build();


        addRenderableWidget(backButton);
        addRenderableWidget(nextButton);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (options.page == 0) {
            backButton.active = false;
        } else {
            backButton.active = true;
        }
        if (options.page == options.pageCount-1) {
            nextButton.active = false;
        } else {
            nextButton.active = true;
        }
    }

    private void buttonBack(Button b) {

    }

    private void buttonNext(Button b) {

    }

    private void buttonSave(Button b) {
        b.active = false;
        closeButton.active = true;
        BeatcraftClient.playerConfig.writeToFile();
    }

    private void buttonClose(Button b) {
        onClose();
    }

    private void buttonReset(Button b) {

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return saved;
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
