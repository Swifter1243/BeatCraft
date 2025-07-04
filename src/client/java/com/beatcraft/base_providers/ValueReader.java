package com.beatcraft.base_providers;

public interface ValueReader<T> {
    T get(T dest);
    T get();
}
