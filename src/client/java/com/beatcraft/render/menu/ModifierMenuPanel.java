package com.beatcraft.render.menu;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.data.types.CycleStack;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.logic.InputSystem;
import com.beatcraft.menu.ModifierMenu;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.replay.ReplayHandler;
import com.beatcraft.replay.ReplayInfo;
import com.beatcraft.screen.SongDownloaderScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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
        float angle = 60 * MathHelper.RADIANS_PER_DEGREE;
        position.set(0, 2, 6.4f);
        position.rotateY(angle);
        orientation.set(new Quaternionf().rotateY(angle));
        size.set(800, 600);
        backgroundColor = 0;

        initLayout();

        toggleModifiers(BeatCraftClient.playerConfig.getActiveModifiers());
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
                getModifierToggle("No Walls", 1, 1, this::toggleNoObstacles, "Zen Mode")
        //        getModifierToggle("No Arrows", 1, 2, this::toggleNoArrows, "Zen Mode"),

        //        getModifierToggle("Ghost Notes", 2, 0, this::toggleGhostNotes, "Disappearing Arrows", "Zen Mode"),
        //        getModifierToggle("Disappearing Arrows", 2, 1, this::toggleDisappearingArrows, "Ghost Notes", "Zen Mode"),
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
                () -> BeatCraftClient.playerConfig.setReducedDebris(false),
                () -> BeatCraftClient.playerConfig.setReducedDebris(true),
                () -> BeatCraftClient.playerConfig.isReducedDebris() ? "ON" : "OFF",
                new Vector3f(-100, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Particles",
                () -> BeatCraftClient.playerConfig.setSparkParticles(false),
                () -> BeatCraftClient.playerConfig.setSparkParticles(true),
                () -> BeatCraftClient.playerConfig.doSparkParticles() ? "ON" : "OFF",
                new Vector3f(-100, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Trail Intensity",
                () -> BeatCraftClient.playerConfig.setTrailIntensity(Math.max(3, CycleStack.getTrailSize()-1)),
                () -> BeatCraftClient.playerConfig.setTrailIntensity(Math.min(200, CycleStack.getTrailSize()+1)),
                () -> {
                    var x = BeatCraftClient.playerConfig.getTrailIntensity();
                    return x <= 3 ? "OFF" : String.valueOf(x);
                },
                new Vector3f(-100, -71, 0)),

            SettingsMenuPanel.getOptionModifier("Show Arms",
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands = false,
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands = true,
                () -> ClientDataHolderVR.getInstance().vrSettings.showPlayerHands ? "SHOW" : "HIDE",
                new Vector3f(-100, -19, 0)),

            SettingsMenuPanel.getOptionModifier("Show Hotbar",
                () -> MinecraftClient.getInstance().options.hudHidden = true,
                () -> MinecraftClient.getInstance().options.hudHidden = false,
                () -> MinecraftClient.getInstance().options.hudHidden ? "HIDE" : "SHOW",
                new Vector3f(-100, 32, 0)),

            SettingsMenuPanel.getOptionModifier("Movement Lock",
                InputSystem::unlockMovement,
                InputSystem::lockMovement,
                () -> InputSystem.isMovementLocked() ? "ON" : "OFF",
                new Vector3f(-100, 84, 0)),

            SettingsMenuPanel.getOptionModifier("Show HUD",
                () -> HUDRenderer.showHUD = false,
                () -> HUDRenderer.showHUD = true,
                () -> HUDRenderer.showHUD ? "SHOW" : "HIDE",
                new Vector3f(230, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Health Style",
                () -> BeatCraftClient.playerConfig.setHealthStyle(BeatCraftClient.playerConfig.getHealthStyle().ordinal()-1),
                () -> BeatCraftClient.playerConfig.setHealthStyle(BeatCraftClient.playerConfig.getHealthStyle().ordinal()+1),
                () -> BeatCraftClient.playerConfig.getHealthStyle().name(),
                new Vector3f(230, -123, 0))
        ));

        settingsPage.children.addAll(List.of(
            SettingsMenuPanel.getOptionModifier("Volume",
                () -> updateVolume((int) (BeatCraftClient.playerConfig.getVolume()*100)-5),
                () -> updateVolume((int) (BeatCraftClient.playerConfig.getVolume()*100)+5),
                this::getVolume,
                new Vector3f(-100, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Place Environments",
                () -> BeatCraftClient.playerConfig.setEnvironmentPlacing(false),
                () -> BeatCraftClient.playerConfig.setEnvironmentPlacing(true),
                () -> BeatCraftClient.playerConfig.doEnvironmentPlacing() ? "ON" : "OFF",
                new Vector3f(-100, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Bloomfog",
                () -> BeatCraftClient.playerConfig.setBloomfogEnabled(false),
                () -> BeatCraftClient.playerConfig.setBloomfogEnabled(true),
                () -> BeatCraftClient.playerConfig.doBloomfog() ? "ON" : "OFF",
                new Vector3f(230, -175, 0)),

            SettingsMenuPanel.getOptionModifier("Bloom",
                () -> BeatCraftClient.playerConfig.setBloomEnabled(false),
                () -> BeatCraftClient.playerConfig.setBloomEnabled(true),
                () -> BeatCraftClient.playerConfig.doBloom() ? "ON" : "OFF",
                new Vector3f(230, -123, 0)),

            SettingsMenuPanel.getOptionModifier("Mirror",
                () -> BeatCraftClient.playerConfig.setMirrorEnabled(false),
                () -> BeatCraftClient.playerConfig.setMirrorEnabled(true),
                () -> BeatCraftClient.playerConfig.doMirror() ? "ON" : "OFF",
                new Vector3f(230, -71, 0)),

            SettingsMenuPanel.getOptionModifier("Sky Fog",
                () -> BeatCraftClient.playerConfig.setSkyFogEnabled(false),
                () -> BeatCraftClient.playerConfig.setSkyFogEnabled(true),
                () -> BeatCraftClient.playerConfig.doSkyFog() ? "ON" : "OFF",
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
                        BeatCraft.LOGGER.error("Missing exclusive modifier: '{}'", exclude);
                    }
                }
            }
            BeatCraftClient.playerConfig.setModifier(label, b);
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
        HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;

    }

    private void setPlayerOptionsPage() {
        currentPage = SongSelectPage.PlayerOptions;
        HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;

    }

    private void setSettingsPage() {
        currentPage = SongSelectPage.Settings;
        HUDRenderer.scene = HUDRenderer.MenuScene.Settings;

    }

    private void setDownloaderPage() {
        currentPage = SongSelectPage.Downloader;
        HUDRenderer.scene = HUDRenderer.MenuScene.Downloader;
    }

    private void setReplayPage() {
        currentPage = SongSelectPage.Replay;
        HUDRenderer.scene = HUDRenderer.MenuScene.SongSelect;
    }

    private void setSabersPage() {
        currentPage = SongSelectPage.Sabers;
        HUDRenderer.scene = HUDRenderer.MenuScene.SaberPreview;
    }

    // Modifier Toggles

    public void toggleModifiers(List<String> modifiers) {
        modifiers.forEach(this::_toggleModifier);
    }

    private void _toggleModifier(String mod) {
        Consumer<Boolean> a = switch (mod) {
            case "No Fail" -> this::toggleNoFail;
            case "1 Life" -> this::toggle1Life;
            case "4 Lives" -> this::toggle4Lives;

            case "Slower Song" -> this::toggleSlowerSong;
            case "Faster Song" -> this::toggleFasterSong;
            case "Super Fast Song" -> this::toggleSuperFastSong;

            default -> b -> {};
        };

        toggles.get(mod).setState(true);

        a.accept(true);

    }

    private void toggleNoFail(boolean state) {
        GameLogicHandler.noFail = state;
    }

    private void toggle1Life(boolean state) {
        GameLogicHandler.maxHealth = state ? 1 : 100;
        GameLogicHandler.health = state ? 1 : 50;
    }

    private void toggle4Lives(boolean state) {
        GameLogicHandler.maxHealth = state ? 4 : 100;
        GameLogicHandler.health = state ? 4 : 50;
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
        BeatmapPlayer.setPlaybackSpeed(state ? 0.85f : 1);
        GameLogicHandler.mapSpeed = state ? 0.85f : 1f;
    }

    private void toggleFasterSong(boolean state) {
        BeatmapPlayer.setPlaybackSpeed(state ? 1.2f : 1);
        GameLogicHandler.mapSpeed = state ? 1.2f : 1f;
    }

    private void toggleSuperFastSong(boolean state) {
        BeatmapPlayer.setPlaybackSpeed(state ? 1.5f : 1);
        GameLogicHandler.mapSpeed = state ? 1.5f : 1f;
    }

    // ^ Modifier Toggles

    private void updateVolume(int percent) {
        percent = Math.clamp(percent, 0, 100);
        BeatmapAudioPlayer.beatmapAudio.setVolume(percent/100f);
        BeatCraftClient.playerConfig.setVolume(percent/100f);
    }

    private String getVolume() {
        return String.format("%.0f", BeatCraftClient.playerConfig.getVolume() * 100);
    }


    private Widget getReplayTile(ReplayInfo info, Vector3f position) {
        var SIZE = new Vector2f(660, 80);

        var tile = new ContainerWidget(
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
                new TextWidget("PLAY", new Vector3f(0, -11, -0.01f), 3),
                info::play,
                new Vector3f((SIZE.x/2f)-190, 0, 0),
                new Vector2f(100, 50)
            ),
            SettingsMenuPanel.getButton(
                new TextWidget("DELETE", new Vector3f(0, -11, -0.01f), 3),
                () -> {
                    HUDRenderer.confirmSongDeleteMenuPanel = new ConfirmSongDeleteMenuPanel(info);
                    HUDRenderer.scene = HUDRenderer.MenuScene.ConfirmSongDelete;
                },
                new Vector3f((SIZE.x/2f)-65, 0, 0),
                new Vector2f(120, 50)
            )
        );


        return tile;
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

    public void setupReplayPageStatic() {
        replayPageStatic.children.clear();

        replayToggle = getToggleWidget(230, 50, b -> {
            if (b) {
                ReplayHandler.recordNextMap();
            } else {
                ReplayHandler.cancelRecording();
            }
        });

        replayPageStatic.children.addAll(List.of(
            SettingsMenuPanel.getButton(
                new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/up_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f),
                () -> {
                    currentReplayPage = Math.max(0, currentReplayPage - 1);
                    setupReplayPage();
                },
                new Vector3f(-335, -160, 0), new Vector2f(50, 50),
                0, 0
            ),
            SettingsMenuPanel.getButton(
                new TextureWidget(Identifier.of(BeatCraft.MOD_ID, "textures/gui/song_selector/down_arrow.png"), new Vector3f(), new Vector2f(50, 50)).withScale(0.75f),
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
                new TextWidget("RECORD NEXT", new Vector3f(0, -11, -0.01f), 3)
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

    protected static ToggleWidget getToggleWidget(int widget_width, int widget_height, Consumer<Boolean> handler) {

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
    public void render(VertexConsumerProvider.Immediate immediate, Vector2f pointerPosition) {
        DrawContext context = new DrawContext(MinecraftClient.getInstance(), immediate);

        Vector3f camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos().toVector3f();

        context.translate(-camPos.x, -camPos.y, -camPos.z);
        context.translate(position.x, position.y, position.z);

        context.multiply(orientation);
        context.scale(1/128f, 1/128f, 1/128f);
        context.push();

        context.fill((int) (-size.x/2f), (int) (-size.y/2f), (int) (size.x/2f), (int) (size.y/2f), backgroundColor);

        widgets.forEach(w -> w.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f())));

        if (refreshReplays) {
            refreshReplays = false;
            setupReplayPage();
        }

        switch (currentPage) {
            case Modifiers -> {
                modifierPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
            case PlayerOptions -> {
                playerOptionsPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
            case Settings -> {
                settingsPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
            case Downloader -> {
                downloaderPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
            case Replay -> {
                replayPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
                replayPageStatic.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
            case Sabers -> {
                customSaberPage.draw(context, pointerPosition == null ? null : pointerPosition.mul(-128, new Vector2f()));
            }
        }

        context.draw();
        context.pop();
    }
}
