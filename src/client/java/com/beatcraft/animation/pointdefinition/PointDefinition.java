package com.beatcraft.animation.pointdefinition;

import com.beatcraft.animation.Easing;
import com.beatcraft.beatmap.data.AnimateTrack;
import com.beatcraft.event.AnimatedPropertyEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;

public abstract class PointDefinition<T> {
    protected ArrayList<Point<T>> points = new ArrayList<>();

    abstract protected T interpolatePoints(int a, int b, float time);

    public static boolean isSimple(JsonArray json) {
        return !json.get(0).isJsonArray();
    }
    protected abstract int getValueLength();
    public int getTimeIndex() {
        return getValueLength();
    }
    public boolean hasFlags(JsonArray json) {
        return json.size() > getTimeIndex() + 1;
    }

    public Integer getFlagIndex(JsonArray json, String flag) {
        if (!hasFlags(json)) {
            return null;
        }

        for (int i = getTimeIndex() + 1; i < json.size(); i++) {
            JsonElement element = json.get(i);
            if (JsonHelper.isString(element) && element.getAsString().contains(flag)) {
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
        json.forEach(x -> {
            JsonArray inner = x.getAsJsonArray();
            Point<T> point = new Point<>();

            float time = inner.get(getTimeIndex()).getAsFloat();
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
}
