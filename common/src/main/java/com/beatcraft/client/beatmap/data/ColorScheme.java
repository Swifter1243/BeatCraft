package com.beatcraft.client.beatmap.data;

import com.beatcraft.common.data.types.Color;

public class ColorScheme {
    private Color noteLeftColor = new Color(0.75294f, 0.188f, 0.188f);
    private Color noteRightColor = new Color(0.1254f, 0.3921f, 0.6588f);
    private Color obstacleColor = new Color(1, 0.1882f, 0.1882f);
    private Color environmentLeftColor = new Color(0.7529f, 0.188f, 0.188f);
    private Color environmentLeftColorBoost = new Color(0.7529f, 0.188f, 0.188f);
    private Color environmentRightColor = new Color(0.18823f, 0.5960f, 1);
    private Color environmentRightColorBoost = new Color(0.18823f, 0.5960f, 1);
    private Color environmentWhiteColor = new Color(1, 1, 1);
    private Color environmentWhiteColorBoost = new Color(1, 1, 1);

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
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getTriangleEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getNiceEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getBigMirrorEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getDragonsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getMonstercatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getPanicEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.7843137f, 0.07843138f, 0.07843138f));
        colorScheme.setNoteRightColor(new Color(0.1568627f, 0.5568627f, 0.8235294f));
        colorScheme.setEnvironmentLeftColor(new Color(0.85f, 0.08499997f, 0.08499997f));
        colorScheme.setEnvironmentRightColor(new Color(0.1882353f, 0.675294f, 1f));
        colorScheme.setObstacleColor(new Color(1f, 0.1882353f, 0.1882353f));
        return colorScheme;
    }

    public static ColorScheme getOriginsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.6792453f, 0.5712628f, 0f));
        colorScheme.setNoteRightColor(new Color(0.7075472f, 0f, 0.5364411f));
        colorScheme.setEnvironmentLeftColor(new Color(0.4910995f, 0.6862745f, 0.7f));
        colorScheme.setEnvironmentRightColor(new Color(0.03844783f, 0.6862745f, 0.9056604f));
        colorScheme.setObstacleColor(new Color(0.06167676f, 0.2869513f, 0.3962264f));
        return colorScheme;
    }

    public static ColorScheme getKDAEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.6588235f, 0.2627451f, 0.1607843f));
        colorScheme.setNoteRightColor(new Color(0.5019608f, 0.08235294f, 0.572549f));
        colorScheme.setEnvironmentLeftColor(new Color(1f, 0.3960785f, 0.2431373f));
        colorScheme.setEnvironmentRightColor(new Color(0.7607844f, 0.1254902f, 0.8666667f));
        colorScheme.setObstacleColor(new Color(1f, 0.3960785f, 0.2431373f));
        return colorScheme;
    }

    public static ColorScheme getCrabRaveEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0f, 0.7130001f, 0.07806564f));
        colorScheme.setNoteRightColor(new Color(0.04805952f, 0.5068096f, 0.734f));
        colorScheme.setEnvironmentLeftColor(new Color(0.134568f, 0.756f, 0.1557533f));
        colorScheme.setEnvironmentRightColor(new Color(0.05647058f, 0.6211764f, 0.9f));
        colorScheme.setObstacleColor(new Color(0f, 0.8117648f, 0.09019608f));
        return colorScheme;
    }

    public static ColorScheme getRocketEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(1f, 0.4980392f, 0f));
        colorScheme.setNoteRightColor(new Color(0f, 0.5294118f, 1f));
        colorScheme.setEnvironmentLeftColor(new Color(0.9f, 0.4866279f, 0.3244186f));
        colorScheme.setEnvironmentRightColor(new Color(0.4f, 0.7180724f, 1f));
        colorScheme.setObstacleColor(new Color(0.3176471f, 0.6117647f, 0.7254902f));
        return colorScheme;
    }

    public static ColorScheme getGreenDayEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.2588235f, 0.7843138f, 0.01960784f));
        colorScheme.setNoteRightColor(new Color(0f, 0.7137255f, 0.6705883f));
        colorScheme.setEnvironmentLeftColor(new Color(0f, 0.7137255f, 0.6705883f));
        colorScheme.setEnvironmentRightColor(new Color(0.2588235f, 0.7843137f, 0.01960784f));
        colorScheme.setObstacleColor(new Color(0f, 0.8117648f, 0.09019608f));
        return colorScheme;
    }

    public static ColorScheme getTimbalandEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.5019608f, 0.5019608f, 0.5019608f));
        colorScheme.setNoteRightColor(new Color(0.1f, 0.5517647f, 1f));
        colorScheme.setEnvironmentLeftColor(new Color(0.1f, 0.5517647f, 1f));
        colorScheme.setEnvironmentRightColor(new Color(0.1f, 0.5517647f, 1f));
        colorScheme.setObstacleColor(new Color(0.5f, 0.5f, 0.5f));
        return colorScheme;
    }

    public static ColorScheme getFitBeatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.8000001f, 0.6078432f, 0.1568628f));
        colorScheme.setNoteRightColor(new Color(0.7921569f, 0.1607843f, 0.682353f));
        colorScheme.setEnvironmentLeftColor(new Color(0.8f, 0.5594772f, 0.5594772f));
        colorScheme.setEnvironmentRightColor(new Color(0.5594772f, 0.5594772f, 0.8f));
        colorScheme.setObstacleColor(new Color(0.2784314f, 0.2784314f, 0.4f));
        return colorScheme;
    }

    public static ColorScheme getLinkinParkEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.6627451f, 0.1643608f, 0.1690187f));
        colorScheme.setNoteRightColor(new Color(0.3870196f, 0.5168997f, 0.5568628f));
        colorScheme.setEnvironmentLeftColor(new Color(0.7529412f, 0.672753f, 0.5925647f));
        colorScheme.setEnvironmentRightColor(new Color(0.6241197f, 0.6890281f, 0.709f));
        colorScheme.setObstacleColor(new Color(0.6627451f, 0.1647059f, 0.172549f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.922f, 0.5957885f, 0.255394f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.282353f, 0.4586275f, 0.6235294f));
        return colorScheme;
    }

    public static ColorScheme getBTSEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(1f, 0.09019607f, 0.4059771f));
        colorScheme.setNoteRightColor(new Color(0.8018868f, 0f, 0.7517689f));
        colorScheme.setEnvironmentLeftColor(new Color(0.7843137f, 0.1254902f, 0.5010797f));
        colorScheme.setEnvironmentRightColor(new Color(0.6941177f, 0.1254902f, 0.8666667f));
        colorScheme.setObstacleColor(new Color(0.6698113f, 0.1800908f, 0.5528399f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.9019608f, 0.5411765f, 1f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.3490196f, 0.8078431f, 1f));
        return colorScheme;
    }

    public static ColorScheme getKaleidoscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.65882355f, 0.1254902f, 0.1254902f));
        colorScheme.setNoteRightColor(new Color(0.28235295f, 0.28235295f, 0.28235295f));
        colorScheme.setEnvironmentLeftColor(new Color(0.65882355f, 0.1254902f, 0.1254902f));
        colorScheme.setEnvironmentRightColor(new Color(0.47058824f, 0.47058824f, 0.47058824f));
        colorScheme.setObstacleColor(new Color(0.25098041f, 0.25098041f, 0.25098041f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.50196081f, 0f, 0f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.49244517f, 0f, 0.53725493f));
        return colorScheme;
    }

    public static ColorScheme getInterscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.726415f, 0.62691f, 0.31181f));
        colorScheme.setNoteRightColor(new Color(0.589571f, 0.297888f, 0.723f));
        colorScheme.setEnvironmentLeftColor(new Color(0.724254f, 0.319804f, 0.913725f));
        colorScheme.setEnvironmentRightColor(new Color(0.764706f, 0.758971f, 0.913725f));
        colorScheme.setObstacleColor(new Color(0.588235f, 0.298039f, 0.721569f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.792453f, 0.429686f, 0.429868f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.7038f, 0.715745f, 0.765f));
        return colorScheme;
    }

    public static ColorScheme getSkrillexEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.69803923f, 0.14117648f, 0.36862746f));
        colorScheme.setNoteRightColor(new Color(0.32933334f, 0.32299998f, 0.38f));
        colorScheme.setEnvironmentLeftColor(new Color(0.80000001f, 0.28000003f, 0.58594489f));
        colorScheme.setEnvironmentRightColor(new Color(0.06525807f, 0.57800001f, 0.56867743f));
        colorScheme.setObstacleColor(new Color(0.15686275f, 0.60392159f, 0.60392159f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.81176478f, 0.30588236f, 0.30588236f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.27843139f, 0.80000001f, 0.44597632f));
        return colorScheme;
    }

    public static ColorScheme getBillieEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.8f, 0.64481932f, 0.432f));
        colorScheme.setNoteRightColor(new Color(0.54808509f, 0.61276591f, 0.64f));
        colorScheme.setEnvironmentLeftColor(new Color(0.81960785f, 0.442f, 0.184f));
        colorScheme.setEnvironmentRightColor(new Color(0.94117647f, 0.70677096f, 0.56470591f));
        colorScheme.setObstacleColor(new Color(0.71325314f, 0.56140977f, 0.78301889f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.8f, 0f, 0f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.55686277f, 0.7019608f, 0.77647066f));
        return colorScheme;
    }

    public static ColorScheme getHalloweenEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.81960785f, 0.49807876f, 0.27702752f));
        colorScheme.setNoteRightColor(new Color(0.37894738f, 0.35789475f, 0.4f));
        colorScheme.setEnvironmentLeftColor(new Color(0.90196079f, 0.23009226f, 0f));
        colorScheme.setEnvironmentRightColor(new Color(0.46005884f, 0.56889427f, 0.92941177f));
        colorScheme.setObstacleColor(new Color(0.81960791f, 0.44313729f, 0.18431373f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.33768433f, 0.63207543f, 0.33690813f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0.60209066f, 0.3280082f, 0.85849059f));
        return colorScheme;
    }

    public static ColorScheme getGagaEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.setNoteLeftColor(new Color(0.85f, 0.4333333f, 0.7833334f));
        colorScheme.setNoteRightColor(new Color(0.4705882f, 0.8f, 0.4078431f));
        colorScheme.setEnvironmentLeftColor(new Color(0.706f, 0.649f, 0.2394706f));
        colorScheme.setEnvironmentRightColor(new Color(0.894f, 0.1625455f, 0.7485644f));
        colorScheme.setObstacleColor(new Color(0.9921569f, 0f, 0.7719755f));
        colorScheme.setEnvironmentLeftColorBoost(new Color(0.754717f, 0.3610244f, 0.22071921f));
        colorScheme.setEnvironmentRightColorBoost(new Color(0f, 0.7058824f, 1f));
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
