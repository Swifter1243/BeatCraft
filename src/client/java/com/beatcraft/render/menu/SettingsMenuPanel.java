package com.beatcraft.render.menu;

import com.beatcraft.BeatCraftClient;
import com.beatcraft.menu.SettingsMenu;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.Callable;

public class SettingsMenuPanel extends MenuPanel<SettingsMenu> {
    public SettingsMenuPanel() {
        super(new SettingsMenu());
        backgroundColor = 0;
        position.set(0.1f, 2f, 6);
        size.set(1000, 500);

        initLayout();
    }

    private int selected;

    private Vector3f clampVector3(Vector3f input, int axis, float min, float max) {
        if (axis == 0) {
            return new Vector3f(Math.clamp(input.x, min, max), input.y, input.z);
        } else if (axis == 1) {
            return new Vector3f(input.x, Math.clamp(input.y, min, max), input.z);
        } else {
            return new Vector3f(input.x, input.y, Math.clamp(input.z, min, max));
        }
    }

    private boolean refresh = false;

    @Override
    public void render(VertexConsumerProvider.Immediate immediate, Vector2f pointerPosition) {
        super.render(immediate, pointerPosition);
        if (refresh) {
            refresh = false;
            initLayout();
        }
    }

    private void initLayout() {
        widgets.clear();

        selected = BeatCraftClient.playerConfig.getSelectedControllerProfileIndex();
        var profile = BeatCraftClient.playerConfig.getActiveControllerProfile();

        TextWidget selectedProfileDisplay = new TextWidget(
            selected == -1 ? "DEFAULT" : String.valueOf(selected),
            new Vector3f(0, 200-11, 0)
        ).withScale(3);

        ButtonWidget profileBack = getButton(new TextWidget("<", new Vector3f(0, -11, 0.05f)).withScale(3), this::back, new Vector3f(-100, 200, 0), new Vector2f(50, 50));
        ButtonWidget profileNext = getButton(new TextWidget(">", new Vector3f(0, -11, 0.05f)).withScale(3), this::next, new Vector3f(100, 200, 0), new Vector2f(50, 50));

        ButtonWidget newProfile = getButton(new TextWidget("NEW PROFILE", new Vector3f(0, -11, 0.05f)).withScale(3), this::addProfile, new Vector3f(270, 200, 0), new Vector2f(200, 50));

        widgets.addAll(List.of(
            new TextWidget("CONTROLLER PROFILES", new Vector3f(0, -200, 0), 3),
            selectedProfileDisplay,
            profileBack,
            profileNext,
            newProfile
        ));

        if (selected >= 0) {

            int LEFT = -100;
            int RIGHT = 250;

            widgets.addAll(List.of(
                getOptionModifier("Left position X",
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(-0.01f, 0, 0), 0, -0.1f, 0.1f)),
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(0.01f, 0, 0), 0, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getLeftTranslation().x),
                    new Vector3f(LEFT, -100, -0.01f)),
                getOptionModifier("Left position Y",
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(0, -0.01f, 0), 1, -0.1f, 0.1f)),
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(0, 0.01f, 0), 1, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getLeftTranslation().y),
                    new Vector3f(LEFT, -50, -0.01f)),
                getOptionModifier("Left position Z",
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(0, 0, -0.01f), 2, -0.1f, 0.1f)),
                    () -> profile.setLeftTranslation(clampVector3(profile.getLeftTranslation().add(0, 0, 0.01f), 2, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getLeftTranslation().z),
                    new Vector3f(LEFT, 0, -0.01f)),
                getOptionModifier("Left rotation X",
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(-1, 0, 0), 0, -180f, 180f)),
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(1, 0, 0), 0, -180f, 180f)),
                    () -> String.format("%.0f", profile.getLeftRotationEuler().x),
                    new Vector3f(LEFT, 50, -0.01f)),
                getOptionModifier("Left rotation Y",
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(0, -1, 0), 1, -180, 180)),
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(0, 1, 0), 1, -180, 180)),
                    () -> String.format("%.0f", profile.getLeftRotationEuler().y),
                    new Vector3f(LEFT, 100, -0.01f)),
                getOptionModifier("Left rotation Z",
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(0, 0, -1), 2, -180, 180)),
                    () -> profile.setLeftRotation(clampVector3(profile.getLeftRotationEuler().add(0, 0, 1), 2, -180, 180)),
                    () -> String.format("%.0f", profile.getLeftRotationEuler().z),
                    new Vector3f(LEFT, 150, -0.01f)),

                getOptionModifier("right position X",
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(-0.01f, 0, 0), 0, -0.1f, 0.1f)),
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(0.01f, 0, 0), 0, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getRightTranslation().x),
                    new Vector3f(RIGHT, -100, -0.01f)),
                getOptionModifier("right position Y",
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(0, -0.01f, 0), 1, -0.1f, 0.1f)),
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(0, 0.01f, 0), 1, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getRightTranslation().y),
                    new Vector3f(RIGHT, -50, -0.01f)),
                getOptionModifier("right position Z",
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(0, 0, -0.01f), 2, -0.1f, 0.1f)),
                    () -> profile.setRightTranslation(clampVector3(profile.getRightTranslation().add(0, 0, 0.01f), 2, -0.1f, 0.1f)),
                    () -> String.format("%.2f", profile.getRightTranslation().z),
                    new Vector3f(RIGHT, 0, -0.01f)),
                getOptionModifier("right rotation X",
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(-1, 0, 0), 0, -180f, 180f)),
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(1, 0, 0), 0, -180f, 180f)),
                    () -> String.format("%.0f", profile.getRightRotationEuler().x),
                    new Vector3f(RIGHT, 50, -0.01f)),
                getOptionModifier("right rotation Y",
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(0, -1, 0), 1, -180, 180)),
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(0, 1, 0), 1, -180, 180)),
                    () -> String.format("%.0f", profile.getRightRotationEuler().y),
                    new Vector3f(RIGHT, 100, -0.01f)),
                getOptionModifier("right rotation Z",
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(0, 0, -1), 2, -180, 180)),
                    () -> profile.setRightRotation(clampVector3(profile.getRightRotationEuler().add(0, 0, 1), 2, -180, 180)),
                    () -> String.format("%.0f", profile.getRightRotationEuler().z),
                    new Vector3f(RIGHT, 150, -0.01f))

            ));
        }

    }

    protected static ButtonWidget getButton(Widget display, Runnable onClick, Vector3f position, Vector2f size) {
        return new ButtonWidget(
            position, size, onClick,
            new HoverWidget(new Vector3f(), new Vector2f(size), List.of(
                 new GradientWidget(new Vector3f(), new Vector2f(size), 0x5F222222, 0x5F222222, 0)
            ), List.of(
                 new GradientWidget(new Vector3f(), new Vector2f(size), 0x5F444444, 0x5F444444, 0)
            )),
            display
        );
    }

    protected static Widget getOptionModifier(String label, Runnable down, Runnable up, Callable<String> getter, Vector3f position) {
        Vector2f SIZE = new Vector2f();

        try {
            var initial = getter.call();
            TextWidget valueDisplay = new TextWidget(initial, new Vector3f(0, -11, 0)).withScale(3);

            Runnable left = () -> {
                try {
                    down.run();
                    valueDisplay.text = getter.call();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            Runnable right = () -> {
                try {
                    up.run();
                    valueDisplay.text = getter.call();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            return new ContainerWidget(
                position, new Vector2f(SIZE),
                new TextWidget(label, new Vector3f(-160, -11, -0.01f)).withScale(1.5f),
                getButton(new TextWidget("<", new Vector3f(0, -11, 0.05f), 3), left, new Vector3f(-60, 0, 0), new Vector2f(50, 50)),
                valueDisplay,
                getButton(new TextWidget(">", new Vector3f(0, -11, 0.05f), 3), right, new Vector3f(60, 0, 0), new Vector2f(50, 50))
            );
        } catch (Exception e) {
            return new TextWidget("ERROR creating widget!", new Vector3f());
        }
    }

    private void back() {
        BeatCraftClient.playerConfig.selectProfile(selected-1);
        refresh = true;
    }

    private void next() {
        BeatCraftClient.playerConfig.selectProfile(selected+1);
        refresh = true;
    }

    private void addProfile() {
        BeatCraftClient.playerConfig.addProfile();
        BeatCraftClient.playerConfig.selectProfile(BeatCraftClient.playerConfig.getProfileCount());
    }


}
