package com.beatcraft.beatmap.data;

import java.util.ArrayList;
import java.util.List;

public class ChromaMaterialManager {
    private static List<ChromaMaterial> chromaMaterials = new ArrayList<ChromaMaterial>();

    public static void addChromaMaterial(ChromaMaterial chromaMaterial) {
        chromaMaterials.add(chromaMaterial);
    }
    public static List<ChromaMaterial> getChromaMaterials() {
        return chromaMaterials;
    }
    public static ChromaMaterial getChromaMaterial(String name) {
        for (ChromaMaterial chromaMaterial : chromaMaterials) {
            if (name == chromaMaterial.getName()){
                return chromaMaterial;
            }
        }
        return null;
    }
}
