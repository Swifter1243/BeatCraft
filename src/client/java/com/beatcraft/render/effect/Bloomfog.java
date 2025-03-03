package com.beatcraft.render.effect;

import net.minecraft.client.render.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.function.BiConsumer;
public class Bloomfog {
    private final ArrayList<BiConsumer<BufferBuilder, Vector3f>> renderCalls = new ArrayList<>();


    public Bloomfog() {

    }

    public void record(BiConsumer<BufferBuilder, Vector3f> call) {
        //renderCalls.add(call);
    }

    public void render() {

    }


}