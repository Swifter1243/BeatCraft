package com.beatcraft.client;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.audio.AudioController;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.beatmap.BeatmapManager;
import com.beatcraft.client.beatmap.BeatmapRenderer;
import com.beatcraft.client.commands.ClientCommands;
import com.beatcraft.client.logic.PhysicsTransform;
import com.beatcraft.client.menu.SongList;
import com.beatcraft.common.data.PlayerConfig;
import com.beatcraft.common.items.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.vivecraft.client_vr.ClientDataHolderVR;
import oshi.util.tuples.Triplet;

import java.util.HashMap;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class BeatcraftClient {
    public static SongList songs = new SongList();
    public static RandomSource random = RandomSource.create();
    public static PlayerConfig playerConfig;
    public static boolean wearingHeadset = false;
    public static boolean FPFC = false;
    public static final HashMap<UUID, Triplet<PhysicsTransform, PhysicsTransform, PhysicsTransform>> controllerTransforms = new HashMap<>();

    public static BeatmapController headsetLinkedBeatmap = null;

    public static void earlyInit() {
        Beatcraft.LOGGER.info("Initializing Beatcraft");
        playerConfig = PlayerConfig.loadFromFile();

    }

    public static void initCommands() {
        Beatcraft.LOGGER.info("Initializing commands");
        ClientCommands.init();
    }

    private static final Matrix4f mat4 = new Matrix4f();
    private static final Quaternionf rot = new Quaternionf();

    private static float snapAngle(float degrees) {
        float angle = Math.round(degrees / 45f) * 45f;
        return (float) Math.toRadians(angle);
    }


    public static void updatePlayerHeadPosAndFPFC(float tickDelta) {
        var vr = ClientDataHolderVR.getInstance().vr;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var uuid = player.getUUID();

        if (!controllerTransforms.containsKey(uuid)) {
            controllerTransforms.put(uuid, new Triplet<>(new PhysicsTransform(-0.4f, 0, 0), new PhysicsTransform(0, 0, 0), new PhysicsTransform(0.4f, 0, 0)));
        }
        var sabers = controllerTransforms.get(uuid);


        var newWearingHeadset = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HEADSET_ITEM);

        if (wearingHeadset && !newWearingHeadset) { // Headset taken off
            if (headsetLinkedBeatmap != null) {
                headsetLinkedBeatmap.stop();
                BeatmapManager.beatmaps.remove(headsetLinkedBeatmap);
                headsetLinkedBeatmap.delete();
                headsetLinkedBeatmap = null;
                AudioController.stopPreview();
            }
        } else if (newWearingHeadset && !wearingHeadset) { // put on
            if (headsetLinkedBeatmap == null) {
                headsetLinkedBeatmap = new BeatmapController(player.clientLevel, player.blockPosition().getBottomCenter().toVector3f(), snapAngle(-player.getViewYRot(tickDelta)), BeatmapRenderer.RenderStyle.HEADSET);
                BeatmapManager.beatmaps.add(headsetLinkedBeatmap);
                headsetLinkedBeatmap.trackPlayer(player.getUUID());
            }
        }
        wearingHeadset = newWearingHeadset;


        var headPos = player.getPosition(tickDelta).toVector3f().add(0, player.getEyeHeight(), 0);
        rot.identity()
            .rotateY(-player.getViewYRot(tickDelta) * Mth.DEG_TO_RAD)
            .normalize()
            .rotateX(player.getViewXRot(tickDelta) * Mth.DEG_TO_RAD)
            .normalize();

        if (vr != null && vr.isActive()) {
            mat4.identity().translate(headPos).rotate(rot);
            sabers.getB().update(mat4);
        } else if (FPFC) {
            rot.rotateX(90 * Mth.DEG_TO_RAD);
            mat4.identity().translate(headPos).rotate(rot);
            sabers.getA().update(mat4);
            sabers.getB().update(mat4);
            sabers.getC().update(mat4);
        }

        // TODO: send update packet to server so other players know the saber locations

    }

}
