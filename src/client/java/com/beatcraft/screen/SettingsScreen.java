package com.beatcraft.screen;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.types.Stash;
import com.beatcraft.render.DebugRenderer;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SettingsScreen extends BaseOwoScreen<FlowLayout> {

    private enum Page {
        GENERAL,
        QUALITY,
        AUDIO,
        DEBUG
    }

    private Page page = Page.GENERAL;

    private final Screen parent;

    private FlowLayout settingPage;

    public SettingsScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout flowLayout) {
        int BUTTON_WIDTH = 75;
        flowLayout.surface(Surface.VANILLA_TRANSLUCENT);
        settingPage = Containers.verticalFlow(Sizing.fill(75), Sizing.fill());
        setPage();
        flowLayout.child(Components.spacer(15)).child(
            Containers.verticalFlow(Sizing.fill(20), Sizing.fill(95))
                .child(Components.spacer(15))
                .child(
                    Components.button(Text.translatable("gui.beatcraft.button.general_settings"), this::gotoGeneralPage).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("gui.beatcraft.button.quality_settings"), this::gotoQualityPage).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("gui.beatcraft.button.audio_settings"), this::gotoAudioPage).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("gui.beatcraft.button.debug_settings"), this::gotoDebugPage).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("screen.beatcraft.song_downloader"), this::gotoSongDownloader).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("screen.beatcraft.credits"), this::openCredits).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                ).child(Components.spacer(2)).child(
                    Components.button(Text.translatable("screen.beatcraft.close"), (b) -> this.close()).sizing(Sizing.fixed(BUTTON_WIDTH), Sizing.content())
                )
        ).child(settingPage);
    }

    private void setPage() {
        settingPage.clearChildren();
        switch (page) {
            case GENERAL -> setGeneralPage();
            case QUALITY -> setQualityPage();
            case AUDIO -> setAudioPage();
            case DEBUG -> setDebugPage();
        }
    }

    private void setGeneralPage() {

        var trailIntensitySlider = Components.discreteSlider(Sizing.fill(50), 1, 20).value((Stash.getTrailSize()-10)/190f).message(str -> Text.of(str + "0"));
        trailIntensitySlider.onChanged().subscribe(this::updateTrailIntensity);

        var reducedDebrisToggle = Components.checkbox(Text.translatable("setting.beatcraft.player_option.reduced_debris"));
        reducedDebrisToggle.checked(BeatCraftClient.playerConfig.isReducedDebris());
        reducedDebrisToggle.onChanged(BeatCraftClient.playerConfig::setReducedDebris);

        var sparkParticlesToggle = Components.checkbox(Text.translatable("setting.beatcraft.quality.particles"));
        sparkParticlesToggle.checked(BeatCraftClient.playerConfig.doSparkParticles());
        sparkParticlesToggle.onChanged(BeatCraftClient.playerConfig::setSparkParticles);

        settingPage.child(Components.spacer(10)).child(
            Containers.grid(Sizing.fill(90), Sizing.content(), 1, 2)
                .child(
                    Components.label(Text.translatable("setting.beatcraft.quality.trail_intensity")).lineHeight(15),
                    0, 0
                ).child(
                    trailIntensitySlider,
                    0, 1
                )
        ).child(Components.spacer(10)).child(
            Containers.grid(Sizing.fill(90), Sizing.content(), 1, 2)
                .child(
                    reducedDebrisToggle,
                    0, 0
                )
                .child(
                    sparkParticlesToggle,
                    0, 1
                )
        );
    }

    private void setQualityPage() {

        var bloomfogToggle = Components.checkbox(Text.translatable("setting.beatcraft.quality.bloomfog"));
        bloomfogToggle.checked(BeatCraftClient.playerConfig.doBloomfog());
        bloomfogToggle.onChanged(BeatCraftClient.playerConfig::setBloomfogEnabled);

        var bloomToggle = Components.checkbox(Text.translatable("setting.beatcraft.quality.bloom"));
        bloomToggle.checked(BeatCraftClient.playerConfig.doBloom());
        bloomToggle.onChanged(BeatCraftClient.playerConfig::setBloomEnabled);

        var mirrorToggle = Components.checkbox(Text.translatable("setting.beatcraft.quality.mirror"));
        mirrorToggle.checked(BeatCraftClient.playerConfig.doMirror());
        mirrorToggle.onChanged(BeatCraftClient.playerConfig::setMirrorEnabled);

        var skyFogToggle = Components.checkbox(Text.translatable("setting.beatcraft.quality.sky_fog"));
        skyFogToggle.checked(BeatCraftClient.playerConfig.doSkyFog());
        skyFogToggle.onChanged(BeatCraftClient.playerConfig::setSkyFogEnabled);

        settingPage.child(Components.spacer(10)).child(
            Containers.grid(Sizing.fill(90), Sizing.content(), 2, 2)
                .child(
                    bloomfogToggle,
                    0, 0
                )
                .child(
                    bloomToggle,
                    0, 1
                )
                .child(
                    mirrorToggle,
                    1, 0
                )
                .child(
                    skyFogToggle,
                    1, 1
                )
        );

    }

    private void setAudioPage() {
        var slider = Components.discreteSlider(Sizing.fill(50), 0, 100).value(BeatCraftClient.playerConfig.getVolume()).message(str -> Text.of(str + "%"));
        slider.onChanged().subscribe(this::updateVolume);

        settingPage.child(Components.spacer(10)).child(
            Containers.grid(Sizing.fill(90), Sizing.content(), 1, 2)
                .child(
                    Components.label(Text.translatable("setting.beatcraft.audio.volume"))
                        .lineHeight(15),
                    0, 0
                ).child(
                    slider,
                    0, 1
                )
        );
    }

    private void setDebugPage() {
        settingPage.child(Components.spacer(10)).child(
            toggleOption(
                "setting.beatcraft.debug.main_renderer",
                DebugRenderer.doDebugRendering,
                this::toggleMainDebugRenderer
            )
        ).child(Components.spacer(2)).child(
            toggleOption(
                "setting.beatcraft.debug.saber_renderer",
                DebugRenderer.debugSaberRendering,
                this::toggleSaberDebugRenderer
            )
        ).child(Components.spacer(2)).child(
            toggleOption(
                "setting.beatcraft.debug.hitboxes",
                DebugRenderer.renderHitboxes,
                this::toggleHitboxRenderer
            )
        ).child(Components.spacer(2)).child(
            toggleOption(
                "setting.beatcraft.debug.arc_lines",
                DebugRenderer.renderArcDebugLines,
                this::toggleArcLineRenderer
            )
        );
    }

    private GridLayout toggleOption(String translatable, boolean state, Consumer<ButtonComponent> onClick) {
        return Containers.grid(Sizing.fill(), Sizing.content(), 1, 2)
            .child(
                Components.label(Text.translatable(translatable)).lineHeight(15),
                0, 0
            ).child(
                Components.button(getToggleText(state), onClick).sizing(Sizing.fixed(30), Sizing.content()),
                0, 1
            );
    }

    private void gotoGeneralPage(ButtonComponent button) {
        page = Page.GENERAL;
        setPage();
    }

    private void gotoQualityPage(ButtonComponent button) {
        page = Page.QUALITY;
        setPage();
    }

    private void gotoAudioPage(ButtonComponent button) {
        page = Page.AUDIO;
        setPage();
    }


    private void gotoDebugPage(ButtonComponent button) {
        page = Page.DEBUG;
        setPage();
    }

    private void gotoSongDownloader(ButtonComponent button) {
        var screen = new SongDownloaderScreen(this);
        assert client != null;
        client.setScreen(screen);
    }

    private void updateTrailIntensity(double value) {
        int size = (int) (value * 10);
        Stash.updateTrailSize(size);
    }

    private void updateVolume(double value) {
        BeatCraftClient.playerConfig.setVolume((float) value / 100f);
        BeatmapAudioPlayer.beatmapAudio.setVolume((float) value / 100f);
    }

    private void toggleMainDebugRenderer(ButtonComponent button) {
        DebugRenderer.doDebugRendering = !DebugRenderer.doDebugRendering;
        button.setMessage(getToggleText(DebugRenderer.doDebugRendering));
    }

    private void toggleSaberDebugRenderer(ButtonComponent button) {
        DebugRenderer.debugSaberRendering = !DebugRenderer.debugSaberRendering;
        button.setMessage(getToggleText(DebugRenderer.debugSaberRendering));
    }

    private void toggleHitboxRenderer(ButtonComponent button) {
        DebugRenderer.renderHitboxes = !DebugRenderer.renderHitboxes;
        button.setMessage(getToggleText(DebugRenderer.renderHitboxes));
    }

    private void toggleArcLineRenderer(ButtonComponent button) {
        DebugRenderer.renderArcDebugLines = !DebugRenderer.renderArcDebugLines;
        button.setMessage(getToggleText(DebugRenderer.renderArcDebugLines));
    }

    private Text getToggleText(boolean state) {
        if (state) {
            return Text.translatable("gui.beatcraft.option.on");
        } else {
            return Text.translatable("gui.beatcraft.option.off");
        }
    }

    private void openCredits(ButtonComponent button) {
        var screen = new ContributorsScreen(this);
        assert client != null;
        client.setScreen(screen);
    }

    @Override
    public void close() {
        BeatCraftClient.playerConfig.writeToFile();
        assert client != null;
        client.setScreen(parent);
    }
}
