# Iron Quarry Documentation

Iron Quarry adds quarries: machines that mine a selected area block by block. You mark the area with a Blueprint, put a drill into the quarry, connect an energy source, and it starts digging. Everything it mines ends up in its own inventory.

If you are new to the mod, start with [Getting Started](getting-started.md).

This documentation is written for Iron Quarry 0.1.0 on Minecraft 26.1.2.

## Contents

For players:

- [Getting Started](getting-started.md): your first quarry, step by step
- [Quarries](quarries.md): the six tiers, the quarry screen, energy, item output and side settings
- [Blueprints](blueprints.md): marking the area a quarry mines
- [Drills](drills.md): mining levels, durability and enchantments
- [Augments](augments.md): Speed, Fortune, Silk Touch and Chest Looting
- [Expansion Cards](expansion-cards.md): mining in other dimensions and mining patterns
- [Quarry Monitor](quarry-monitor.md): watching a quarry from far away
- [Warnings and Troubleshooting](warnings.md): what each warning means and how to fix it
- [Crafting Recipes](crafting.md): all default recipes

For server owners and modpack makers:

- [Configuration](configuration.md): all options of the server and client config
- [Data Packs and Tags](data-packs.md): changing items, blocks, trades and recipes
- [Compatibility](compatibility.md): energy, pipes, recipe viewers, Lootr and claim mods

## Playing a modpack or on a server

This documentation describes the mod with its default settings. Modpacks and servers often change numbers such as speed, energy use, slot counts, ranges and durability. They may also change recipes, villager trades, loot and the items that fill augments, or turn some items off. The mechanics stay the same. Only the values differ.

The game always shows the values that apply to you:

- Energy use, speed and fortune of a quarry: the tooltip of the quarry item, or the info icon in the quarry screen.
- Energy capacity of a quarry: the quarry tooltip while holding Shift, or the energy bar in the quarry screen.
- Usable augment and expansion slots: slots that a quarry can't use show a lock in the quarry screen.
- Mining level and durability of a drill: the drill's tooltip.
- Strength, energy cost and fill level of an augment: the augment's tooltip.
- Capacity upgrade items: the augment's tooltip while holding Shift. Empty slots cycle through the accepted items.
- Durability of an Augmenting Template and range of a Quarry Monitor: their tooltips.
- Recipes and the items that fill augments: your recipe viewer (JEI or REI). Press R on an item to see its recipe, U to see what it is used for.
- Speed and fortune limits: when a quarry goes above a limit, the warning names it.

An item that shows `<This item is disabled by server config>` in its tooltip has been turned off. It does nothing and can no longer be crafted, filled, bought or found. Turned-off items are also missing from the creative inventory.

## Terms

**RF** is the energy unit of the mod. Other mods call the same energy FE or E. **RF/t** means energy per tick; a tick is 1/20 of a second.

An **operation** is one mined block, or one item taken out of a container by the [Chest Looting Augment](augments.md#chest-looting-augment). The game shows a quarry's speed in operations per second (op/s).

A **chunk** is a 16 × 16 block column of the world, from the bottom to the top. Press F3 + G to see chunk borders.

Many items show more details in their tooltip while you hold **Shift**.
