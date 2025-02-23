# THIS SHIT IS STILL WORK IN PROGRESS.

About 3 years ago (2021) I made a [reddit post](https://www.reddit.com/r/Minecraft/comments/l4w7of/working_on_making_beat_saber_in_minecraft) showing off my attempt at making Beat Saber in Minecraft. It got all sorts of attention and people really wanted to play it. There was only one problem..

# IT _SUCKED_

It was using:
- A scuffed dev build of Vivecraft
- A resource pack
- A datapack (commands and stuff)
- A unity project to generate the resource pack and datapack

Not only was this a *terrible* approach, but the code itself wasn't even good at all or nearly maintainable. I was scared of properly developing it/didn't know how, so I tried to use as many vanilla features as possible. The workarounds were creative and cool, but it was just horrifying and didn't perform well.

God knows where any of that stuff is anymore. I definitely don't wanna upload it because it probably contains the Minecraft codebase. Also there was no game loop, you could only hit blocks. So nobody should bother trying to get it from me.

# RUN THAT BACK

I'm *currently* in the process of remaking it EXCLUSIVELY as a [Vivecraft](https://modrinth.com/mod/vivecraft) dependant mod for Fabric. The current roadmap is:
- Load Beatmap V2, V3, and V4
- Color notes, bombs, chains, arcs, walls
- Accurate spawning animations
- Accurate scoring
- AnimateTrack, AssignPathAnimation, AssignPlayerToTrack (yes, separating hands and head stuff too), and AssignTrackParent events.
- V2 lightshow, MAAAAAAAAYBE V3 (and maybe V4)
- Chroma coloring features
- Ingame song selection menu
- Ingame Beatsaver downloader
- Level modifiers (song speed, ghost notes, etc.)

This is quite an insane undertaking so wish me luck o7

---
If you'd like to try out what I have at any point, clone the repo and run the project with [IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows) (scroll down to get community edition, which is free)

# WHY DON'T YOU MAKE IT FOR FORGE TOO? ARE YOU A LITTLE *BITCH*?

ion wanna

# How do I actually *play?*

Get the mod from Modrinth (coming SOON™) or the releases page here on GitHub

---
go to `X: 0, Y: 0, Z: 0`. Face towards `positive Z (south)`  


get sabers: `/sabers`  


The `/song` command:  

`/song load <song> <difficulty_set> <difficulty>` loads a beatmap difficulty (V2, V3, or V4)  
i.e. `/song load "Beat Saber" Standard ExpertPlus`

`/song play [beat]`  
`/song pause`  
`/song speed reset|<fraction greater than 0>`  
`/song unload`  
`/song loadFile <path_to_difficulty_dat_file>`

for custom sabers:
`/give @s beatcraft:saber[beatcraft:saber_color=<PACKED_COLOR>]`  
you can use `/color_helper` to convert from rgb, normalized rgb, or hex, to a packed color int  
