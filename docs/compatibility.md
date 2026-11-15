# Compatibility

## Minecraft and loaders

Iron Quarry runs on Minecraft 26.1.2 with Fabric or NeoForge. It works in single player and on dedicated servers. It must be installed on both the server and every client. On Fabric it needs [Fabric API](https://modrinth.com/mod/fabric-api); the energy library (Team Reborn Energy) is included in the mod. On NeoForge it needs nothing else.

## Energy

Iron Quarry itself doesn't produce energy. You need a mod with generators.

On Fabric, any mod that uses Team Reborn Energy works, which most Fabric tech mods do. On NeoForge, any mod that uses NeoForge's energy (FE) works.

How the quarry takes in energy is explained under [Energy](quarries.md#energy).

## Pipes and item transport

Pipes, conveyors and other item transport mods can use the quarry's sides. You choose which inventory a side connects to in the quarry screen (see [Side settings](quarries.md#side-settings-io)).

## Recipe viewers

JEI and REI show all recipes of the mod, including the Smithing Table recipes for augments and Blueprints. Unsealing a Blueprint on the Grindstone is shown as a recipe in JEI and as an info page in REI.

## Lootr

Iron Quarry supports [Lootr](https://modrinth.com/mod/lootr) 1.22.36.108 or newer. How the [Chest Looting Augment](augments.md#chest-looting-augment) handles Lootr chests is explained on the Augments page.

## Land claims and protection

Before a quarry breaks a block, it uses the loader's normal block-breaking events to ask claim and protection mods whether its owner may break that block. So mods that protect an area against players also protect it against other players' quarries. See [Ownership and land claims](quarries.md#ownership-and-land-claims).

## Shaders

Shader packs for [Iris](https://modrinth.com/mod/iris) can change how the Blueprint outline and the Quarry Monitor look. Iron Quarry fixes two of these problems itself: while a shader pack is in use, it draws the outline and the monitor's preview a little differently. You can change both in the [client config](configuration.md#client-config), under Blueprint Preview and Quarry Monitor Preview.

### Blueprint outline

Normally the parts of the [Blueprint outline](blueprints.md#seeing-the-area) that are hidden behind blocks are drawn fainter than the rest. This shows you which edges are covered and which are not. Many shader packs draw all parts equally strong, so you can't tell them apart anymore. While a shader pack is in use, the hidden parts are drawn as dashed lines instead.

With some shader packs the outline is also hard to see in bright daylight. There is no setting for that.

### Quarry Monitor

While you hold a [Quarry Monitor](quarry-monitor.md#in-the-world), it shows a preview of its quarry in the world. When you stand near the quarry, the preview is drawn exactly on the placed quarry. With many shader packs it then flickers, because of their temporal anti-aliasing (TAA). While a shader pack is in use, the preview is drawn a tiny bit closer to you instead, which stops the flickering.

With some shader packs the preview can also appear in front of the item in your hand. There is no setting for that.

## Mod Menu

On Fabric, [Mod Menu](https://modrinth.com/mod/modmenu) adds a settings button for the [client config](configuration.md#client-config).

---

[← Data Packs and Tags](data-packs.md) · [Overview](README.md)
