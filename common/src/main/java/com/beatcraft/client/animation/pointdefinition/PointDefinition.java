package com.beatcraft.client.animation.pointdefinition;

import com.beatcraft.Beatcraft;
import com.beatcraft.client.animation.Easing;
import com.beatcraft.client.animation.event.AnimatedPathEvent;
import com.beatcraft.client.animation.base_providers.BaseProviderHandler;
import com.beatcraft.client.beatmap.data.event.AnimateTrack;
import com.beatcraft.client.animation.event.AnimatedPropertyEvent;
import com.beatcraft.client.beatmap.data.event.AssignPathAnimation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

public abstract class PointDefinition<T> {
    protected ArrayList<Point<T>> points = new ArrayList<>();

    abstract protected T interpolatePoints(int a, int b, float time);

    public static boolean isModifier(JsonElement json) {
        if (!json.isJsonArray()) {
            if (json.isJsonPrimitive()) {
                var p = json.getAsJsonPrimitive();
                return (p.isString());
            }
        }
        for (var v : json.getAsJsonArray()) {
            if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) {
                var s = v.getAsString();
                if (s.startsWith("base") || s.startsWith("op")) {
                    return true;
                }
            } else if (v.isJsonArray()) {
                for (var x : v.getAsJsonArray()) {
                    if (x.isJsonPrimitive() && x.getAsJsonPrimitive().isString()) {
                        var s = x.getAsString();
                        if (s.startsWith("base") || s.startsWith("op")) {
                            return true;
                        }
                        if (s.startsWith("ease")) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean isSimple(JsonArray json) {
        if (json.get(0).isJsonArray()) {
            return false;
        } else if (json.get(0).isJsonPrimitive() && json.get(0).getAsJsonPrimitive().isString()) {
            return false;
        }
        return true;
    }
    protected abstract int getValueLength(JsonArray inner);
    public int getTimeIndex(JsonArray inner) {
        return getValueLength(inner);
    }
    public boolean hasFlags(JsonArray json) {
        return json.size() > getTimeIndex(json) + 1;
    }

    public Integer getFlagIndex(JsonArray json, String flag) {
        if (!hasFlags(json)) {
            return null;
        }

        for (int i = getTimeIndex(json) + 1; i < json.size(); i++) {
            JsonElement element = json.get(i);
            if (GsonHelper.isStringValue(element) && element.getAsString().contains(flag)) {
                return i;
            }
        }

        return null;
    }

    public Integer getEasingIndex(JsonArray json) {
        return getFlagIndex(json, "ease");
    }

    public Integer getSplineIndex(JsonArray json) {
        return getFlagIndex(json, "spline");
    }

    public PointDefinition(JsonArray json) throws RuntimeException {
        if (isSimple(json)) {
            loadSimple(json);
        } else {
            loadComplex(json);
        }
    }

    private void loadSimple(JsonArray json) {
        Point<T> point = new Point<>();
        loadValue(json, point, true);
        points.add(point);
    }

    private void loadComplex(JsonArray json) {
        if (isModifier(json)) {
            Point<T> point = new Point<>();
            loadValue(json, point, false);
            points.add(point);
        } else {
            json.forEach(x -> {
                JsonArray inner = x.getAsJsonArray();
                Point<T> point = new Point<>();

                float time = inner.get(getTimeIndex(inner)).getAsFloat();
                point.setTime(time);

                Integer easingIndex = getEasingIndex(inner);
                if (easingIndex != null) {
                    String easing = inner.get(easingIndex).getAsString();
                    point.setEasing(Easing.getEasing(easing));
                }

                Integer splineIndex = getSplineIndex(inner);
                point.setSpline(splineIndex != null);

                loadValue(inner, point, false);
                points.add(point);
            });
        }
    }
    protected abstract void loadValue(JsonArray json, Point<T> point, boolean isSimple);

    public T interpolate(float time) {
        if (points.isEmpty()) {
            return null;
        }

        Point<T> lastPoint = points.getLast();
        if (lastPoint.getTime() <= time) {
            return lastPoint.getValue();
        }

        Point<T> firstPoint = points.getFirst();
        if (firstPoint.getTime() >= time) {
            return firstPoint.getValue();
        }

        TimeIndexInfo indexInfo = searchIndexAtTime(time);
        Point<T> leftPoint = points.get(indexInfo.left);
        Point<T> rightPoint = points.get(indexInfo.right);

        float normalTime = 0;
        float divisor = rightPoint.getTime() - leftPoint.getTime();
        if (divisor != 0) {
            normalTime = (time - leftPoint.getTime()) / divisor;
        }

        if (rightPoint.getEasing() != null) {
            normalTime = rightPoint.getEasing().apply(normalTime);
        }

        return interpolatePoints(indexInfo.left, indexInfo.right, normalTime);
    }

    private record TimeIndexInfo(int left, int right) {}

    private TimeIndexInfo searchIndexAtTime(float time) {
        int left = 0;
        int right = points.size();

        while (left < right - 1) {
            int middle = (left + right) / 2;
            float pointTime = points.get(middle).getTime();

            if (pointTime < time) {
                left = middle;
            }
            else {
                right = middle;
            }
        }

        return new TimeIndexInfo(left, right);
    }

    public AnimatedPropertyEvent<T> toAnimatedPropertyEvent(AnimateTrack animateTrack) {
        return new AnimatedPropertyEvent<>(this, animateTrack);
    }

    public AnimatedPathEvent<T> toAnimatedPathEvent(AssignPathAnimation assignPathAnimation) {
        return new AnimatedPathEvent<>(this, assignPathAnimation);
    }
}
