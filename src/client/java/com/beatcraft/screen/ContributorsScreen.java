package com.beatcraft.screen;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ContributorsScreen extends BaseOwoScreen<FlowLayout> {
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    private final Screen parent;
    public ContributorsScreen(Screen parent) {
        this.parent = parent;
    }

    public ContributorsScreen() {
        this(null);
    }

    @Override
    protected void build(FlowLayout flowLayout) {
        int SIZE = 25;
        var swifter_pfp = Components.texture(BeatCraft.id("textures/credits_menu/swifter_25x25.png"), 0, 0, SIZE, SIZE, SIZE, SIZE);
        var westbot_pfp = Components.texture(BeatCraft.id("textures/credits_menu/westbot_25x25.png"), 0, 0, SIZE, SIZE, SIZE, SIZE);

        //flowLayout.surface(Surface.blur(5, 10));

        flowLayout.surface(Surface.flat(0xFF020202));

        flowLayout.child(
            Containers.verticalScroll(Sizing.fill(), Sizing.fill(90),
                Containers.verticalFlow(Sizing.fill(), Sizing.content())
                    .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.content())
                            .child(
                                swifter_pfp
                            ).child(Components.spacer(2))
                            .child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                    .child(
                                        Components.label(Text.of("Swifter")).lineHeight(15)
                                    )
                                    .child(
                                        Components.label(Text.translatable("credits.beatcraft.developer"))
                                    )
                            ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    ).child(Components.spacer(2))
                    .child(
                        Containers.horizontalFlow(Sizing.fill(), Sizing.content())
                            .child(
                                westbot_pfp
                            ).child(Components.spacer(2))
                            .child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content())
                                    .child(
                                        Components.label(Text.of("Westbot")).lineHeight(15)
                                    )
                                    .child(
                                        Components.label(Text.translatable("credits.beatcraft.developer"))
                                    )
                            ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    ).child(Components.spacer(2))
                    .child(
                        Containers.verticalFlow(Sizing.fill(), Sizing.content())
                            .child(
                                Components.label(Text.of("FutureMapper")).lineHeight(12)
                            )
                            .child(
                                Components.label(Text.translatable("credits.beatcraft.contributor"))
                            ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    ).child(Components.spacer(2))
                    .child(
                        Containers.verticalFlow(Sizing.fill(), Sizing.content())
                            .child(
                                Components.label(Text.of("literallycat")).lineHeight(12)
                            )
                            .child(
                                Components.label(Text.translatable("credits.beatcraft.contributor"))
                            ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    )
            )
        ).child(
            Components.button(Text.translatable("screen.beatcraft.close"), (button) -> this.close())
        ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

}
