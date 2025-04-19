package com.beatcraft.lightshow.lights;

import com.beatcraft.data.types.Color;
import com.beatcraft.utils.MathUtil;
import net.minecraft.util.math.MathHelper;

public class LightState {

    private Color color;
    private float brightness;
    private int effectiveColor = 0;

    public LightState(Color color, float brightness) {
        this.color = color;
        this.brightness = brightness;
        effectiveColor = calcEffectiveColor();
    }

    public LightState lerpFromTo(LightState to, float t) {
        var c = MathUtil.lerpColor(this.color, to.color, t);
        var b = MathHelper.lerp(t, this.brightness, to.brightness);
        return new LightState(c, b);
    }

    public LightState lerpToFrom(LightState from, float t) {
        return from.lerpFromTo(this, t);
    }

    public LightState copy() {
        return new LightState(new Color(color.toARGB()), brightness);
    }

    private int calcEffectiveColor() {
        return color.lerpBrightness(brightness * 1.2f);
    }

    public int getEffectiveColor() {
        return effectiveColor;
    }

    public int getBloomColor() {
        return color.lerpBrightness(brightness);
    }

    public float getBrightness() {
        return brightness;
    }

    public int getColor() {
        return color.toARGB();
    }

    public void clampAlpha() {
        color.setAlpha(Math.max(1f/255f, color.getAlpha()));
    }

    public void setColor(Color color) {
        this.color = color;
        effectiveColor = calcEffectiveColor();
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
        effectiveColor = calcEffectiveColor();
    }

    private void set(Color color, float brightness) {
        this.color = color;
        this.brightness = brightness;
        effectiveColor = calcEffectiveColor();
    }

    @Override
    public String toString() {
        return String.format("LightState{c:%s, b:%s}", Long.toString(getColor(), 16), brightness);
    }
}
