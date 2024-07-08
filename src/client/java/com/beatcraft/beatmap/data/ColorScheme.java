package com.beatcraft.beatmap.data;

public class ColorScheme {
    public Color noteLeftColor = new Color(0.75294f, 0.188f, 0.188f);
    public Color noteRightColor = new Color(0.1254f, 0.3921f, 0.6588f);
    public Color obstacleColor = new Color(1, 0.1882f, 0.1882f);
    public Color environmentLeftColor = new Color(0.7529f, 0.188f, 0.188f);
    public Color environmentLeftColorBoost = new Color(0.7529f, 0.188f, 0.188f);
    public Color environmentRightColor = new Color(0.18823f, 0.5960f, 1);
    public Color environmentRightColorBoost = new Color(0.18823f, 0.5960f, 1);
    public Color environmentWhiteColor = new Color(1, 1, 1);
    public Color environmentWhiteColorBoost = new Color(1, 1, 1);

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
            default -> new ColorScheme();
        };
    }

    public static ColorScheme getDefaultEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getTriangleEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getNiceEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getBigMirrorEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getDragonsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getMonstercatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getPanicEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.7843137f, 0.07843138f, 0.07843138f);
        colorScheme.noteRightColor = new Color(0.1568627f, 0.5568627f, 0.8235294f);
        colorScheme.environmentLeftColor = new Color(0.85f, 0.08499997f, 0.08499997f);
        colorScheme.environmentRightColor = new Color(0.1882353f, 0.675294f, 1f);
        colorScheme.obstacleColor = new Color(1f, 0.1882353f, 0.1882353f);
        return colorScheme;
    }

    public static ColorScheme getOriginsEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.6792453f, 0.5712628f, 0f);
        colorScheme.noteRightColor = new Color(0.7075472f, 0f, 0.5364411f);
        colorScheme.environmentLeftColor = new Color(0.4910995f, 0.6862745f, 0.7f);
        colorScheme.environmentRightColor = new Color(0.03844783f, 0.6862745f, 0.9056604f);
        colorScheme.obstacleColor = new Color(0.06167676f, 0.2869513f, 0.3962264f);
        return colorScheme;
    }

    public static ColorScheme getKDAEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.6588235f, 0.2627451f, 0.1607843f);
        colorScheme.noteRightColor = new Color(0.5019608f, 0.08235294f, 0.572549f);
        colorScheme.environmentLeftColor = new Color(1f, 0.3960785f, 0.2431373f);
        colorScheme.environmentRightColor = new Color(0.7607844f, 0.1254902f, 0.8666667f);
        colorScheme.obstacleColor = new Color(1f, 0.3960785f, 0.2431373f);
        return colorScheme;
    }

    public static ColorScheme getCrabRaveEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0f, 0.7130001f, 0.07806564f);
        colorScheme.noteRightColor = new Color(0.04805952f, 0.5068096f, 0.734f);
        colorScheme.environmentLeftColor = new Color(0.134568f, 0.756f, 0.1557533f);
        colorScheme.environmentRightColor = new Color(0.05647058f, 0.6211764f, 0.9f);
        colorScheme.obstacleColor = new Color(0f, 0.8117648f, 0.09019608f);
        return colorScheme;
    }

    public static ColorScheme getRocketEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(1f, 0.4980392f, 0f);
        colorScheme.noteRightColor = new Color(0f, 0.5294118f, 1f);
        colorScheme.environmentLeftColor = new Color(0.9f, 0.4866279f, 0.3244186f);
        colorScheme.environmentRightColor = new Color(0.4f, 0.7180724f, 1f);
        colorScheme.obstacleColor = new Color(0.3176471f, 0.6117647f, 0.7254902f);
        return colorScheme;
    }

    public static ColorScheme getGreenDayEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.2588235f, 0.7843138f, 0.01960784f);
        colorScheme.noteRightColor = new Color(0f, 0.7137255f, 0.6705883f);
        colorScheme.environmentLeftColor = new Color(0f, 0.7137255f, 0.6705883f);
        colorScheme.environmentRightColor = new Color(0.2588235f, 0.7843137f, 0.01960784f);
        colorScheme.obstacleColor = new Color(0f, 0.8117648f, 0.09019608f);
        return colorScheme;
    }

    public static ColorScheme getTimbalandEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.5019608f, 0.5019608f, 0.5019608f);
        colorScheme.noteRightColor = new Color(0.1f, 0.5517647f, 1f);
        colorScheme.environmentLeftColor = new Color(0.1f, 0.5517647f, 1f);
        colorScheme.environmentRightColor = new Color(0.1f, 0.5517647f, 1f);
        colorScheme.obstacleColor = new Color(0.5f, 0.5f, 0.5f);
        return colorScheme;
    }

    public static ColorScheme getFitBeatEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.8000001f, 0.6078432f, 0.1568628f);
        colorScheme.noteRightColor = new Color(0.7921569f, 0.1607843f, 0.682353f);
        colorScheme.environmentLeftColor = new Color(0.8f, 0.5594772f, 0.5594772f);
        colorScheme.environmentRightColor = new Color(0.5594772f, 0.5594772f, 0.8f);
        colorScheme.obstacleColor = new Color(0.2784314f, 0.2784314f, 0.4f);
        return colorScheme;
    }

    public static ColorScheme getLinkinParkEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.6627451f, 0.1643608f, 0.1690187f);
        colorScheme.noteRightColor = new Color(0.3870196f, 0.5168997f, 0.5568628f);
        colorScheme.environmentLeftColor = new Color(0.7529412f, 0.672753f, 0.5925647f);
        colorScheme.environmentRightColor = new Color(0.6241197f, 0.6890281f, 0.709f);
        colorScheme.obstacleColor = new Color(0.6627451f, 0.1647059f, 0.172549f);
        colorScheme.environmentLeftColorBoost = new Color(0.922f, 0.5957885f, 0.255394f);
        colorScheme.environmentRightColorBoost = new Color(0.282353f, 0.4586275f, 0.6235294f);
        return colorScheme;
    }

    public static ColorScheme getBTSEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(1f, 0.09019607f, 0.4059771f);
        colorScheme.noteRightColor = new Color(0.8018868f, 0f, 0.7517689f);
        colorScheme.environmentLeftColor = new Color(0.7843137f, 0.1254902f, 0.5010797f);
        colorScheme.environmentRightColor = new Color(0.6941177f, 0.1254902f, 0.8666667f);
        colorScheme.obstacleColor = new Color(0.6698113f, 0.1800908f, 0.5528399f);
        colorScheme.environmentLeftColorBoost = new Color(0.9019608f, 0.5411765f, 1f);
        colorScheme.environmentRightColorBoost = new Color(0.3490196f, 0.8078431f, 1f);
        return colorScheme;
    }

    public static ColorScheme getKaleidoscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.65882355f, 0.1254902f, 0.1254902f);
        colorScheme.noteRightColor = new Color(0.28235295f, 0.28235295f, 0.28235295f);
        colorScheme.environmentLeftColor = new Color(0.65882355f, 0.1254902f, 0.1254902f);
        colorScheme.environmentRightColor = new Color(0.47058824f, 0.47058824f, 0.47058824f);
        colorScheme.obstacleColor = new Color(0.25098041f, 0.25098041f, 0.25098041f);
        colorScheme.environmentLeftColorBoost = new Color(0.50196081f, 0f, 0f);
        colorScheme.environmentRightColorBoost = new Color(0.49244517f, 0f, 0.53725493f);
        return colorScheme;
    }

    public static ColorScheme getInterscopeEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.726415f, 0.62691f, 0.31181f);
        colorScheme.noteRightColor = new Color(0.589571f, 0.297888f, 0.723f);
        colorScheme.environmentLeftColor = new Color(0.724254f, 0.319804f, 0.913725f);
        colorScheme.environmentRightColor = new Color(0.764706f, 0.758971f, 0.913725f);
        colorScheme.obstacleColor = new Color(0.588235f, 0.298039f, 0.721569f);
        colorScheme.environmentLeftColorBoost = new Color(0.792453f, 0.429686f, 0.429868f);
        colorScheme.environmentRightColorBoost = new Color(0.7038f, 0.715745f, 0.765f);
        return colorScheme;
    }

    public static ColorScheme getSkrillexEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.69803923f, 0.14117648f, 0.36862746f);
        colorScheme.noteRightColor = new Color(0.32933334f, 0.32299998f, 0.38f);
        colorScheme.environmentLeftColor = new Color(0.80000001f, 0.28000003f, 0.58594489f);
        colorScheme.environmentRightColor = new Color(0.06525807f, 0.57800001f, 0.56867743f);
        colorScheme.obstacleColor = new Color(0.15686275f, 0.60392159f, 0.60392159f);
        colorScheme.environmentLeftColorBoost = new Color(0.81176478f, 0.30588236f, 0.30588236f);
        colorScheme.environmentRightColorBoost = new Color(0.27843139f, 0.80000001f, 0.44597632f);
        return colorScheme;
    }

    public static ColorScheme getBillieEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.8f, 0.64481932f, 0.432f);
        colorScheme.noteRightColor = new Color(0.54808509f, 0.61276591f, 0.64f);
        colorScheme.environmentLeftColor = new Color(0.81960785f, 0.442f, 0.184f);
        colorScheme.environmentRightColor = new Color(0.94117647f, 0.70677096f, 0.56470591f);
        colorScheme.obstacleColor = new Color(0.71325314f, 0.56140977f, 0.78301889f);
        colorScheme.environmentLeftColorBoost = new Color(0.8f, 0f, 0f);
        colorScheme.environmentRightColorBoost = new Color(0.55686277f, 0.7019608f, 0.77647066f);
        return colorScheme;
    }

    public static ColorScheme getHalloweenEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.81960785f, 0.49807876f, 0.27702752f);
        colorScheme.noteRightColor = new Color(0.37894738f, 0.35789475f, 0.4f);
        colorScheme.environmentLeftColor = new Color(0.90196079f, 0.23009226f, 0f);
        colorScheme.environmentRightColor = new Color(0.46005884f, 0.56889427f, 0.92941177f);
        colorScheme.obstacleColor = new Color(0.81960791f, 0.44313729f, 0.18431373f);
        colorScheme.environmentLeftColorBoost = new Color(0.33768433f, 0.63207543f, 0.33690813f);
        colorScheme.environmentRightColorBoost = new Color(0.60209066f, 0.3280082f, 0.85849059f);
        return colorScheme;
    }

    public static ColorScheme getGagaEnvironment() {
        ColorScheme colorScheme = new ColorScheme();
        colorScheme.noteLeftColor = new Color(0.85f, 0.4333333f, 0.7833334f);
        colorScheme.noteRightColor = new Color(0.4705882f, 0.8f, 0.4078431f);
        colorScheme.environmentLeftColor = new Color(0.706f, 0.649f, 0.2394706f);
        colorScheme.environmentRightColor = new Color(0.894f, 0.1625455f, 0.7485644f);
        colorScheme.obstacleColor = new Color(0.9921569f, 0f, 0.7719755f);
        colorScheme.environmentLeftColorBoost = new Color(0.754717f, 0.3610244f, 0.22071921f);
        colorScheme.environmentRightColorBoost = new Color(0f, 0.7058824f, 1f);
        return colorScheme;
    }
}
