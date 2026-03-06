package com.beatcraft.common.data.types;

import com.beatcraft.common.utils.JsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ColorScheme {
    private Color noteLeftColor              = new Color(0xFF_BF2F2F);
    private Color noteRightColor             = new Color(0xFF_1F63A7);
    private Color obstacleColor              = new Color(0xFF_FF2F2F);
    private Color environmentLeftColor       = new Color(0xFF_BF2F2F);
    private Color environmentLeftColorBoost  = new Color(0xFF_BF2F2F);
    private Color environmentRightColor      = new Color(0xFF_1F63A7);
    private Color environmentRightColorBoost = new Color(0xFF_1F63A7);
    private Color environmentWhiteColor      = new Color(0xFF_FFFFFF);
    private Color environmentWhiteColorBoost = new Color(0xFF_FFFFFF);
    private boolean isCustom = false;

    public ColorScheme() {

    }

    public ColorScheme(ColorScheme other) {
        noteLeftColor = new Color(other.noteLeftColor);
        noteRightColor = new Color(other.noteRightColor);
        obstacleColor = new Color(other.obstacleColor);
        environmentLeftColor = new Color(other.environmentLeftColor);
        environmentLeftColorBoost = new Color(other.environmentLeftColorBoost);
        environmentRightColor = new Color(other.environmentRightColor);
        environmentRightColorBoost = new Color(other.environmentRightColorBoost);
        environmentWhiteColor = new Color(other.environmentWhiteColor);
        environmentWhiteColorBoost = new Color(other.environmentWhiteColorBoost);
        isCustom = other.isCustom;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public JsonObject toJson() {
        var json = new JsonObject();

        json.addProperty("noteLeftColor", noteLeftColor.toHexString());
        json.addProperty("noteRightColor", noteRightColor.toHexString());
        json.addProperty("obstacleColor", obstacleColor.toHexString());
        json.addProperty("environmentLeftColor", environmentLeftColor.toHexString());
        json.addProperty("environmentLeftColorBoost", environmentLeftColorBoost.toHexString());
        json.addProperty("environmentRightColor", environmentRightColor.toHexString());
        json.addProperty("environmentRightColorBoost", environmentRightColorBoost.toHexString());
        json.addProperty("environmentWhiteColor", environmentWhiteColor.toHexString());
        json.addProperty("environmentWhiteColorBoost", environmentWhiteColorBoost.toHexString());

        return json;
    }

    private static Color fromStr(JsonElement in) {
        return Color.fromHexString(in.getAsString());
    }

    public static ColorScheme fromJsonOrDefault(JsonObject json, boolean isCustom) {
        var cs = new ColorScheme();
        cs.isCustom = isCustom;

        cs.noteLeftColor = JsonUtil.getOrDefault(json, "noteLeftColor", ColorScheme::fromStr, cs.noteLeftColor);
        cs.noteRightColor = JsonUtil.getOrDefault(json, "noteRightColor", ColorScheme::fromStr, cs.noteRightColor);
        cs.obstacleColor = JsonUtil.getOrDefault(json, "obstacleColor", ColorScheme::fromStr, cs.obstacleColor);
        cs.environmentLeftColor = JsonUtil.getOrDefault(json, "environmentLeftColor", ColorScheme::fromStr, cs.environmentLeftColor);
        cs.environmentLeftColorBoost = JsonUtil.getOrDefault(json, "environmentLeftColorBoost", ColorScheme::fromStr, cs.environmentLeftColorBoost);
        cs.environmentRightColor = JsonUtil.getOrDefault(json, "environmentRightColor", ColorScheme::fromStr, cs.environmentRightColor);
        cs.environmentRightColorBoost = JsonUtil.getOrDefault(json, "environmentRightColorBoost", ColorScheme::fromStr, cs.environmentRightColorBoost);
        cs.environmentWhiteColor = JsonUtil.getOrDefault(json, "environmentWhiteColor", ColorScheme::fromStr, cs.environmentWhiteColor);
        cs.environmentWhiteColorBoost = JsonUtil.getOrDefault(json, "environmentWhiteColorBoost", ColorScheme::fromStr, cs.environmentWhiteColorBoost);

        return cs;
    }

    public Color getIndexed(int idx) {
        return switch (idx) {
            case 0 -> noteLeftColor;
            case 1 -> noteRightColor;
            case 2 -> obstacleColor;
            case 3 -> environmentLeftColor;
            case 4 -> environmentRightColor;
            case 5 -> environmentWhiteColor;
            case 6 -> environmentLeftColorBoost;
            case 7 -> environmentRightColorBoost;
            default -> environmentWhiteColorBoost;
        };
    }

    public static ColorScheme getEnvironmentColorScheme(String environmentName) {
        return switch (environmentName) {
            case "DefaultEnvironment" -> getDefaultEnvironment();
            case "TriangleEnvironment" -> getTriangleEnvironment();
            case "NiceEnvironment" -> getNiceEnvironment();
            case "BigMirrorEnvironment" -> getBigMirrorEnvironment();
            case "DragonsEnvironment" -> getDragonsEnvironment();
            case "MonstercatEnvironment" -> getMonstercatEnvironment();
            case "PanicEnvironment" -> getPanicEnvironment();
            case "OriginsEnvironment" -> getOriginsEnvironment();
            case "KDAEnvironment" -> getKDAEnvironment();
            case "CrabRaveEnvironment" -> getCrabRaveEnvironment();
            case "RocketEnvironment" -> getRocketEnvironment();
            case "GreenDayEnvironment" -> getGreenDayEnvironment();
            case "TimbalandEnvironment" -> getTimbalandEnvironment();
            case "FitBeatEnvironment" -> getFitBeatEnvironment();
            case "LinkinParkEnvironment" -> getLinkinParkEnvironment();
            case "BTSEnvironment" -> getBTSEnvironment();
            case "KaleidoscopeEnvironment" -> getKaleidoscopeEnvironment();
            case "InterscopeEnvironment" -> getInterscopeEnvironment();
            case "SkrillexEnvironment" -> getSkrillexEnvironment();
            case "BillieEnvironment" -> getBillieEnvironment();
            case "HalloweenEnvironment" -> getHalloweenEnvironment();
            case "GagaEnvironment" -> getGagaEnvironment();
            case "WeaveEnvironment" -> getWeaveEnvironment();
            case "PyroEnvironment" -> getPyroEnvironment();
            case "EDMEnvironment" -> getEdmEnvironment();
            case "TheSecondEnvironment" -> getTheSecondEnvironment();
            case "LizzoEnvironment" -> getLizzoEnvironment();
            case "TheWeekndEnvironment" -> getTheWeekndEnvironment();
            case "RockMixtapeEnvironment" -> getRockMixtapeEnvironment();
            case "Dragons2Environment" -> getDragons2Environment();
            case "Panic2Environment" -> getPanic2Environment();
            case "QueenEnvironment" -> getQueenEnvironment();
            case "LinkinPark2Environment" -> getLinkinPark2Environment();
            case "TheRollingStonesEnvironment" -> getTheRollingStonesEnvironment();
            case "LatticeEnvironment" -> getLatticeEnvironment();
            case "DaftPunkEnvironment" -> getDaftPunkEnvironment();
            case "HipHopEnvironment" -> getHipHopEnvironment();
            case "ColliderEnvironment" -> getColliderEnvironment();
            case "BritneyEnvironment" -> getBritneyEnvironment();
            case "Monstercat2Environment" -> getMonstercat2Environment();
            case "MetallicaEnvironment" -> getMetallicaEnvironment();
            default -> new ColorScheme();
        };
    }

    public static ColorScheme getDefaultEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getTriangleEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getNiceEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getBigMirrorEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getDragonsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getMonstercatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getPanicEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_C71414));
        colorScheme.setNoteRightColor(new Color(0xFF_278DD1));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D81515));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_30ACFF));
        colorScheme.setObstacleColor(new Color(0xFF_FF3030));
        return colorScheme;
    }

    public static ColorScheme getOriginsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_AD9100));
        colorScheme.setNoteRightColor(new Color(0xFF_B40088));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_7DAEB2));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_09AEE6));
        colorScheme.setObstacleColor(new Color(0xFF_0F4965));
        return colorScheme;
    }

    public static ColorScheme getKDAEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_A74328));
        colorScheme.setNoteRightColor(new Color(0xFF_801491));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_FF653E));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_C220DD));
        colorScheme.setObstacleColor(new Color(0xFF_FF653E));
        return colorScheme;
    }

    public static ColorScheme getCrabRaveEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_00B513));
        colorScheme.setNoteRightColor(new Color(0xFF_0C81BB));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_22C027));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_0E9EE5));
        colorScheme.setObstacleColor(new Color(0xFF_00CF17));
        return colorScheme;
    }

    public static ColorScheme getRocketEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_FF7E00));
        colorScheme.setNoteRightColor(new Color(0xFF_0087FF));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_E57C52));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_66B7FF));
        colorScheme.setObstacleColor(new Color(0xFF_519BB9));
        return colorScheme;
    }

    public static ColorScheme getGreenDayEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_41C804));
        colorScheme.setNoteRightColor(new Color(0xFF_00B6AB));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_00B6AB));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_41C704));
        colorScheme.setObstacleColor(new Color(0xFF_00CF17));
        return colorScheme;
    }

    public static ColorScheme getTimbalandEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_808080));
        colorScheme.setNoteRightColor(new Color(0xFF_198CFF));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_198CFF));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_198CFF));
        colorScheme.setObstacleColor(new Color(0xFF_7F7F7F));
        return colorScheme;
    }

    public static ColorScheme getFitBeatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_CC9B28));
        colorScheme.setNoteRightColor(new Color(0xFF_CA28AE));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_CC8E8E));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_8E8ECC));
        colorScheme.setObstacleColor(new Color(0xFF_474766));
        return colorScheme;
    }

    public static ColorScheme getLinkinParkEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_A9292B));
        colorScheme.setNoteRightColor(new Color(0xFF_62838E));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_C0AB97));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_9FAFB4));
        colorScheme.setObstacleColor(new Color(0xFF_A92A2B));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_EB9741));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_48749E));
        return colorScheme;
    }

    public static ColorScheme getBTSEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_FF1667));
        colorScheme.setNoteRightColor(new Color(0xFF_CC00BF));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_C7207F));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_B120DD));
        colorScheme.setObstacleColor(new Color(0xFF_AA2D8C));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_E68AFF));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_58CDFF));
        return colorScheme;
    }

    public static ColorScheme getKaleidoscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_A82020));
        colorScheme.setNoteRightColor(new Color(0xFF_484848));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_A82020));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_787878));
        colorScheme.setObstacleColor(new Color(0xFF_404040));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_800000));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_7D0089));
        return colorScheme;
    }

    public static ColorScheme getInterscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_B99F4F));
        colorScheme.setNoteRightColor(new Color(0xFF_964BB8));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_B851E8));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_C3C1E8));
        colorScheme.setObstacleColor(new Color(0xFF_954BB8));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_CA6D6D));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_B3B6C3));
        return colorScheme;
    }

    public static ColorScheme getSkrillexEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_B2245E));
        colorScheme.setNoteRightColor(new Color(0xFF_535260));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_CC4795));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_109391));
        colorScheme.setObstacleColor(new Color(0xFF_289A9A));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_CF4E4E));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_47CC71));
        return colorScheme;
    }

    public static ColorScheme getBillieEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_CCA46E));
        colorScheme.setNoteRightColor(new Color(0xFF_8B9CA3));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_D1702E));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_EFB490));
        colorScheme.setObstacleColor(new Color(0xFF_B58FC7));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_CC0000));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_8EB3C6));
        return colorScheme;
    }

    public static ColorScheme getHalloweenEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_D17F46));
        colorScheme.setNoteRightColor(new Color(0xFF_605B66));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_E63A00));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_7591ED));
        colorScheme.setObstacleColor(new Color(0xFF_D1712F));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_56A155));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_9953DA));
        return colorScheme;
    }

    public static ColorScheme getGagaEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0xFF_D86EC7));
        colorScheme.setNoteRightColor(new Color(0xFF_77CC67));
        colorScheme.setEnvironmentLeftColor(new Color(0xFF_B4A53D));
        colorScheme.setEnvironmentRightColor(new Color(0xFF_E329BE));
        colorScheme.setObstacleColor(new Color(0xFF_FD00C4));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0xFF_C05C38));
        colorScheme.setEnvironmentRightColorBoost(new Color(0xFF_00B4FF));
        return colorScheme;
    }

    public static ColorScheme getWeaveEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFc81414, 0xFF288ed2,
            0xFFd91616, 0xFF30acff,
            0xFFff3030,
            0xFFd216d9, 0xFFa1a1a1
        );
        return cs;
    }

    public static ColorScheme getPyroEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFF93000a, 0xFFffab00,
            0xFFff1c34, 0xFFe2bc43,
            0xFFd9b36d,
            0xFFff002d, 0xFFc3c3c3
        );
        return cs;
    }

    public static ColorScheme getEdmEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFa1a1a1, 0xFF2db2e0,
            0xFF15b700, 0xFF005eb7,
            0xFF2db2e0,
            0xFFbc002d, 0xFF6d00c0
        );
        return cs;
    }

    public static ColorScheme getTheSecondEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFc81414, 0xFF288ed2,
            0xFFd91616, 0xFF30acff,
            0xFFff3030,
            0xFFd216d9, 0xFF00ffa5
        );
        return cs;
    }

    public static ColorScheme getLizzoEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFffcf60, 0xFFab41e5,
            0xFFd6a537, 0xFFd13de0,
            0xFFff8030,
            0xFFff668d, 0xFF5ecbff
        );
        return cs;
    }

    public static ColorScheme getTheWeekndEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFF952121, 0xFF394a54,
            0xFFff4c24, 0xFF2b60b4,
            0xFFea4c02,
            0xFFf49a1f, 0xFF86d3fd
        );
        cs.setEnvironmentWhiteColor(new Color(0xFFe0e0e0));
        cs.setEnvironmentWhiteColorBoost(new Color(0xFFfaeddc));
        return cs;
    }

    public static ColorScheme getRockMixtapeEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFF996c0b, 0xFF99bec7,
            0xFFbf1f29, 0xFFf29429,
            0xFFffffff,
            0xFFf522ea, 0xFF60cfe6
        );
        return cs;
    }

    public static ColorScheme getDragons2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFb9a848, 0xFF40c367,
            0xFF05fe11, 0xFF000eff,
            0xFF8d3fff,
            0xFFf90805, 0xFFffd33a
        );
        return cs;
    }

    public static ColorScheme getPanic2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFe65591, 0xFF278e7c,
            0xFFb21d5f, 0xFF309e9f,
            0xFFf76146,
            0xFFe67211, 0xFFa270db
        );
        cs.setEnvironmentWhiteColor(new Color(0xFFd4e6ec));
        cs.setEnvironmentWhiteColorBoost(new Color(0xFFe0d4ec));
        return cs;
    }

    public static ColorScheme getQueenEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFF94918e, 0xFF8622ad,
            0xFFeea31f, 0xFF0bb7e5,
            0xFFeea31f,
            0xFFc42513, 0xFF6602b9
        );
        return cs;
    }

    public static ColorScheme getLinkinPark2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFa92a2b, 0xFF63848e,
            0xFFa92a2c, 0xFF9fb0b5,
            0xFFa92a2c,
            0xFFeb9841, 0xFF48759f
        );
        cs.setEnvironmentWhiteColor(new Color(0xFFc0ac97));
        return cs;
    }

    public static ColorScheme getTheRollingStonesEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFe5001d, 0xFF8622ad,
            0xFFf30367, 0xFF7a67ff,
            0xFFf30367,
            0xFF907600, 0xFF01a3ab
        );
        return cs;
    }

    public static ColorScheme getLatticeEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFd62c8b, 0xFF00abfa,
            0xFFe429bf, 0xFF3295cb,
            0xFF77b5ff,
            0xFF8b22d0, 0xFF67eaea
        );
        return cs;
    }

    public static ColorScheme getDaftPunkEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFb83a2e, 0xFF1fb2b0,
            0xFFffb340, 0xFF8554d1,
            0xFF9b00ff,
            0xFFdb007a, 0xFF00d1cd
        );
        cs.setEnvironmentWhiteColor(new Color(0xFF7acfff));
        cs.setEnvironmentWhiteColorBoost(new Color(0xFFffd57c));
        return cs;
    }

    public static ColorScheme getHipHopEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFff9550, 0xFF049c96,
            0xFFe97e00, 0xFF0fcd2f,
            0xFFff508d,
            0xFF24fff7, 0xFF3a46ff
        );
        cs.setEnvironmentWhiteColor(new Color(0xFFccc1b4));
        cs.setEnvironmentWhiteColorBoost(new Color(0xFFe0e0e0));
        return cs;
    }

    public static ColorScheme getColliderEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFf67e26, 0xFF2b99db,
            0xFFf66800, 0xFF2b64db,
            0xFFd61900,
            0xFFe50906, 0xFFda69f8
        );
        return cs;
    }

    public static ColorScheme getBritneyEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFe92499, 0xFF3f94ff,
            0xFFfd03fc, 0xFF3181e7,
            0xFFd61900,
            0xFFff7582, 0xFF7bedcb
        );
        return cs;
    }

    public static ColorScheme getMonstercat2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFFdf5885, 0xFF634d9c,
            0xFF9e0dd3, 0xFF56b61c,
            0xFF3b26a3,
            0xFFb61c1c, 0xFF168ca5
        );
        return cs;
    }

    public static ColorScheme getMetallicaEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xFF485567, 0xFF93b7d2,
            0xFFdd7e61, 0xFF4174dd,
            0xFFd61900,
            0xFFdb6419, 0xFF00bfa1
        );
        cs.setEnvironmentWhiteColor(new Color(0xFF839bad));
        cs.setEnvironmentWhiteColorBoost(new Color(0xFF839bad));
        return cs;
    }

    public Color getNoteLeftColor() {
        return noteLeftColor;
    }

    public void setNoteLeftColor(Color noteLeftColor) {
        this.noteLeftColor = noteLeftColor;
    }

    public Color getNoteRightColor() {
        return noteRightColor;
    }

    public void setNoteRightColor(Color noteRightColor) {
        this.noteRightColor = noteRightColor;
    }

    public Color getObstacleColor() {
        return obstacleColor;
    }

    public void setObstacleColor(Color obstacleColor) {
        this.obstacleColor = obstacleColor;
    }

    public Color getEnvironmentLeftColor() {
        return environmentLeftColor;
    }

    public void setEnvironmentLeftColor(Color environmentLeftColor) {
        this.environmentLeftColor = environmentLeftColor;
    }

    public Color getEnvironmentLeftColorBoost() {
        return environmentLeftColorBoost;
    }

    public void setEnvironmentLeftColorBoost(Color environmentLeftColorBoost) {
        this.environmentLeftColorBoost = environmentLeftColorBoost;
    }

    public Color getEnvironmentRightColor() {
        return environmentRightColor;
    }

    public void setEnvironmentRightColor(Color environmentRightColor) {
        this.environmentRightColor = environmentRightColor;
    }

    public Color getEnvironmentRightColorBoost() {
        return environmentRightColorBoost;
    }

    public void setEnvironmentRightColorBoost(Color environmentRightColorBoost) {
        this.environmentRightColorBoost = environmentRightColorBoost;
    }

    public Color getEnvironmentWhiteColor() {
        return environmentWhiteColor;
    }

    public void setEnvironmentWhiteColor(Color environmentWhiteColor) {
        this.environmentWhiteColor = environmentWhiteColor;
    }

    public Color getEnvironmentWhiteColorBoost() {
        return environmentWhiteColorBoost;
    }

    public void setEnvironmentWhiteColorBoost(Color environmentWhiteColorBoost) {
        this.environmentWhiteColorBoost = environmentWhiteColorBoost;
    }

    public void setColors(
        int leftNote, int rightNote,
        int leftEnv, int rightEnv,
        int obstacleColor,
        int leftBoost, int rightBoost
    ) {
        this.setNoteLeftColor(new Color(leftNote));
        this.setNoteRightColor(new Color(rightNote));
        this.setEnvironmentLeftColor(new Color(leftEnv));
        this.setEnvironmentRightColor(new Color(rightEnv));
        this.setObstacleColor(new Color(obstacleColor));
        this.setEnvironmentLeftColorBoost(new Color(leftBoost));
        this.setEnvironmentRightColorBoost(new Color(rightBoost));
    }

}
