package com.beatcraft.screen;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.render.DebugRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

/*
Tabs: General | Quality | Audio | Controllers | Debug

General tab:
(Vivecraft) show hands [toggle]                          (should be tied to vivecraft's setting)
Saber Trail Intensity  (from Quality tab)
Volume                 (from Audio tab)

Quality Tab:
Saber Trail Intensity  [int from 10 to 300 in inc of 10] (also in general tab)
Smoke Graphics         [toggle]
Burn Mark Trails       [toggle]

Audio tab:
Volume                 [slider]                          (also in general tab AND minecraft's audio settings page)
Ambient Volume Scale   [slider from 0-100%]
Latency                [int >= 0] [toggle to enable]

Controllers tab:
Selected Profile       [int from 0-2]
    profile options:
    Position           [xyz sliders]
    Rotation           [xyz sliders]

Debug tab:
Debug Rendering        [toggle]
Saber Debug Renderers  [toggle]
Show Hitboxes          [toggle]

 */

public class SettingsScreen extends Screen {

    private int page = 0;
    private final Screen parent;
    private GameOptions options;

    public SettingsScreen(Screen parent) {
        super(Text.translatable("screen.beatcraft.settings"));
        this.parent = parent;
        options = new GameOptions(MinecraftClient.getInstance(), BeatCraftClient.playerConfig.configFolder);
    }


    private void onGeneralButtonPressed(ButtonWidget button) {
        page = 0;
        clearChildren();
        init();
    }

    private void onQualityButtonPressed(ButtonWidget button) {
        page = 1;
        clearChildren();
        init();
    }

    private void onAudioButtonPressed(ButtonWidget button) {
        page = 2;
        clearChildren();
        init();
    }

    private void onControllersButtonPressed(ButtonWidget button) {
        page = 3;
        clearChildren();
        init();
    }

    private void onDebugButtonPressed(ButtonWidget button) {
        page = 4;
        clearChildren();
        init();
    }

    @Override
    protected void init() {
        int left_width = 150;
        int right_width = 100;
        int common_width = 120;
        int button_height = 20;
        int left_column = (width / 2 + 50) - (left_width + 5);
        int left_column_alt = (width / 2 + 30) - (common_width + 5);
        int right_column = (width / 2 + 50) + 5;
        int right_column_alt = (width / 2 + 30) + 5;

        var done_button = ButtonWidget.builder(Text.translatable("gui.back"), this::goBack)
            .dimensions(width/2 - 50, height-25, 100, 20)
            .build();
        addDrawableChild(done_button);

        int menu_x = 5;
        int menu_y = 20;

        // Common buttons
        var general_button = ButtonWidget.builder(Text.translatable("gui.beatcraft.button.general_settings"), this::onGeneralButtonPressed)
            .dimensions(menu_x, menu_y, 100, button_height)
            .build();

        var quality_button = ButtonWidget.builder(Text.translatable("gui.beatcraft.button.quality_settings"), this::onQualityButtonPressed)
            .dimensions(menu_x, menu_y + 25, 100, button_height)
            .build();

        var audio_button = ButtonWidget.builder(Text.translatable("gui.beatcraft.button.audio_settings"), this::onAudioButtonPressed)
            .dimensions(menu_x, menu_y + 50, 100, button_height)
            .build();

        var controllers_button = ButtonWidget.builder(Text.translatable("gui.beatcraft.button.controllers_settings"), this::onControllersButtonPressed)
            .dimensions(menu_x, menu_y + 75, 100, button_height)
            .build();

        var debug_button = ButtonWidget.builder(Text.translatable("gui.beatcraft.button.debug_settings"), this::onDebugButtonPressed)
            .dimensions(menu_x, menu_y + 100, 100, button_height)
            .build();

        addDrawableChild(general_button);
        addDrawableChild(quality_button);
        addDrawableChild(audio_button);
        addDrawableChild(controllers_button);
        addDrawableChild(debug_button);

        if (page == 0) { // General
            var saberTrailIntensityLabel = new TextWidget(Text.translatable("setting.beatcraft.quality.trail_intensity"), this.textRenderer);
            saberTrailIntensityLabel.setDimensionsAndPosition(left_width, button_height, left_column, menu_y);

            var saberTrailSlider = new SliderWidget(right_column, menu_y, right_width, button_height, Text.literal("60"), (5f/29f)) {
                @Override
                protected void updateMessage() {
                    int val = (int) ((value * 29) + 1) * 10;
                    this.setMessage(Text.literal(Integer.toString(val)));
                }

                @Override
                protected void applyValue() {
                    int val = (int) ((value * 29) + 1) * 10;
                    updateTrailIntensity(val);
                }
            };

            addDrawableChild(saberTrailIntensityLabel);
            addDrawableChild(saberTrailSlider);

            var volumeSlider = new SliderWidget(left_column_alt, menu_y + 25, common_width, button_height, Text.translatable("setting.beatcraft.audio.volume", "100%"), 1.0) {

                @Override
                protected void updateMessage() {
                    int vol = (int) (value * 100);
                    setMessage(Text.translatable("setting.beatcraft.audio.volume", vol + "%"));
                }

                @Override
                protected void applyValue() {
                    updateVolume(value);
                }
            };

            addDrawableChild(volumeSlider);

        }
        else if (page == 1) { // Quality

            var saberTrailIntensityLabel = new TextWidget(Text.translatable("setting.beatcraft.quality.trail_intensity"), this.textRenderer);
            saberTrailIntensityLabel.setDimensionsAndPosition(left_width, button_height, left_column, menu_y);

            var saberTrailSlider = new SliderWidget(right_column, menu_y, right_width, button_height, Text.literal("60"), (5f/29f)) {
                @Override
                protected void updateMessage() {
                    int val = (int) ((value * 29) + 1) * 10;
                    this.setMessage(Text.literal(Integer.toString(val)));
                }

                @Override
                protected void applyValue() {
                    int val = (int) ((value * 29) + 1) * 10;
                    updateTrailIntensity(val);
                }
            };

            addDrawableChild(saberTrailIntensityLabel);
            addDrawableChild(saberTrailSlider);

            var smokeGraphicsToggle = ButtonWidget.builder(Text.translatable("setting.beatcraft.quality.smoke_graphics", BeatCraftClient.playerConfig.shouldRenderSmoke() ? "ON" : "OFF"), this::toggleSmokeGraphics)
                .dimensions(left_column_alt, menu_y + 25, common_width, button_height)
                .build();

            addDrawableChild(smokeGraphicsToggle);

            var burnMarkTrailToggle = ButtonWidget.builder(Text.translatable("setting.beatcraft.quality.burn_mark_trails", BeatCraftClient.playerConfig.shouldRenderBurnMarkTrails() ? "ON" : "OFF"), this::toggleBurnMarks)
                .dimensions(right_column_alt, menu_y + 25, common_width, button_height)
                .build();

            addDrawableChild(burnMarkTrailToggle);

        }
        else if (page == 2) { // Audio

            var volumeSlider = new SliderWidget(left_column_alt, menu_y, common_width, button_height, Text.translatable("setting.beatcraft.audio.volume", "100%"), 1.0) {

                @Override
                protected void updateMessage() {
                    int vol = (int) (value * 100);
                    setMessage(Text.translatable("setting.beatcraft.audio.volume", vol + "%"));
                }

                @Override
                protected void applyValue() {
                    updateVolume(value);
                }
            };

            addDrawableChild(volumeSlider);

        }
        else if (page == 3) { // Controllers

        }
        else if (page == 4) { // Debug
            var mainRenderToggle = ButtonWidget.builder(Text.translatable("setting.beatcraft.debug.main_renderer", DebugRenderer.doDebugRendering ? "ON" : "OFF"), this::toggleDebugRendering)
                .dimensions(width / 2 - (common_width/2) + 30, menu_y, common_width, button_height)
                .build();

            addDrawableChild(mainRenderToggle);

            var saberRenderToggle = ButtonWidget.builder(Text.translatable("setting.beatcraft.debug.saber_renderer", DebugRenderer.debugSaberRendering ? "ON" : "OFF"), this::toggleDebugSaberRendering)
                .dimensions(left_column_alt, menu_y + 25, common_width, button_height)
                .tooltip(Tooltip.of(Text.translatable("setting.beatcraft.debug.saber_renderer", DebugRenderer.debugSaberRendering ? "ON" : "OFF")))
                .build();

            addDrawableChild(saberRenderToggle);

            var hitboxToggle = ButtonWidget.builder(Text.translatable("setting.beatcraft.debug.hitboxes", DebugRenderer.renderHitboxes ? "ON" : "OFF"), this::toggleHitboxes)
                .dimensions(right_column_alt, menu_y + 25, common_width, button_height)
                .build();

            addDrawableChild(hitboxToggle);

        }

    }

    public void goBack(ButtonWidget button) {
        close();
    }

    public void updateTrailIntensity(int value) {

    }

    public void updateVolume(double volume) {

    }

    public void toggleSmokeGraphics(ButtonWidget button) {
        boolean smoke = BeatCraftClient.playerConfig.shouldRenderSmoke();
        BeatCraftClient.playerConfig.setSmokeRendering(!smoke);
        button.setMessage(Text.translatable("setting.beatcraft.quality.smoke_graphics", smoke ? "OFF" : "ON"));
    }

    public void toggleBurnMarks(ButtonWidget button) {
        boolean marks = BeatCraftClient.playerConfig.shouldRenderBurnMarkTrails();
        BeatCraftClient.playerConfig.setBurnMarkRendering(!marks);
        button.setMessage(Text.translatable("setting.beatcraft.quality.burn_mark_trails", marks ? "OFF" : "ON"));

    }


    public void toggleDebugRendering(ButtonWidget button) {
        DebugRenderer.doDebugRendering = !DebugRenderer.doDebugRendering;
        button.setMessage(Text.translatable("setting.beatcraft.debug.main_renderer", DebugRenderer.doDebugRendering ? "ON" : "OFF"));
    }

    public void toggleDebugSaberRendering(ButtonWidget button) {
        DebugRenderer.debugSaberRendering = !DebugRenderer.debugSaberRendering;
        button.setMessage(Text.translatable("setting.beatcraft.debug.saber_renderer", DebugRenderer.debugSaberRendering ? "ON" : "OFF"));
        button.setTooltip(Tooltip.of(Text.translatable("setting.beatcraft.debug.saber_renderer", DebugRenderer.debugSaberRendering ? "ON" : "OFF")));
    }

    public void toggleHitboxes(ButtonWidget button) {
        DebugRenderer.renderHitboxes = !DebugRenderer.renderHitboxes;
        button.setMessage(Text.translatable("setting.beatcraft.debug.hitboxes", DebugRenderer.renderHitboxes ? "ON" : "OFF"));
    }


    @Override
    public void close() {
        client.setScreen(parent);
    }
}
