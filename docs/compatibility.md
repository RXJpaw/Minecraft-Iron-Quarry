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

With [Iris](https://modrinth.com/mod/iris) and a shader pack, the Blueprint outline can be harder to see in bright daylight. Some shader packs also affect the [Quarry Monitor](quarry-monitor.md#in-the-world): close to the quarry, the highlighted quarry can flicker or appear in front of the item in your hand.

## Mod Menu

On Fabric, [Mod Menu](https://modrinth.com/mod/modmenu) adds a settings button for the [client config](configuration.md#client-config).

---

[← Data Packs and Tags](data-packs.md) · [Overview](README.md)
