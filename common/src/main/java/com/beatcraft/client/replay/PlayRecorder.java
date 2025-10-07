package com.beatcraft.client.replay;


import com.beatcraft.client.beatmap.BeatmapController;
import com.beatcraft.common.replay.PlayFrame;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlayRecorder {

    public final BeatmapController controller;

    public String outputFile;
    public String songID;
    public String difficultySet;
    public String difficulty;
    private final ArrayList<PlayFrame> frames = new ArrayList<>();

    public PlayRecorder(BeatmapController controller) {
        this.controller = controller;
    }

    public void update(float beat) {
        if (outputFile != null && controller.isPlaying()) {
            frames.add(new PlayFrame(
                beat,
                new Vector3f(controller.logic.leftSaberPos), new Quaternionf(controller.logic.leftSaberRotation),
                new Vector3f(controller.logic.rightSaberPos), new Quaternionf(controller.logic.rightSaberRotation),
                new Vector3f(controller.logic.headPos), new Quaternionf(controller.logic.headRot)
            ));
        }
    }

    public void seek(float beat) {
        frames.removeIf(frame -> frame.beat() >= beat);
    }

    public void save() throws IOException {
        String path = Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/beatcraft/replay/" + outputFile;

        //JsonObject json = new JsonObject();

        //json.addProperty("song", songName);
        //json.addProperty("set", difficultySet);
        //json.addProperty("diff", difficulty);


        var idBytes = songID.getBytes(StandardCharsets.UTF_8);
        var setBytes = difficultySet.getBytes(StandardCharsets.UTF_8);
        var diffBytes = difficulty.getBytes(StandardCharsets.UTF_8);

        var sizeID = idBytes.length;
        var sizeSet = setBytes.length;
        var sizeDiff = diffBytes.length;

        // storing an array of N play frames
        // each play frame is 1 float, 3 vector3fs, and 3 quaternionfs
        var outputFrames = ByteBuffer.allocate(sizeID + sizeSet + sizeDiff + ((Float.BYTES * ((3 * 4) + (3 * 3) + 1)) * frames.size()) + (Integer.BYTES * 5));

        outputFrames.putInt(ReplayHandler.VERSION);

        outputFrames.putInt(sizeID);
        outputFrames.put(idBytes);

        outputFrames.putInt(sizeSet);
        outputFrames.put(setBytes);

        outputFrames.putInt(sizeDiff);
        outputFrames.put(diffBytes);

        outputFrames.putInt(frames.size());
        frames.forEach(frame -> frame.write(outputFrames));

        Files.write(Path.of(path), outputFrames.array());

        ReplayHandler.loadReplays();

    }

    public void reset() {
        frames.clear();
        outputFile = null;
        songID = null;
        difficultySet = null;
        difficulty = null;
    }

}
