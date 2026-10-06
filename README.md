# Spatial GUI

**A client-side Minecraft mod that turns screens into immersive 3D displays, with fully configurable first-person and third-person modes.**
___
## Features

### First-person Mode
![First-person menu crafting GIF](https://cdn.modrinth.com/data/cached_images/961b53d75867806bf45fb6f7df0e5e34b242b322.gif)

*   Crosshair and mouse modes
*   Screen position, distance, scale, and rotation are all configurable
*   Parallax effect in mouse mode
*   Configurable screen opening animation
*   Curved screen option, especially good with mods that add large GUI extensions, such as JEI

### Third-person Mode
![third-person screenshot](https://cdn.modrinth.com/data/cached_images/74d080ca00474431a2d1fed3da972c82ef43b7c1.png)

*   Camera and screen position, distance, scale, and rotation are all configurable
*   Parallax effect
*   Smooth camera transition and configurable screen opening animation

## Dependencies

| Loader | Mod
| --- | --- |
| All | Cloth Config |
| Fabric | Fabric API & Mod Menu |

## Configuration

Access config via the keybind (default unbound), Mod Menu, or edit the config file.
It is highly recommended to check the config to tailor the mod to your liking, and there are tons of options.

## Compatibility

Spatial GUI is a client-side mod and does not need to be installed on the server.

However, this does **not** guarantee compatibility with every mod. Spatial GUI modifies how GUI screens and the camera are rendered, so mods that significantly change GUI rendering, screen layouts, camera behavior, mouse functionality, or related rendering systems may cause visual issues or unexpected behavior.

If you find a reproducible incompatibility with another mod, please report it on GitHub so it can be investigated. Reporting an issue helps make the mod better for everyone!

# FAQ
| Question | Answer |
| --- | --- |
| **Does Spatial GUI work in multiplayer?** | Yes. Other players and the server do not need to have Spatial GUI installed. |
| **Does Spatial GUI give a gameplay advantage?** | Spatial GUI is primarily visual and does not add or automate gameplay mechanics. Some settings can change how the camera behaves, so whether it is allowed on a particular server is ultimately up to that server's rules. |
| **Why can't I enable third-person mode?** | This is likely caused by Punchy, go to *Punchy's config -> Compatibility*, and turn off *Spatial GUI Support*. |
| **Is Spatial GUI compatible with _____?** | Probably! However, compatibility isn't guaranteed, especially with mods that modify GUIs, cameras, or rendering. If you encounter an issue with a specific mod, feel free to report it on GitHub. |
| **Why is my GUI behaving strangely?** | If other mods are installed, it may be a compatibility issue. It's usually GUI related but not always, try and locate the mod causing this and open an issue on GitHub if your issue isn't already reported. |
| **Can I use Spatial GUI in a modpack?** | Yes. Spatial GUI is welcome in modpacks and no additional permission is required. Compatibility with other mods is not guaranteed. |
| **Is Spatial GUI purely visual?** | Almost entirely. Spatial GUI changes how existing screens and the camera are presented. One setting intentionally allows your rotation to persist after closing a GUI, which can turn your character's view. |

## Notes
*   Feel free to suggest features or report bugs on the GitHub
*   This mod is absolutely welcome to be used in modpacks, just be careful about it's compatilbility with certain mods

### Special thanks to:
vibing for all of their pull requests  
Dizabanik for tons of code fixes and improvements  
BUILDLCS for the Brazilian Portuguese translation  
suheckii for the Russian translation  
22SSendo & jankeverse for particularly awesome feature suggestions  
Anyone else who reported bugs or suggested features :)
