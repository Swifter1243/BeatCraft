package com.beatcraft.beatmap.data;

import com.beatcraft.data.types.Color;

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
            0xc81414, 0x288ed2,
            0xd91616, 0x30acff,
            0xff3030,
            0xd216d9, 0xa1a1a1
        );
        return cs;
    }

    public static ColorScheme getPyroEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0x93000a, 0xffab00,
            0xff1c34, 0xe2bc43,
            0xd9b36d,
            0xff002d, 0xc3c3c3
        );
        return cs;
    }

    public static ColorScheme getEdmEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xa1a1a1, 0x2db2e0,
            0x15b700, 0x005eb7,
            0x2db2e0,
            0xbc002d, 0x6d00c0
        );
        return cs;
    }

    public static ColorScheme getTheSecondEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xc81414, 0x288ed2,
            0xd91616, 0x30acff,
            0xff3030,
            0xd216d9, 0x00ffa5
        );
        return cs;
    }

    public static ColorScheme getLizzoEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xffcf60, 0xab41e5,
            0xd6a537, 0xd13de0,
            0xff8030,
            0xff668d, 0x5ecbff
        );
        return cs;
    }

    public static ColorScheme getTheWeekndEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0x952121, 0x394a54,
            0xff4c24, 0x2b60b4,
            0xea4c02,
            0xf49a1f, 0x86d3fd
        );
        cs.setEnvironmentWhiteColor(new Color(0xe0e0e0));
        cs.setEnvironmentWhiteColorBoost(new Color(0xfaeddc));
        return cs;
    }

    public static ColorScheme getRockMixtapeEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0x996c0b, 0x99bec7,
            0xbf1f29, 0xf29429,
            0xffffff,
            0xf522ea, 0x60cfe6
        );
        return cs;
    }

    public static ColorScheme getDragons2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xb9a848, 0x40c367,
            0x05fe11, 0x000eff,
            0x8d3fff,
            0xf90805, 0xffd33a
        );
        return cs;
    }

    public static ColorScheme getPanic2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xe65591, 0x278e7c,
            0xb21d5f, 0x309e9f,
            0xf76146,
            0xe67211, 0xa270db
        );
        cs.setEnvironmentWhiteColor(new Color(0xd4e6ec));
        cs.setEnvironmentWhiteColorBoost(new Color(0xe0d4ec));
        return cs;
    }

    public static ColorScheme getQueenEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0x94918e, 0x8622ad,
            0xeea31f, 0x0bb7e5,
            0xeea31f,
            0xc42513, 0x6602b9
        );
        return cs;
    }

    public static ColorScheme getLinkinPark2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xa92a2b, 0x63848e,
            0xa92a2c, 0x9fb0b5,
            0xa92a2c,
            0xeb9841, 0x48759f
        );
        cs.setEnvironmentWhiteColor(new Color(0xc0ac97));
        return cs;
    }

    public static ColorScheme getTheRollingStonesEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xe5001d, 0x8622ad,
            0xf30367, 0x7a67ff,
            0xf30367,
            0x907600, 0x01a3ab
        );
        return cs;
    }

    public static ColorScheme getLatticeEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xd62c8b, 0x00abfa,
            0xe429bf, 0x3295cb,
            0x77b5ff,
            0x8b22d0, 0x67eaea
        );
        return cs;
    }

    public static ColorScheme getDaftPunkEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xb83a2e, 0x1fb2b0,
            0xffb340, 0x8554d1,
            0x9b00ff,
            0xdb007a, 0x00d1cd
        );
        cs.setEnvironmentWhiteColor(new Color(0x7acfff));
        cs.setEnvironmentWhiteColorBoost(new Color(0xffd57c));
        return cs;
    }

    public static ColorScheme getHipHopEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xff9550, 0x049c96,
            0xe97e00, 0x0fcd2f,
            0xff508d,
            0x24fff7, 0x3a46ff
        );
        cs.setEnvironmentWhiteColor(new Color(0xccc1b4));
        cs.setEnvironmentWhiteColorBoost(new Color(0xe0e0e0));
        return cs;
    }

    public static ColorScheme getColliderEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xf67e26, 0x2b99db,
            0xf66800, 0x2b64db,
            0xd61900,
            0xe50906, 0xda69f8
        );
        return cs;
    }

    public static ColorScheme getBritneyEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xe92499, 0x3f94ff,
            0xfd03fc, 0x3181e7,
            0xd61900,
            0xff7582, 0x7bedcb
        );
        return cs;
    }

    public static ColorScheme getMonstercat2Environment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0xdf5885, 0x634d9c,
            0x9e0dd3, 0x56b61c,
            0x3b26a3,
            0xb61c1c, 0x168ca5
        );
        return cs;
    }

    public static ColorScheme getMetallicaEnvironment() {
        ColorScheme cs = new ColorScheme();
        cs.setColors(
            0x485567, 0x93b7d2,
            0xdd7e61, 0x4174dd,
            0xd61900,
            0xdb6419, 0x00bfa1
        );
        cs.setEnvironmentWhiteColor(new Color(0x839bad));
        cs.setEnvironmentWhiteColorBoost(new Color(0x839bad));
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
