package com.beatcraft.beatmap.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChromaMaterialManager {
    private static final List<ChromaMaterial> chromaMaterials = new ArrayList<>();

    public static void addChromaMaterial(ChromaMaterial chromaMaterial) {
        chromaMaterials.add(chromaMaterial);
    }
    public static List<ChromaMaterial> getChromaMaterials() {
        return chromaMaterials;
    }
    public static ChromaMaterial getChromaMaterial(String name) {
        for (ChromaMaterial chromaMaterial : chromaMaterials) {
            if (Objects.equals(name, chromaMaterial.getName())){
                return chromaMaterial;
            }
        }
        return null;
    }
}
