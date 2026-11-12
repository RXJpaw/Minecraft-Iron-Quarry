# Drills

The drill is the quarry's pickaxe. It decides which blocks drop items, and it wears down a little with every block. Without a drill, the quarry does nothing.

Every drill works in every quarry tier. A better drill doesn't make the quarry faster by itself, but it lasts much longer and gets drops from harder ores.

## The six drills

The Shulker Drill is crafted from a Netherite Drill, and the Nether Star Drill from a Shulker Drill. A drill crafted this way is new: enchantments and remaining durability of the old one are lost. All drill recipes are on the [Crafting Recipes](crafting.md#drills) page.

Default values:

| Drill | Mining level | Blocks before it breaks | Enchantability |
|---|---|---|---|
| Copper Drill | Copper | 649 | 2 |
| Iron Drill | Iron | 1,284 | 5 |
| Diamond Drill | Diamond | 6,649 | 4 |
| Netherite Drill | Netherite | 93,750 | 5 |
| Shulker Drill | Netherite | 159,375 | 7 |
| Nether Star Drill | Netherite | 796,875 | 9 |

> [!NOTE]
> Modpacks and servers can change these values, including the mining level (see [Configuration](configuration.md#drills)). A drill's tooltip shows its actual `Mining Level` and `Durability`.

## Mining level

A drill gets the same drops as the pickaxe of its mining level. The Copper Drill gets what a stone or copper pickaxe gets.

| Block | Copper | Iron | Diamond | Netherite |
|---|---|---|---|---|
| Stone, coal, iron, copper and lapis ore | yes | yes | yes | yes |
| Gold, diamond, emerald and redstone ore | no | yes | yes | yes |
| Obsidian, crying obsidian, ancient debris | no | no | yes | yes |

Blocks from other mods follow the same rules as for the matching pickaxe. If a modpack gives a drill the mining level of a tool from another mod, the drill gets the drops of that tool.

> [!WARNING]
> A block the drill isn't good enough for is still removed, but drops nothing. Check that your drill can handle the ores in your area before you start.

## Durability

Every mined block costs one point of durability. Water and lava cost nothing. When the durability runs out, the drill breaks and disappears, and the quarry stops with the warning `⚠ Drill slot empty`. To replace drills automatically, feed new ones in through a pipe on a red side (see [Side settings](quarries.md#side-settings-io)).

## Enchantments

Drills can be enchanted at an Enchanting Table or with books on an Anvil. Two enchantments work on them:

- **Unbreaking** makes the drill last longer, like on a normal tool.
- **Efficiency** makes the quarry faster. Levels I to V add 0.30, 0.69, 1.20, 1.86 or 2.71 to the quarry's speed factor and the same amount to its speed energy factor (see [Energy factors](augments.md#energy-factors)). The quarry then uses more energy per tick, but it also mines faster by the same factor, so a block doesn't cost more energy than before.

---

[← Blueprints](blueprints.md) · [Overview](README.md) · [Augments →](augments.md)
