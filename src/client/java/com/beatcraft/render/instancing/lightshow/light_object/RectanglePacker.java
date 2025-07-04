package com.beatcraft.render.instancing.lightshow.light_object;


import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class RectanglePacker {
    private final int binWidth;
    private final int binHeight;
    private final List<Shelf> shelves;

    public RectanglePacker(int width, int height) {
        this.binWidth = width;
        this.binHeight = height;
        this.shelves = new ArrayList<>();
    }

    public Vector2i pack(int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        if (width > binWidth || height > binHeight) {
            return null;
        }

        for (Shelf shelf : shelves) {
            Vector2i result = shelf.tryPack(width, height);
            if (result != null) {
                return result;
            }
        }

        int nextShelfY = getNextShelfY();
        if (nextShelfY + height <= binHeight) {
            Shelf newShelf = new Shelf(nextShelfY, height);
            Vector2i result = newShelf.tryPack(width, height);
            if (result != null) {
                shelves.add(newShelf);
                return result;
            }
        }

        return null;
    }

    private int getNextShelfY() {
        if (shelves.isEmpty()) {
            return 0;
        }
        Shelf lastShelf = shelves.getLast();
        return lastShelf.y + lastShelf.height;
    }

    public double getUtilization() {
        int totalArea = binWidth * binHeight;
        int usedArea = 0;

        for (Shelf shelf : shelves) {
            usedArea += shelf.getUsedArea();
        }

        return (double) usedArea / totalArea * 100.0;
    }

    public void clear() {
        shelves.clear();
    }

    public int getWidth() {
        return binWidth;
    }

    public int getHeight() {
        return binHeight;
    }

    private class Shelf {
        final int y;
        final int height;
        int currentX;

        Shelf(int y, int height) {
            this.y = y;
            this.height = height;
            this.currentX = 0;
        }

        Vector2i tryPack(int width, int height) {
            if (height <= this.height && currentX + width <= binWidth) {
                Vector2i result = new Vector2i(currentX, y);
                currentX += width;
                return result;
            }
            return null;
        }

        int getUsedArea() {
            return currentX * height;
        }
    }
}