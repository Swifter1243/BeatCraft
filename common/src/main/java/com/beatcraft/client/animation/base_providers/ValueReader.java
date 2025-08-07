package com.beatcraft.client.animation.base_providers;

public interface ValueReader<T> {
    T get(T dest);
    T get();
}
