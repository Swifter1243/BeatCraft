package com.beatcraft.client.render.menu;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.audio.AudioController;
import com.beatcraft.client.logic.InputSystem;
import com.beatcraft.client.menu.ModifierMenu;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.client.replay.ReplayHandler;
import com.beatcraft.client.replay.ReplayInfo;
import com.beatcraft.common.data.map.SongDownloader;
import com.beatcraft.common.data.types.CycleStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ModifierMenuPanel extends MenuPanel<ModifierMenu> {

    private enum SongSelectPage {
        Modifiers,
        PlayerOptions,
        Settings,
        Downloader,
        Replay,
        Sabers
    }

    private SongSelectPage currentPage = SongSelectPage.Modifiers;

    private final ContainerWidget modifierPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget playerOptionsPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget settingsPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget downloaderPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget replayPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget replayPageStatic = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());
    private final ContainerWidget customSaberPage = new ContainerWidget(new Vector3f(0, 0, -0.01f), new Vector2f());

    public ModifierMenuPanel(ModifierMenu data) {
        super(data);
        float angle = 60 * Mth.DEG_TO_RAD;
        position.set(0, 2, 6.4f);
        position.rotateY(angle);
        orientation.set(new Quaternionf().rotateY(angle));
        size.set(800, 600);
        backgroundColor = 0;

        initLayout();

        toggleModifiers(this.data.hudRenderer.controller.activeModifiers);
    }

    private void initLayout() {
        widgets.clear();
        modifierPage.children.clear();
        playerOptionsPage.children.clear();
        settingsPage.children.clear();
        downloaderPage.children.clear();
        customSaberPage.children.clear();

        // Top buttons: Modifiers | Player Options | Settings | BeatSaver | Replay
        int BUTTON_COUNT = 3;
        widgets.addAll(List.of(
            getOptionButton("Modifiers", 0, 0, BUTTON_COUNT, this::setModifierPage, SongSelectPage.Modifiers),
            getOptionButton("Player Options", 1, 0, BUTTON_COUNT, this::setPlayerOptionsPage, SongSelectPage.PlayerOptions),
            getOptionButton("Settings", 2, 0, BUTTON_COUNT, this::setSettingsPage, SongSelectPage.Settings),
            getOptionButton("BeatSaver", 0, 1, BUTTON_COUNT, this::setDownloaderPage, SongSelectPage.Downloader),
            getOptionButton("Replay", 1, 1, BUTTON_COUNT, this::setReplayPage, SongSelectPage.Replay),
            getOptionButton("Custom Sabers", 2, 1, BUTTON_COUNT, this::setSabersPage, SongSelectPage.Sabers)
        ));

        modifierPage.children.addAll(List.of(
                getModifierToggle("No Fail", 0, 0, this::toggleNoFail, "1 Life", "4 Lives", "Zen Mode"),
                getModifierToggle("1 Life", 0, 1, this::toggle1Life, "No Fail", "4 Lives", "Zen Mode"),
                getModifierToggle("4 Lives", 0, 2, this::toggle4Lives, "No Fail", "1 Life", "Zen Mode"),

                getModifierToggle("No Bombs", 1, 0, this::toggleNoBombs, "Zen Mode"),
                getModifierToggle("No Walls", 1, 1, this::toggleNoObstacles, "Zen Mode"),
        //        getModifierToggle("No Arrows", 1, 2, this::toggleNoArrows, "Zen Mode"),

                getModifierToggle("Ghost Notes", 2, 0, this::toggleGhostNotes, "Disappearing Arrows", "Zen Mode"),
                getModifierToggle("Disappearing Arrows", 2, 1, this::toggleDisappearingArrows, "Ghost Notes", "Zen Mode")
        //        getModifierToggle("Small Notes", 2, 2, this::toggleSmallNotes, "Zen Mode")
        ));
        modifierPage.children.addAll(List.of(
                //getModifierToggle("Pro Mode", 3, 0, this::toggleProMode, "Zen Mode"),
                //getModifierToggle("Strict Angles", 3, 1, this::toggleStrictAngles, "Zen Mode"),
                getModifierToggle("Zen Mode", 3, 2, this::toggleZenMode, "No Fail", "1 Life", "4 Lives", "No Bombs", "No Walls", "No Arrows", "Ghost Notes", "Disappearing Arrows", "Small Notes", "Pro Mode", "Strict Angles"),

                getModifierToggle("Slower Song", 4, 0, this::toggleSlowerSong, "Faster Song", "Super Fast Song"),
                getModifierToggle("Faster Song", 4, 1, this::toggleFasterSong, "Slower Song", "Super Fast Song"),
                getModifierToggle("Super Fast Song", 4, 2, this::toggleSuperFastSong, "Slower Song", "Faster Song")
        ));

        // Player options:
        // debris on/off
        // particles on/off
        // trail intensity
        // vivecraft: show player arms
        // show minecraft hud (F1)
        // cut sound volume
        // custom jump distance maybe?
        // player height?
        // dynamic volume adjustment (idk how to even implement this as a feature tbh)
        playerOptionsPage.children.addAll(List.of(
            SettingsMenuPanel.getOptionModifier("Reduced Debris",
                () -> BeatcraftClient.playerConfig.preferences.reducedDebris(false),
                () -> BeatcraftClient.playerConfig.preferences.reducedDebris(true),
                () -> BeatcraftClient.playerConfig.preferences.reducedDebris() ? "ON" : "OFF",
                new Vector3f(-100, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Particles",
                () -> BeatcraftClient.playerConfig.quality.sparkParticles(false),
                () -> BeatcraftClient.playerConfig.quality.sparkParticles(true),
                () -> BeatcraftClient.playerConfig.quality.sparkParticles() ? "ON" : "OFF",
                new Vector3f(-100, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Trail Intensity",
                () -> BeatcraftClient.playerConfig.preferences.trailIntensity(Math.max(3, CycleStack.getTrailSize()-1)),
                () -> BeatcraftClient.playerConfig.preferences.trailIntensity(Math.min(200, CycleStack.getTrailSize()+1)),
                () -> {
                    var x = BeatcraftClient.playerConfig.preferences.trailIntensity();
                    return x <= 3 ? "OFF" : String.valueOf(x);
                },
                new Vector3f(-100, -71, 0)),

            SettingsMenuPanel.getOptionModifier("Show Arms",
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands = false,
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands = true,
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands ? "SHOW" : "HIDE",
                new Vector3f(-100, -19, 0)),

            SettingsMenuPanel.getOptionModifier("Show Hotbar",
                () -> Minecraft.getInstance().options.hideGui = true,
                () -> Minecraft.getInstance().options.hideGui = false,
                () -> Minecraft.getInstance().options.hideGui ? "HIDE" : "SHOW",
                new Vector3f(-100, 32, 0)),

            SettingsMenuPanel.getOptionModifier("Movement Lock",
                InputSystem::unlockMovement,
                InputSystem::lockMovement,
                () -> InputSystem.isMovementLocked() ? "ON" : "OFF",
                new Vector3f(-100, 84, 0)),

            SettingsMenuPanel.getOptionModifier("Show HUD",
                () -> data.hudRenderer.showHUD = false,
                () -> data.hudRenderer.showHUD = true,
                () -> data.hudRenderer.showHUD ? "SHOW" : "HIDE",
                new Vector3f(230, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Health Style",
                () -> BeatcraftClient.playerConfig.preferences.healthStyle(BeatcraftClient.playerConfig.preferences.healthStyle().ordinal()-1),
                () -> BeatcraftClient.playerConfig.preferences.healthStyle(BeatcraftClient.playerConfig.preferences.healthStyle().ordinal()+1),
                () -> BeatcraftClient.playerConfig.preferences.healthStyle().name(),
                new Vector3f(230, -123, 0))
        ));

        settingsPage.children.addAll(List.of(
            SettingsMenuPanel.getOptionModifier("Volume",
                () -> updateVolume((int) (BeatcraftClient.playerConfig.audio.volume()*100)-5),
                () -> updateVolume((int) (BeatcraftClient.playerConfig.audio.volume()*100)+5),
                this::getVolume,
                new Vector3f(-100, -175, 0)),

            // SettingsMenuPanel.getOptionModifier("Place Environments",
            //     () -> BeatcraftClient.playerConfig.setEnvironmentPlacing(false),
            //     () -> BeatcraftClient.playerConfig.setEnvironmentPlacing(true),
            //     () -> BeatcraftClient.playerConfig.doEnvironmentPlacing() ? "ON" : "OFF",
            //     new Vector3f(-100, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Bloomfog",
                () -> BeatcraftClient.playerConfig.quality.doBloomfog(false),
                () -> BeatcraftClient.playerConfig.quality.doBloomfog(true),
                () -> BeatcraftClient.playerConfig.quality.doBloomfog() ? "ON" : "OFF",
                new Vector3f(230, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Bloom",
                () -> BeatcraftClient.playerConfig.quality.doBloom(false),
                () -> BeatcraftClient.playerConfig.quality.doBloom(true),
                () -> BeatcraftClient.playerConfig.quality.doBloom() ? "ON" : "OFF",
                new Vector3f(230, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Mirror",
                () -> BeatcraftClient.playerConfig.quality.doMirror(false),
                () -> BeatcraftClient.playerConfig.quality.doMirror(true),
                () -> BeatcraftClient.playerConfig.quality.doMirror() ? "ON" : "OFF",
                new Vector3f(230, -71, 0)),

            SettingsMenuPanel.getOptionModifier("Sky Fog",
                () -> BeatcraftClient.playerConfig.quality.skyFog(false),
                () -> BeatcraftClient.playerConfig.quality.skyFog(true),
                () -> BeatcraftClient.playerConfig.quality.skyFog() ? "ON" : "OFF",
                new Vector3f(230, -19, 0))

        ));


        var q = SongDownloader.queryBuilder;
        downloaderPage.children.addAll(List.of(
            new TextWidget("Filters (WIP)", new Vector3f(0, -200, 0.01f), 2.5f),

            get3StateBool("Ascending Order", () -> q.ascending, v -> q.ascending = v, new Vector3f(-200, -150, 0), new Vector2f(250, 45)),
            get3StateBool("Mod: Chroma", () -> q.chroma, v -> q.chroma = v, new Vector3f(-200, -100, 0), new Vector2f(250, 45)),
            get3StateBool("Mod: Noodle", () -> q.noodle, v -> q.noodle = v, new Vector3f(-200, -50, 0), new Vector2f(250, 45)),
            get3StateBool("Mod: Vivify*", () -> q.vivify, v -> q.vivify = v, new Vector3f(-200, 0, 0), new Vector2f(250, 45)),
            get3StateBool("Curated", () -> q.curated, v -> q.curated = v, new Vector3f(-200, 50, 0), new Vector2f(250, 45)),
            get3StateBool("Verified", () -> q.verified, v -> q.verified = v, new Vector3f(-200, 100, 0), new Vector2f(250, 45)),

            new TextWidget("*Vivify maps are not fully supported", new Vector3f(0, 140, 0.01f), 1.5f)
        ));

        customSaberPage.children.add(new TextWidget("WIP. for now use /custom_sabers", new Vector3f(0, -11, 0.01f), 4));

        setupReplayPageStatic();
        //replayPage.children.add(new TextWidget("COMING SOON", new Vector3f(0, -11, -0.01f), 3));

    }

    private Widget get3StateBool(String label, Callable<Boolean> getter, Consumer<Boolean> setter, Vector3f position, Vector2f size) {
        return SettingsMenuPanel.getButton(
            new TextWidget(() -> {
                var v = getter.call();
                return label + " : " + (v == null ? "--" : v ? "True" : "False");
            }, new Vector3f(-(size.x/2) + 5, -11, 0.01f), 2).alignedLeft().withDynamicScaling((int) (size.x/2)),
            () -> {
                try {
                    var v = getter.call();
                        setter.accept((v == null ? Boolean.TRUE : (v ? Boolean.FALSE : null)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
           position, size
        );
    }

    private Widget getOptionButton(String label, int column, int row, int count, Runnable onClick, SongSelectPage page) {
        int AVAILABLE_WIDTH = 750;

        int widgetWidth = AVAILABLE_WIDTH / count;

        int widgetX = (-(count * widgetWidth) / 2 + column * widgetWidth) + (widgetWidth/2);

        return new ButtonWidget(
            new Vector3f(widgetX, -275 + (40 * row), 0.01f), new Vector2f(widgetWidth, 40),
            onClick,
            new HoverWidget(new Vector3f(), new Vector2f(widgetWidth, 40), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> page == currentPage ? 0x5F444444 : 0x5F222222, () -> page == currentPage ? 0x5F444444 : 0x5F222222, 0)
            ), List.of(
                new DynamicGradientWidget(new Vector3f(), new Vector2f(widgetWidth, 40), () -> page == currentPage ? 0x5F888888 : 0x5F666666, () -> page == currentPage ? 0x5F888888 : 0x5F666666, 0)
            )),
            new TextWidget(label, new Vector3f(0, -10, 0.05f)).withScale(2)
        );
    }

    private final HashMap<String, ToggleWidget> toggles = new HashMap<>();

    private Consumer<Boolean> getExclusionHandler(String label, Consumer<Boolean> toggleHandler, String... exclusive) {
        return (b) -> {
            if (b) {
                for (String exclude : exclusive) {
                    if (toggles.containsKey(exclude)) {
                        toggles.get(exclude).setState(false);
                    } else {
                        Beatcraft.LOGGER.error("Missing exclusive modifier: '{}'", exclude);
                    }
                }
            }
            data.hudRenderer.controller.setModifier(label, b);
            toggleHandler.accept(b);
        };
    }

    private Widget getModifierToggle(String label, int row, int column, Consumer<Boolean> toggleHandler, String... exclusive) {
        int CENTER_X = 0;
        int CENTER_Y = 120;
        int MAX_WIDTH = 750;
        int MAX_HEIGHT = 400;
        int COLUMNS = 3;

        int widget_height = 64;
        int widget_width = MAX_WIDTH / COLUMNS;

        float x = CENTER_X + ((column - (COLUMNS - 1) / 2.0f) * widget_width);

        float y = CENTER_Y - ((MAX_HEIGHT / 2.0f) - row * widget_height);

        var toggle = getToggleWidget(widget_width, widget_height, getExclusionHandler(label, toggleHandler, exclusive));
        toggles.put(label, toggle);
        return new ContainerWidget(
            new Vector3f(x, y, 0), new Vector2f(widget_width, widget_height),
            toggle,
            new TextWidget(label, new Vector3f(0, -9, 0.05f)).withScale(1.5f)
        );
    }

    private void setModifierPage() {
        currentPage = SongSelectPage.Modifiers;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.SongSelect;

    }

    private void setPlayerOptionsPage() {
        currentPage = SongSelectPage.PlayerOptions;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.SongSelect;

    }

    private void setSettingsPage() {
        currentPage = SongSelectPage.Settings;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.Settings;

    }

    private void setDownloaderPage() {
        currentPage = SongSelectPage.Downloader;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.Downloader;
    }

    private void setReplayPage() {
        currentPage = SongSelectPage.Replay;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.SongSelect;
    }

    private void setSabersPage() {
        currentPage = SongSelectPage.Sabers;
        data.hudRenderer.controller.scene = HUDRenderer.MenuScene.SaberPreview;
    }

    // Modifier Toggles

    public void toggleModifiers(List<String> modifiers) {
        disableAll();
        modifiers.forEach(this::_toggleModifier);
    }

    private void _toggleModifier(String mod) {
        Consumer<Boolean> a = switch (mod) {
            case "No Fail" -> this::toggleNoFail;
            case "1 Life" -> this::toggle1Life;
            case "4 Lives" -> this::toggle4Lives;

            case "No Bombs" -> this::toggleNoBombs;
            case "No Walls" -> this::toggleNoObstacles;

            case "Ghost Notes" -> this::toggleGhostNotes;
            case "Disappearing Arrows" -> this::toggleDisappearingArrows;
            case "Zen Mode" -> this::toggleZenMode;

            case "Slower Song" -> this::toggleSlowerSong;
            case "Faster Song" -> this::toggleFasterSong;
            case "Super Fast Song" -> this::toggleSuperFastSong;

            default -> b -> {};
        };

        toggles.get(mod).setState(true);

        a.accept(true);

    }

    public void disableAll() {
        for (var toggle : toggles.values()) {
            toggle.setState(false);
        }
    }

    private void toggleNoFail(boolean state) {
        data.hudRenderer.controller.logic.noFail = state;
    }

    private void toggle1Life(boolean state) {
        data.hudRenderer.controller.logic.maxHealth = state ? 1 : 100;
        data.hudRenderer.controller.logic.health = state ? 1 : 50;
    }

    private void toggle4Lives(boolean state) {
        data.hudRenderer.controller.logic.maxHealth = state ? 4 : 100;
        data.hudRenderer.controller.logic.health = state ? 4 : 50;
    }

    private void toggleNoBombs(boolean state) {

    }

    private void toggleNoObstacles(boolean state) {

    }

    private void toggleNoArrows(boolean state) {

    }

    private void toggleGhostNotes(boolean state) {

    }

    private void toggleDisappearingArrows(boolean state) {

    }

    private void toggleSmallNotes(boolean state) {

    }

    private void toggleProMode(boolean state) {

    }

    private void toggleStrictAngles(boolean state) {

    }

    private void toggleZenMode(boolean state) {

    }

    private void toggleSlowerSong(boolean state) {
        data.hudRenderer.controller.setSpeed(state ? 0.85f : 1);
        data.hudRenderer.controller.logic.mapSpeed = state ? 0.85f : 1f;
    }

    private void toggleFasterSong(boolean state) {
        data.hudRenderer.controller.setSpeed(state ? 1.2f : 1);
        data.hudRenderer.controller.logic.mapSpeed = state ? 1.2f : 1f;
    }

    private void toggleSuperFastSong(boolean state) {
        data.hudRenderer.controller.setSpeed(state ? 1.5f : 1);
        data.hudRenderer.controller.logic.mapSpeed = state ? 1.5f : 1f;
    }

    // ^ Modifier Toggles

    private void updateVolume(int percent) {
        percent = Math.clamp(percent, 0, 100);
        AudioController.setVolume(percent/100f);
        BeatcraftClient.playerConfig.audio.volume(percent/100f);
    }

    private String getVolume() {
        return String.format("%.0f", BeatcraftClient.playerConfig.audio.volume() * 100);
    }


    private static final Component PLAY = Component.translatable("menu.beatcraft.song_select.play");
    private static final Component DELETE = Component.translatable("menu.beatcraft.song_select.delete");
    private Widget getReplayTile(ReplayInfo info, Vector3f position) {
        var SIZE = new Vector2f(660, 80);

        return new ContainerWidget(
            position,
            SIZE,
            new GradientWidget(
                new Vector3f(), SIZE,
                0x7F000000, 0x7F000000,
                0
            ),
            new TextWidget(info.name(), new Vector3f((-SIZE.x/2f) + 5, -35, -0.01f), 3).alignedLeft().withDynamicScaling((int) (SIZE.x - 300)/3),
            new TextWidget(info.mapID(), new Vector3f((-SIZE.x/2f) + 5, -6, -0.01f), 1.5f).alignedLeft().withDynamicScaling((int) (100f/1.5f)),
            new TextWidget(info.set(), new Vector3f((-SIZE.x/2f) + 95, 2, -0.01f), 2.5f).alignedLeft().withDynamicScaling(100),
            new TextWidget(info.diff(), new Vector3f(-50, 2, -0.01f), 2.5f).alignedLeft().withDynamicScaling(100),
            SettingsMenuPanel.getButton(
                new TextWidget(PLAY, new Vector3f(0, -11, -0.01f), 3),
                info::play,
                new Vector3f((SIZE.x/2f)-190, 0, 0),
                new Vector2f(100, 50)
            ),
            SettingsMenuPanel.getButton(
                new TextWidget(DELETE, new Vector3f(0, -11, -0.01f), 3),
                () -> {
                    data.hudRenderer.confirmSongDeleteMenuPanel = new ConfirmSongDeleteMenuPanel(info);
                    data.hudRenderer.controller.scene = HUDRenderer.MenuScene.ConfirmSongDelete;
                },
                new Vector3f((SIZE.x/2f)-65, 0, 0),
                new Vector2f(120, 50)
            )
        );
    }

    public boolean refreshReplays = false;

    protected ToggleWidget replayToggle;

    public void setReplayToggleState(boolean state) {
        replayToggle.setState(state);
    }

    private int currentReplayPage = 0;
    private int replaysPerPage = 4;

    private static final Vector3f basePos = new Vector3f(20, -150, 0);
    private static final int height = 360;

    private static final Component RECORD_NEXT = Component.translatable("menu.beatcraft.replay.record_next");
    public void setupReplayPageStatic() {
        replayPageStatic.children.clear();

        replayToggle = getToggleWidget(230, 50, b -> {
            if (b) {
                data.hudRenderer.controller.replayHandler.recordNextMap();
            } else {
                data.hudRenderer.controller.replayHandler.cancelRecording();
            }
        });

        replayPageStatic.children.addAll(List.of(
            SettingsMenuPanel.getButton(
                new TextureWidget(Beatcraft.id("textures/gui/song_selector/up_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f),
                () -> {
                    currentReplayPage = Math.max(0, currentReplayPage - 1);
                    setupReplayPage();
                },
                new Vector3f(-335, -160, 0), new Vector2f(50, 50),
                0, 0
            ),
            SettingsMenuPanel.getButton(
                new TextureWidget(Beatcraft.id("textures/gui/song_selector/down_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f),
                () -> {
                    currentReplayPage = Math.min(currentReplayPage + 1, ReplayHandler.getReplayCount() / replaysPerPage);
                    setupReplayPage();
                },
                new Vector3f(-335, 160, 0), new Vector2f(50, 50),
                0, 0
            ),

            new ContainerWidget(
                new Vector3f(0, 220, 0), new Vector2f(230, 50),
                replayToggle,
                new TextWidget(RECORD_NEXT, new Vector3f(0, -11, -0.01f), 3)
            )
        ));

        setupReplayPage();
    }

    public void setupReplayPage() {
        replayPage.children.clear();

        var si = currentReplayPage * replaysPerPage;
        List<ReplayInfo> replays = ReplayHandler.getReplays(si, replaysPerPage);

        int i = 0;
        for (var replay : replays) {
            var pos = basePos.add(0, i * (((float) height)/(float) replaysPerPage), 0, new Vector3f());
            replayPage.children.add(
                getReplayTile(replay, pos)
            );
            i++;
        }

    }

    protected ToggleWidget getToggleWidget(int widget_width, int widget_height, Consumer<Boolean> handler) {

        return new ToggleWidget(new Vector3f(0, 0, 0), new Vector2f(widget_width-4, widget_height-4), List.of(
            new HoverWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), List.of(
                new GradientWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), 0x5F113399, 0x5F113399, 0)
            ), List.of(
                new GradientWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), 0x5F1143CC, 0x5F1143CC, 0)
            ))
        ), List.of(
            new HoverWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), List.of(
                new GradientWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), 0x5F222222, 0x5F222222, 0)
            ), List.of(
                new GradientWidget(new Vector3f(), new Vector2f(widget_width-4, widget_height-4), 0x5F666666, 0x5F666666, 0)
            ))
        ), handler);
    }


    @Override
    public void render(MultiBufferSource.BufferSource immediate, Vector2f pointerPosition, boolean triggerPressed) {
        GuiGraphics context = new GuiGraphics(Minecraft.getInstance(), immediate);

        Vector3f camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

        context.pose().translate(-camPos.x, -camPos.y, -camPos.z);

        var wp = data.hudRenderer.controller.worldPosition;
        context.pose().translate(wp.x, wp.y, wp.z);
        context.pose().mulPose(new Quaternionf().rotationY(data.hudRenderer.controller.worldAngle));

        context.pose().translate(position.x, position.y, position.z);

        context.pose().mulPose(orientation);
        context.pose().scale(1/128f, 1/128f, 1/128f);
        context.pose().pushPose();

        context.fill((int) (-size.x/2f), (int) (-size.y/2f), (int) (size.x/2f), (int) (size.y/2f), backgroundColor);

        widgets.forEach(w -> w.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed));

        if (refreshReplays) {
            refreshReplays = false;
            setupReplayPage();
        }

        switch (currentPage) {
            case Modifiers -> {
                modifierPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
            case PlayerOptions -> {
                playerOptionsPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
            case Settings -> {
                settingsPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
            case Downloader -> {
                downloaderPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
            case Replay -> {
                replayPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
                replayPageStatic.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
            case Sabers -> {
                customSaberPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()), triggerPressed);
            }
        }

        context.flush();
        context.pose().popPose();
    }
}
