package com.beatcraft.client.lightshow.environment;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;

public class DataEnvironmentV2Layout {

    private enum IdGroup {
        LEFT_LASERS,
        RIGHT_LASERS,
        CENTER_LASERS,
        BACK_LASERS,
        RING_LIGHTS;

        static IdGroup get(String s) {
            return switch (s) {
                case "left-lasers" -> IdGroup.LEFT_LASERS;
                case "right-lasers" -> IdGroup.RIGHT_LASERS;
                case "center-lasers" -> IdGroup.CENTER_LASERS;
                case "back-lasers" -> IdGroup.BACK_LASERS;
                case "ring-lights" -> IdGroup.RING_LIGHTS;
                default -> throw new RuntimeException("Invalid id group: " + s);
            };
        }
    }

    private enum EventGroup {
        OuterRing,
        InnerRing,
        LeftSpinning,
        RightSpinning,
    }

    private static class IdIter implements Iterable<IdIter.Id>, Iterator<IdIter.Id> {

        record Id(IdGroup group, int id) {}

        private final Object[] arr;

        private IdGroup currentGroup = null;
        private int idx = 0;

        IdIter(JsonArray data) {
            arr = new Object[data.size()];
            for (var i = 0; i < data.size(); ++i) {
                var v = data.get(i).getAsJsonPrimitive();
                if (v.isString()) {
                    var group = IdGroup.get(v.getAsString());
                    arr[i] = group;
                    if (currentGroup == null) {
                        currentGroup = group;
                    }
                } else {
                    var x = v.getAsInt();
                    arr[i] = x;
                }
            }
        }

        @Override
        public @NotNull Iterator<Id> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return idx < arr.length;
        }

        @Override
        public Id next() {
            while (true) {
                var x = arr[idx++];
                if (x instanceof IdGroup group) {
                    currentGroup = group;
                }
                else if (x instanceof Integer i) {
                    return new Id(currentGroup, i);
                }
            }
        }
    }

    ResourceLocation envId;

    public DataEnvironmentV2Layout(ResourceLocation envId) throws IOException {
        this.envId = envId;
        load();
    }

    public void load() throws IOException {
        var rm = Minecraft.getInstance().getResourceManager();
        var reader = rm.getResource(envId).orElseThrow().openAsReader();
        var json = JsonParser.parseReader(reader).getAsJsonObject();

        var layout = json.getAsJsonArray("layout");

        for (var )

    }

}
