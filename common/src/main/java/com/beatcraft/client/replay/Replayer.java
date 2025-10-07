package com.beatcraft.client.replay;


import com.beatcraft.Beatcraft;
import com.beatcraft.client.BeatcraftClient;
import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.client.render.HUDRenderer;
import com.beatcraft.client.render.effect.SaberRenderer;
import com.beatcraft.common.data.components.ModComponents;
import com.beatcraft.common.data.map.SongData;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.common.replay.PlayFrame;
import com.beatcraft.common.utils.MathUtil;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Replayer {

    private final BeatmapController controller;

    private final ArrayList<PlayFrame> frames = new ArrayList<>();
    private final ArrayListDeque<PlayFrame> upcoming = new ArrayListDeque<>();
    private PlayFrame current = new PlayFrame(0, new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf());
    public boolean runReplay = false;
    private final ItemStack leftSaber = new ItemStack(ModItems.SABER_ITEM, 1);
    private final ItemStack rightSaber = new ItemStack(ModItems.SABER_ITEM, 1);

    public Replayer(BeatmapController controller) {
        this.controller = controller;
    }

    public void loadReplay(String replayFile) throws IOException {
        frames.clear();
        leftSaber.set(ModComponents.AUTO_SYNC_COLOR.get(), 0);
        leftSaber.set(ModComponents.SABER_COLOR_COMPONENT.get(), 0xc03030);

        rightSaber.set(ModComponents.AUTO_SYNC_COLOR.get(), 1);
        rightSaber.set(ModComponents.SABER_COLOR_COMPONENT.get(), 0x2064a8);

        byte[] rawData = Files.readAllBytes(Path.of(replayFile));

        ByteBuffer buf = ByteBuffer.wrap(rawData);

        int replayVersion = buf.getInt();

        if (replayVersion == 1) {
            loadV1(buf);
        }

    }

    private void loadV1(ByteBuffer buf) {
        int idSize = buf.getInt();
        byte[] idBytes = new byte[idSize];
        buf.get(idBytes, 0, idSize);
        var id = new String(idBytes, StandardCharsets.UTF_8);

        int setSize = buf.getInt();
        byte[] setBytes = new byte[setSize];
        buf.get(setBytes, 0, setSize);
        var set = new String(setBytes, StandardCharsets.UTF_8);

        int diffSize = buf.getInt();
        byte[] diffBytes = new byte[diffSize];
        buf.get(diffBytes, 0, diffSize);
        var diff = new String(diffBytes, StandardCharsets.UTF_8);

        int frameCount = buf.getInt();

        frames.add(new PlayFrame(0, new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf(), new Vector3f(), new Quaternionf()));

        for (int i = 0; i < frameCount; i++) {
            frames.add(PlayFrame.load(buf));
        }

        upcoming.addAll(frames);

        current = upcoming.pollFirst();

        SongData data = BeatcraftClient.songs.getById(id);

        runReplay = true;
        SongData.BeatmapInfo info = data.getBeatMapInfo(set, diff);

        if (info == null) {
            Beatcraft.LOGGER.info("Failed to load {}:{} from song: {}", set, diff, data);
            return;
        }

        controller.replayHandler.cancelRecording();

        controller.hudRenderer.songSelectMenuPanel.tryPlayMap(info);
    }

    public void reset() {
        frames.clear();
        upcoming.clear();
        runReplay = false;
    }

    public void update(float beat) {
        if (!runReplay || beat < 0) return;
        if (frames.isEmpty() || upcoming.isEmpty()) return;

        PlayFrame next;
        while (true) {
            next = upcoming.peekFirst();
            if (next == null) return;
            if (next.beat() < beat) {
                current = upcoming.pollFirst();
                if (current == null) return;
            } else {
                break;
            }
        }


        float sb = current.beat();
        float eb = next.beat();

        float f = MathUtil.inverseLerp(sb, eb, beat);

        Vector3f leftSaberPos = MathUtil.lerpVector3(current.leftSaberPosition(), next.leftSaberPosition(), f);
        Vector3f rightSaberPos = MathUtil.lerpVector3(current.rightSaberPosition(), next.rightSaberPosition(), f);
        Vector3f headPos = MathUtil.lerpVector3(current.headPos(), next.headPos(), f);

        Quaternionf leftSaberRot = MathUtil.lerpQuaternion(current.leftSaberRotation(), next.leftSaberRotation(), f);
        Quaternionf rightSaberRot = MathUtil.lerpQuaternion(current.rightSaberRotation(), next.rightSaberRotation(), f);
        Quaternionf headRot = MathUtil.lerpQuaternion(current.headRotation(), next.headRotation(), f);

        controller.logic.rightSaberPos = rightSaberPos;
        controller.logic.rightSaberRotation = rightSaberRot;
        controller.logic.leftSaberPos = leftSaberPos;
        controller.logic.leftSaberRotation = leftSaberRot;
        controller.logic.headPos = headPos;
        controller.logic.headRot = headRot;

        SaberRenderer.renderReplaySaber(leftSaber, leftSaberPos, leftSaberRot);
        SaberRenderer.renderReplaySaber(rightSaber, rightSaberPos, rightSaberRot);

        // ClientPlayNetworking.send(new SaberSyncC2SPayload(leftSaberPos, leftSaberRot, rightSaberPos, rightSaberRot, headPos, headRot));
        // ClientPlayNetworking.send(new BeatSyncC2SPayload(beat));

    }

    public void seek(float beat) {
        upcoming.clear();
        upcoming.addAll(frames);
    }


}
