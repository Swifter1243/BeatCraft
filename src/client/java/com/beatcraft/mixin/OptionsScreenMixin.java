package com.beatcraft.mixin;

import com.beatcraft.screen.SettingsScreen;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/GridWidget;<init>()V"
        )
    )
    private void injectOptionButton(CallbackInfo ci, @Local(ordinal = 0) DirectionalLayoutWidget header) {
        ButtonWidget button = ButtonWidget
            .builder(Text.translatable("screen.beatcraft.settings"), btn -> this.client.setScreen(new SettingsScreen(this)))
            .size(150, 20)
            .build();

        if (!ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
            header.add(button, header.copyPositioner().alignLeft().marginTop(ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled ? -28 : -4));
        } else {
            header.add(button, header.copyPositioner().alignRight().marginTop(-28));
        }

    }

}
