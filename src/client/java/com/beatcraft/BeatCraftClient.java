package com.beatcraft;


import com.beatcraft.beatmap.BeatmapPlayer;
import com.beatcraft.data.ColorNote;
import com.beatcraft.data.CutDirection;
import com.beatcraft.render.ClientRenderSubscriber;
import com.beatcraft.render.PhysicalColorNote;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BeatCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("playsong")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("Song played"));
                    return 1;
                })
                .then(argument("beat", FloatArgumentType.floatArg(0)).executes(context -> {
                    float beat = FloatArgumentType.getFloat(context, "beat");
                    BeatmapPlayer.play(beat);

                    context.getSource().sendFeedback(Text.literal("Song played at beat " + beat));
                    return 1;
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("pausesong")
                .executes(context -> {
                            BeatmapPlayer.pause();
                            context.getSource().sendFeedback(Text.literal("Song paused"));
                            return 1;
                        }
                )));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("restartsong")
                .executes(context -> {
                            BeatmapPlayer.restart();
                            context.getSource().sendFeedback(Text.literal("Song restarted"));
                            return 1;
                        }
                )));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("songspeed")
                .then(argument("speed", FloatArgumentType.floatArg(0)
                ).executes(context -> {
                    float speed = FloatArgumentType.getFloat(context, "speed");
                    BeatmapPlayer.speed = speed;
                    context.getSource().sendFeedback(Text.literal("Song speed set to " + speed + "!"));
                    return 1;
                }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("clearobjects")
                .executes(context -> {
                            ClientRenderSubscriber.physicalObjects.clear();
                            context.getSource().sendFeedback(Text.literal("Objects cleared!"));
                            return 1;
                        }
                )));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("addcolornote")
                .then(argument("beat", FloatArgumentType.floatArg(0)
                ).then(argument("x", FloatArgumentType.floatArg(0)
                ).then(argument("y", FloatArgumentType.floatArg(0)
                ).then(argument("angleOffset", FloatArgumentType.floatArg()
                ).then(argument("njs", FloatArgumentType.floatArg(0)
                ).then(argument("offset", FloatArgumentType.floatArg()
                ).then(argument("cutDirection", StringArgumentType.string()
                ).executes(context -> {
                    float beat = FloatArgumentType.getFloat(context, "beat");
                    float x = FloatArgumentType.getFloat(context, "x");
                    float y = FloatArgumentType.getFloat(context, "y");
                    float angleOffset = FloatArgumentType.getFloat(context, "angleOffset");
                    float njs = FloatArgumentType.getFloat(context, "njs");
                    float offset = FloatArgumentType.getFloat(context, "offset");
                    String cutDirectionRaw = StringArgumentType.getString(context, "cutDirection");
                    CutDirection cutDirection = getCutFromString(cutDirectionRaw);

                    ColorNote colorNote = new ColorNote();
                    colorNote.beat = beat;
                    colorNote.x = x;
                    colorNote.y = y;
                    colorNote.angleOffset = angleOffset;
                    colorNote.njs = njs;
                    colorNote.offset = offset;
                    colorNote.cutDirection = cutDirection;
                    PhysicalColorNote physicalColorNote = new PhysicalColorNote(colorNote);
                    ClientRenderSubscriber.physicalObjects.add(physicalColorNote);

                    context.getSource().sendFeedback(Text.literal("Color note placed at beat " + beat + "!"));
                    return 1;
                }))))))))));
    }

    private CutDirection getCutFromString(String cutDirection) {
        return switch (cutDirection) {
            case "up" -> CutDirection.UP;
            case "left" -> CutDirection.LEFT;
            case "right" -> CutDirection.RIGHT;
            case "up_left", "left_up", "upleft", "leftup" -> CutDirection.UP_LEFT;
            case "down_left", "left_down", "downleft", "leftdown" -> CutDirection.DOWN_LEFT;
            case "up_right", "right_up", "upright", "rightup" -> CutDirection.UP_RIGHT;
            case "down_right", "right_down", "downright", "rightdown" -> CutDirection.DOWN_RIGHT;
            case "dot" -> CutDirection.DOT;
            default -> CutDirection.DOWN;
        };
    }

}