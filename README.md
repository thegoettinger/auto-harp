# Auto Harp

A Fabric mod that automatically plays Melody's Harp in Hypixel SkyBlock.

## ⚠️ Warning
This mod automates slot actions in a Hypixel GUI. That breaks Hypixel's rules,
and **using it can get your account banned**. Use at your own risk.

## Requirements
- Minecraft 26.1.2
- Fabric Loader 0.19.3 or newer
- Fabric API

## Installation
1. Download the latest `auto-harp-x.x.x.jar` from [Releases](../../releases).
2. Put it in your `mods` folder together with Fabric API.
3. Open Melody's Harp with an empty cursor and start a song. The mod plays notes
   using the drop-one action (Q) on quartz slots instead of picking them up.
   Only the seven playable keys in row five (slots 37–43) of a six-row
   `Harp - …` menu are used, not falling-note rows or your player inventory.
   Remove older Auto Harp jars before installing this version.

## Drop-action fork
This fork uses `ContainerInput.THROW` with button `0` (drop one item), not
`PICKUP`. Notes are tracked from server slot updates and full container updates,
not the locally predicted inventory after throwing an item.

A key is consumed once per quartz appearance. Repeated quartz updates, full
resyncs of the same note, and empty-slot updates do not rearm it. Only a nonempty,
non-quartz server replacement rearms that key for the next note. This avoids
local disappearance/restoration being mistaken for another note. A server that
never sends a background replacement between two notes cannot distinguish them
with this strategy; the mod deliberately does not retry the old note.

Actions run immediately after the server update is applied on the client thread.
Pending notes can wait for an empty cursor, but are cancelled if the server
replaces the note first. There is no global cooldown, so separate keys and chords
remain independent. This does not reduce network latency or guarantee that the
server will accept the action.

## Tests
`./gradlew test` runs regression tests for duplicate updates, empty-slot
restoration, consecutive notes, chords, slot filtering, and menu-state resets.

## Building from source
Requires JDK 25. Run `./gradlew build`, and the jar is in `build/libs/`.