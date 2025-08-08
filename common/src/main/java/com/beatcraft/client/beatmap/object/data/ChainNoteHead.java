package com.beatcraft.client.beatmap.object.data;

import com.beatcraft.client.beatmap.data.Difficulty;
import com.beatcraft.client.beatmap.data.Info;
import com.beatcraft.client.beatmap.data.CutDirection;
import com.beatcraft.client.beatmap.data.NoteType;
import com.beatcraft.common.data.types.BezierCurve;
import com.beatcraft.common.data.types.Color;
import com.beatcraft.common.utils.JsonUtil;
import com.beatcraft.common.utils.MathUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ChainNoteHead extends GameplayObject implements ScorableObject {
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

        headNote.cutDirection = CutDirection.values()[JsonUtil.getOrDefault(json, "d", JsonElement::getAsInt, 0)];
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
        Vector3f tailOffset = new Vector3f(tailPosition.x, tailPosition.y, 0).sub(headPos);
        float magnitude = tailOffset.length();
        float f = (headDirection.baseAngleDegrees - 90f) * Mth.DEG_TO_RAD;
        Vector3f control = new Vector3f(((float) Math.cos(f)) * 0.5f * magnitude, ((float) Math.sin(f)) * 0.5f * magnitude, 0);

        BezierCurve spline = new BezierCurve(new Vector3f(0, 0, 0), control, tailOffset);

        float gap = squishFactor / (float) sliceCount;

        ArrayList<Pair<Vector3f, Float>> placements = new ArrayList<>();

        float beatSpan = tailBeat - headBeat;

        for (int i = 1; i <= sliceCount; i++) {

            Vector3f pos = spline.evaluate(gap * (float) i).add(headPos);
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

            Vector3f pos = pair.getA();
            float angle = pair.getB();

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

    @Override
    public Color score$getColor() {
        return color;
    }

    @Override
    public NoteType score$getNoteType() {
        return noteType;
    }

    @Override
    public CutDirection score$getCutDirection() {
        return getCutDirection();
    }
}
