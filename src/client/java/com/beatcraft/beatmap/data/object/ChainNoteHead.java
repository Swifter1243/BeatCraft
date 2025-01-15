package com.beatcraft.beatmap.data.object;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.beatmap.Difficulty;
import com.beatcraft.beatmap.Info;
import com.beatcraft.beatmap.data.CutDirection;
import com.beatcraft.beatmap.data.HermiteSpline;
import com.beatcraft.beatmap.data.NoteType;
import com.beatcraft.data.types.Color;
import com.beatcraft.utils.JsonUtil;
import com.beatcraft.utils.MathUtil;
import com.beatcraft.utils.NoteMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ChainNoteHead extends GameplayObject {
    private float angleOffset = 0;
    private CutDirection cutDirection;
    private NoteType noteType;
    private Color color;
    private boolean disableNoteLook = false;
    private boolean disableNoteGravity = false;

    private void applyColorScheme(Info.SetDifficulty setDifficulty) {
        if (noteType == NoteType.RED) {
            color = setDifficulty.getColorScheme().getNoteLeftColor();
        } else {
            color = setDifficulty.getColorScheme().getNoteRightColor();
        }
    }


    public static Pair<ChainNoteHead, List<ChainNoteLink>> buildV3(JsonObject json, Difficulty difficulty) {
        ChainNoteHead headNote = new ChainNoteHead();
        headNote.loadV3(json, difficulty);

        headNote.cutDirection = CutDirection.values()[json.get("d").getAsInt()];
        headNote.noteType = NoteType.values()[JsonUtil.getOrDefault(json, "c", JsonElement::getAsInt, 0)];

        headNote.applyColorScheme(difficulty.getSetDifficulty());

        float tailBeat = json.get("tb").getAsFloat();
        float tailX = JsonUtil.getOrDefault(json, "tx", JsonElement::getAsFloat, 0f);
        float tailY = JsonUtil.getOrDefault(json, "ty", JsonElement::getAsFloat, 0f);

        int sliceCount = json.get("sc").getAsInt();
        float squishFactor = JsonUtil.getOrDefault(json, "s", JsonElement::getAsFloat, 1f);

        List<Pair<Vector3f, Float>> linkPositions = evaluateSpline(
            new Vector2f(headNote.getX(), headNote.getY()), headNote.getBeat(), headNote.cutDirection,
            new Vector2f(tailX, tailY), tailBeat, sliceCount, squishFactor
        );



        return new Pair<>(headNote, generateChainLinks(linkPositions, chainNoteLink -> {
            chainNoteLink.loadV3(json, difficulty);
            return null;
        }));
    }

    public static Pair<ChainNoteHead, List<ChainNoteLink>> buildV4(JsonObject json, JsonArray colorNotesData, JsonArray chainsData, Difficulty difficulty) {
        ChainNoteHead headNote = new ChainNoteHead();

        headNote.loadV4(json, colorNotesData, difficulty);
        headNote.beat = JsonUtil.getOrDefault(json, "hb", JsonElement::getAsFloat, 0f);

        int headMetaIndex = JsonUtil.getOrDefault(json, "i", JsonElement::getAsInt, 0);
        int chainMetaIndex = JsonUtil.getOrDefault(json, "ci", JsonElement::getAsInt, 0);

        JsonObject headMetaData = colorNotesData.get(headMetaIndex).getAsJsonObject();
        JsonObject chainMetaData = chainsData.get(chainMetaIndex).getAsJsonObject();

        headNote.cutDirection = CutDirection.values()[JsonUtil.getOrDefault(headMetaData, "d", JsonElement::getAsInt, 0)];
        headNote.noteType = NoteType.values()[JsonUtil.getOrDefault(headMetaData, "c", JsonElement::getAsInt, 0)];

        headNote.applyColorScheme(difficulty.getSetDifficulty());

        float tailX = JsonUtil.getOrDefault(chainMetaData, "tx", JsonElement::getAsFloat, 0f);
        float tailY = JsonUtil.getOrDefault(chainMetaData, "ty", JsonElement::getAsFloat, 0f);
        float tailBeat = json.get("tb").getAsFloat();
        int sliceCount = chainMetaData.get("c").getAsInt();
        float squishFactor = JsonUtil.getOrDefault(chainMetaData, "s", JsonElement::getAsFloat, 1f);

        List<Pair<Vector3f, Float>> linkPositions = evaluateSpline(
            new Vector2f(headNote.getX(), headNote.getY()), headNote.getBeat(), headNote.cutDirection,
            new Vector2f(tailX, tailY), tailBeat, sliceCount, squishFactor
        );

        return new Pair<>(headNote, generateChainLinks(linkPositions, chainNoteLink -> {
            chainNoteLink.loadV4(json, chainsData, headNote.noteType, difficulty);
            return null;
        }));
    }

    /// returns a list of positions for each slice in the chain.
    /// the Vector3f represents the grid-space x/y and then z as a beat.
    /// the float is the rotation of the note in degrees
    private static List<Pair<Vector3f, Float>> evaluateSpline(
        Vector2f headPosition, float headBeat, CutDirection headDirection,
        Vector2f tailPosition, float tailBeat,
        int sliceCount, float squishFactor
    ) {
        if (sliceCount == 0) return List.of();

        Vector3f headPos = new Vector3f(headPosition.x, headPosition.y, 0);
        Vector3f tailPos = new Vector3f(tailPosition.x, tailPosition.y, 0);

        float d = headPos.distance(tailPos);

        Vector3f splineMagnitude = new Vector3f(0, -d, 0);
        splineMagnitude.rotateZ(NoteMath.degreesFromCut(headDirection) * MathHelper.RADIANS_PER_DEGREE);


        HermiteSpline spline = new HermiteSpline(headPos, tailPos, splineMagnitude);

        float gap = squishFactor / (float) sliceCount;

        ArrayList<Pair<Vector3f, Float>> placements = new ArrayList<>();

        float beatSpan = tailBeat - headBeat;

        for (int i = 1; i <= sliceCount; i++) {

            Vector3f pos = spline.evaluate(gap * (float) i);
            Vector3f tangent = spline.getTangent(gap * (float) i);
            float angleDegrees = MathUtil.getVectorAngleDegrees(new Vector2f(tangent.x, tangent.y)) - 90;

            placements.add(new Pair<>(
                new Vector3f(pos.x, pos.y, headBeat + (beatSpan * (gap * (float) i))),
                angleDegrees
            ));

        }

        return placements;
    }

    private static List<ChainNoteLink> generateChainLinks(List<Pair<Vector3f, Float>> positions, Function<ChainNoteLink, Void> loader) {
        ArrayList<ChainNoteLink> chainLinks = new ArrayList<>();


        for (Pair<Vector3f, Float> pair : positions) {

            Vector3f pos = pair.getLeft();
            float angle = pair.getRight();

            ChainNoteLink chainLink = new ChainNoteLink();
            loader.apply(chainLink);

            chainLink.setAngleOffset(angle);
            chainLink.setPos(pos);
            chainLinks.add(chainLink);
        }


        return chainLinks;
    }

    public float getAngleOffset() {
        return angleOffset;
    }

    public CutDirection getCutDirection() {
        return cutDirection;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public Color getColor() {
        return color;
    }

    public boolean isNoteLookDisabled() {
        return disableNoteLook;
    }

    public boolean isNoteGravityDisabled() {
        return disableNoteGravity;
    }

}
