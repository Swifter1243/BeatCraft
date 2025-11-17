# BeatCraft
A fan-made, faithful recreation of [Beat Saber](https://beatsaber.com/) in Minecraft.

This mod was inspired by deep knowledge of how Beat Saber functions, created from members of the community who have been studying it for years. 

Not only does this mod function like Beat Saber, it's also playable inside virtual reality using [Vivecraft!](https://modrinth.com/mod/vivecraft) This allows for a truly accurate experience, as you'll really be swinging your sabers physically in 3D space!

Experience a unique take on virtual reality's most popular rhythm game, all within the confines of the blocky sandbox we all know and love!

> [!NOTE]
> This mod is still in active development, please be patient about features that haven't been added yet, and report bugs to the GitHub [here](https://github.com/Swifter1243/BeatCraft/issues) (check that it hasn't already been reported first please)

### Join the discord server!
https://discord.gg/eQH4pbHptM

### Download the mod from modrinth or curseforge!
BeatCraft can be downloaded for fabric or neoforge from
[modrinth](https://modrinth.com/mod/beatcraft) or [curseforge](https://www.curseforge.com/minecraft/mc-mods/beatcraft)  
or from releases here on GitHub.  

Mod dependencies
- Vivecraft [fabric](https://modrinth.com/mod/vivecraft/version/1.21.1-1.3.2-fabric) / [neoforge](https://modrinth.com/mod/vivecraft/version/1.21.1-1.3.2-neoforge)
- Architectury API [fabric](https://modrinth.com/mod/architectury-api/version/13.0.8+fabric) / [neoforge](https://modrinth.com/mod/architectury-api/version/13.0.8+neoforge)


# Gameplay Feature roadmap
- [x] Load Beatmap V2, V3, V4
- [x] All game objects
- [x] Accurate spawn animations
- [ ] Accurate scoring
- [x] AnimateTrack, AssignPathAnimation events
- [ ] AssignPlayerToTrack (yes, separating hands and head stuff too), and AssignTrackParent events.
- [x] V2 lightshows
- [x] V3 lightshows
- [ ] V4 lightshows
- [x] Chroma coloring features
- [x] In-game song select and download screen
- [x] Level modifiers

# How to play
for casual gameplay, creative is recommended, but you can also access all gameplay elements in survival now too!  

# Mod Features

## Blocks/Items

### Sabers
must be held in order to cut notes.  

New recipes (pretend there's a saber in the output lol):  
<img height="100" alt="crafting-grid(1)" src="https://github.com/user-attachments/assets/fdf541f4-8197-4638-a6d8-e74014de8cda" />
<img height="100" alt="crafting-grid" src="https://github.com/user-attachments/assets/0c215daf-98ea-4bbe-9005-cc52b541f3e6" />


### Headset
must be worn to see the beatmap  

Recipe (dye positions can be swapped):  
<img height="100" alt="crafting-grid(2)" src="https://github.com/user-attachments/assets/83e12c2c-9b47-47cd-83b2-1bf1570ae8b2" />

## Commands
```
/beatmap
├─ place <positionXYZ> <rotation>
│       places a beatmap at the specified location and rotation (degrees), is not bound to a headset and you must use the track command to interact with it
├─ list
│       lists the UUIDs of all existing beatmaps
└─ <uuid>
   ├─ play
   │  │     resumes the selected beatmap if it is currently paused
   │  └─ <map> <set> <difficulty>
   │           plays the specified beatmap
   ├─ speed <value>
   │        sets the playback speed, can be between 0 (exclusive) and 7 (inclusive)
   ├─ seek <beat>
   │        jumps to the specified beat
   ├─ pause
   │        pauses the map if one is playing
   ├─ resume
   │        resumes the map if one is paused
   └─ track <player-uuid>
            makes the map track the specified player. That player will then be able to interact with menus and cut notes

/fpfc [true|false]
    First Person Face Controlled sabers (totally not first person free cam)  
    you can specify true or false to set, or just /fpfc alone to toggle  
    makes the saber colliders track to your head so you can cut notes outside VR  

```

## Multiplayer
Multiplayer is currently unsupported, but will now be easier for me to re-implement now that there can be multiple beatmaps  


