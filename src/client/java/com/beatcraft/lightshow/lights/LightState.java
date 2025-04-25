package com.beatcraft.lightshow.lights;

import com.beatcraft.animation.Easing;
import com.beatcraft.data.types.Color;
import com.beatcraft.utils.MathUtil;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public class LightState {

    private Color color;
    private float brightness;
    private int effectiveColor = 0;

    public Function<Float, Float> strobeEasing = Easing::easeStep;
    public float strobeBrightness = 0;
    public float strobeFrequency = 0;
    public boolean strobeFade = false;

    public void setStrobeState(Function<Float, Float> easing, float brightness, float frequency, boolean fade) {
        strobeEasing = easing;
        strobeBrightness = brightness;
        strobeFrequency = frequency;
        strobeFade = fade;
    }

    public LightState(Color color, float brightness) {
        this.color = color;
        this.brightness = brightness;
        effectiveColor = calcEffectiveColor();
    }

    public void reset() {
        this.color.set(0, 0, 0, 0);
        this.brightness = 0;
        effectiveColor = calcEffectiveColor();
    }

    public LightState lerpFromTo(LightState to, float t) {
        var c = MathUtil.lerpColor(this.color, to.color, t);
        var b = MathHelper.lerp(t, this.brightness, to.brightness);
        return new LightState(c, b);
    }

    public void lerpFromTo(LightState to, float t, LightState dest) {
        var c = MathUtil.lerpColor(this.color, to.color, t);
        var b = MathHelper.lerp(t, this.brightness, to.brightness);
        dest.set(c, b);//return new LightState(c, b);
    }

    public void set(LightState other) {
        this.color.set(other.color);
        this.brightness = other.brightness;
        effectiveColor = calcEffectiveColor();
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
