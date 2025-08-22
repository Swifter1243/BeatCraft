package com.beatcraft.common.beatmap;

public class BeatmapInfo {

    public enum MapLocation {
        LOCAL,  // the map folder is stored locally in the profile's beatmaps folder
        SERVER, // map is stored directly on the currently connected server (identical to local for singleplayer)
        PEER,   // someone else connected to the same server has it // probably make it be opt-in to allow people to mirror maps from you (also might be dangerous, idk)
        WEB     // must fetch the map from beatsaver
    }




}
