# BeatCraft
A fan-made, faithful recreation of [Beat Saber](https://beatsaber.com/) in Minecraft.

This mod was inspired by deep knowledge of how Beat Saber functions, created from members of the community who have been studying it for years. 

Not only does this mod function like Beat Saber, it's also playable inside of virtual reality using [Vivecraft!](https://modrinth.com/mod/vivecraft) This allows for a truly accurate experience, as you'll really be swinging your sabers physically in 3D space!

Experience a unique take on virtual reality's most popular rhythm game, all within the confines of the blocky sandbox we all know and love!

> [!NOTE]
> This mod is still in active development, please be patient about features that haven't been added yet, and report bugs to the github [here](https://github.com/Swifter1243/BeatCraft/issues) (check that it hasn't already been reported first please)

### Download the mod from modrinth!
BeatCraft can be downloaded from here: [https://modrinth.com/mod/beatcraft](https://modrinth.com/mod/beatcraft)  
or from releases here on GitHub.  
> made for fabric 1.21.1  

Mod Dependencies: (these will be auto-downloaded if you get the mod with the modrinth app)
- [Vivecraft](https://modrinth.com/mod/vivecraft)
- [GeckoLib](https://modrinth.com/mod/geckolib)
- [owo-lib](https://modrinth.com/mod/owo-lib)
- [fabric-api](https://modrinth.com/mod/fabric-api)

# Feature roadmap

- [x] Load Beatmap V2, V3, and V4
- [x] Color notes, bombs, chains, arcs, walls
- [x] Accurate spawning animations
- [ ] Accurate scoring
- [x] AnimateTrack, AssignPathAnimation events
- [ ] AssignPlayerToTrack (yes, separating hands and head stuff too), and AssignTrackParent events.
- [x] V2 lightshows
- [ ] V3 lightshows
- [ ] maybe V4 lightshows
- [x] Chroma coloring features
- [x] Ingame song selection menu
- [x] Ingame Beatsaver downloader
- [ ] Level modifiers (song speed, ghost notes, etc.)


# How to play

It's recommended to create a new world with creative + cheats, using the BeatCraft world generation preset.  
You should spawn in the play area with a pair of sabers.  
If you break the play area or lose your sabers, refer to [commands](#commands)


# Technical features

## Blocks and Items

### Sabers
from the creative inventory, sabers will appear black.  
to fix this there are 2 item components that modify the saber colors:  
`beatcraft:saber_color`:  
> a packed color integer. the base 10 equivelent to a hexadecimal value. (ei. 0xFFFFFF, which would be white, is 16777215 in base 10)  

`beatcraft:sync_color`:  
> can be -1, 0, or 1  
> -1: saber will always render with the color specified by `beatcraft:saber_color`  
> 0: when in a map, the saber will switch to the left note color  
> 1: when in a map, the saber will switch to the right note color  

### Decorative block set
a set of blocks so you can build environments that feel closer to beat saber.  

Black Mirror Block  
> has no relation to black mirror.  
> just a solid black block.  
> at some point, I'd like this block to be reflective either with shaders or some other magic.  

Light Tile BLocks  
> Look kinda glowy.  
> Comes in many shapes.  

## World Gen
There is a new `BeatCraft` worldgen preset that generates an empty world with a layer of barriers to stand on.  
Apon joining a new world for the first time:
- you will be teleported to 0 0 0
- you will be given a pair of sabers
- the time will be set to midnight
- difficulty will be set to peaceful
- gamerule DoDaylightCycle will be set to false

## Commands

### /sabers
> gives you sabers that default to the iconic red and blue of beat saber, but will also auto-sync to the current map's color scheme.  

### /playarea
> generates the default pillar and runway for playing.  

### /fpfc <"enable"|"disable">
> enables/disables FPFC mode, alowing you to interact with the menu while not in VR.

### /color_helper
`/color_helper hex <hex_code>`  
> converts a color in the form RRGGBB to several formats. useful for giving yourself a saber with a specific color.  

`/color_helper intRGB <r> <g> <b>`  
> converts 3 ints (0 to 255) to several formats. useful for giving yourself a saber with a specific color.  

`/color_helper floatRGB <r> <g> <b>`  
> converts 3 floats (0.0 to 1.0) to several formats. useful for giving yourself a saber with a specific color.  

### /song

> [!WARNING]
> some sub-commands of `/song` may cause bugs as they are being phased out.  
> they are still included mainly for debugging.  

`/song play [beat]`
> resumes the currently playing song if beat is not provided, otherwise plays from the specified beat.

`/song pause`
> pauses the song. This works differently from the pause button in the way that this doesn't bring up the pause screen, and you can still see and interact with the notes and stuff.

`/song restart`
> restarts the map from the beginning

`/song speed <scalar|"reset">`  
> "reset" sets the speed to 1.0. otherwise speed is set to the specified value as long as it's between 0.0001 and 5.0

`/song unload`
> [!WANRING]
> unloads the current map.  
> running this is likely to break/softlock the mod.

`/song loadFile`
> [!WARNING]
> loads a beatmap difficulty from an absolute path to a file.  
> running this may crash the game, or the HUD may not work correctly.

`/song scrub <beats>`  
> moves forward or backwards in the map by however many beats are specified.

`/song load <map> <set> <difficulty>`
> [!WARNING]
> loads a song from the beatmaps folder. This command was replaced with the song selection menu, and using the command will not properly update the HUD.

`/song record <output_file>`
> [!WARNING]
> specify a file name (i.e. "recording.json") that the next map you play will be saved to.  
> this feature is currently buggy and not recommended for use.

`/song replay <replay_file>`
> [!WARNING]
> specify a file name (i.e. "recording.json") to replay.  
> currently very broken.

## Multiplayer
> [!WARNING]
> All multiplayer features may be very buggy, so please report bugs [here](https://github.com/Swifter1243/BeatCraft/issues)

Currently, multiplayer lets you and your friends take turns playing maps in the same world.  
