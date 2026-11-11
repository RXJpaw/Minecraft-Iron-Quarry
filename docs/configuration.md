# Configuration

Iron Quarry has two config files in the `config/iron_quarry/` folder of the game or server. `server.jsonc` holds the balancing: quarry stats, dimensions, augments, expansion cards, monitors and drills. It is used by the server, and in single player by your game for all your worlds. `client.jsonc` holds each player's own settings for sounds, particles and the monitor display.

Both files are created with the default values when the game starts for the first time. Each option has a comment in the file that explains it and shows its default value and allowed range.

## How the files are read

`server.jsonc` is read when the server or single player world starts, and again on `/reload`, so changes work without a restart. It is sent to every player who joins and again after each `/reload`. Players on a server always see the server's values in tooltips, whatever their own file says.

`client.jsonc` is read when the game starts. Its options can also be changed in-game, see [Client config](#client-config).

Mistakes in a file don't break the game:

- A missing option is added back with its default value.
- A value that is out of range is moved into the range, with a warning in the log.
- A wrong value is replaced by the default, with a warning in the log.
- An unknown option is reported in the log, with a suggestion if it looks like a typo.

To reset an option, delete it from the file. It comes back with its default value.

## For modpack makers: what players can see

The player pages of this documentation show the default values and point players to the places where the game shows the real ones: tooltips, the locks in the quarry screen, warnings and recipe viewers. The [list on the overview page](README.md#playing-a-modpack-or-on-a-server) shows where each value appears. So most of your changes reach players without any extra work.

These changes are not visible in the game until a player runs into them, so you may want to mention them in your modpack's notes:

- the `dimensions` list (players only see a warning once a quarry stops),
- loot pools of the Chest Looting Augment and the Gilbert Pattern Expansion Card,
- villager trades that you turned off, moved or repriced,
- blocks you added to the `iron_quarry:ignored_by_quarry` tag ([Data Packs and Tags](data-packs.md#blocks)).

## Server config

### `quarryStats`

One entry per quarry tier: `copperQuarry`, `ironQuarry`, `goldQuarry`, `diamondQuarry`, `netheriteQuarry`, `netherStarQuarry`.

| Option | Meaning |
|---|---|
| `augmentLimit` | Usable augment slots, 0 to 6. |
| `expansionLimit` | Usable expansion slots, 0 to 6. |
| `ticksPerOperation` | Ticks needed to mine one block, before augments. 20 ticks = 1 second. |
| `energyCapacity` | Energy storage in RF. |
| `baseConsumption` | RF per tick while mining, before augments. One block costs `baseConsumption × ticksPerOperation`. |

Defaults:

| Tier | `augmentLimit` | `expansionLimit` | `ticksPerOperation` | `energyCapacity` | `baseConsumption` |
|---|---|---|---|---|---|
| `copperQuarry` | 0 | 1 | 40 | 20,000 | 20 |
| `ironQuarry` | 1 | 2 | 30 | 100,000 | 40 |
| `goldQuarry` | 2 | 3 | 20 | 500,000 | 90 |
| `diamondQuarry` | 3 | 4 | 10 | 2,500,000 | 270 |
| `netheriteQuarry` | 4 | 5 | 5 | 10,000,000 | 810 |
| `netherStarQuarry` | 6 | 6 | 2 | 50,000,000 | 3,040 |

If you lower `augmentLimit` or `expansionLimit`, items in slots that become locked stop working (see [The quarry screen](quarries.md#the-quarry-screen)).

### `dimensions`

Decides in which dimensions quarries may mine. This applies to the area of the Blueprint, not to where the quarry stands.

| Option | Meaning |
|---|---|
| `mode` | `"blacklist"`: quarries mine everywhere except in the listed dimensions. `"whitelist"`: quarries only mine in the listed dimensions. Default: `"blacklist"`. |
| `list` | Dimension IDs like `"minecraft:the_end"`, or `"somemod:*"` for every dimension of a mod. Default: empty. |

No mining in the End and in any dimension of a mod called `somemod`:

```jsonc
"dimensions": {
  "mode": "blacklist",
  "list": ["minecraft:the_end", "somemod:*"]
}
```

Mining only in a special mining dimension:

```jsonc
"dimensions": {
  "mode": "whitelist",
  "list": ["somemod:mining_world"]
}
```

A quarry with a Blueprint from a forbidden dimension stops with the red warning `⚠ Mining is disabled in the blueprint's dimension`. An unknown `mode` falls back to `"blacklist"`, and invalid entries are reported in the log.

### `augments`

#### `augments.speed`

| Option | Default | Meaning |
|---|---|---|
| `disabled` | `false` | Turns the augment off: it has no effect, is hidden from the creative inventory and can't be filled at the Smithing Table. |
| `baseAmount` | `1000` | Points an augment holds without capacity upgrades. Capacity upgrades raise it, see [Capacity upgrades](augments.md#capacity-upgrades). |
| `maxSpeedBonus` | `15.0` | Speed factor of a completely filled augment with all capacity upgrades. |
| `maxEnergyBonus` | `45.0` | Speed energy factor of a completely filled augment with all capacity upgrades. |
| `energyRatioExponent` | `1.5` | Shape of the energy cost curve. Above 0, partly filled augments cost less energy per speed than full ones, and the higher the value, the bigger the saving. |
| `speedFactorLimit` | `93.71293` | Highest total speed factor of a quarry. The default is 1 + 6 full augments + Efficiency V. |

#### `augments.fortune`

| Option | Default | Meaning |
|---|---|---|
| `disabled` | `false` | Turns the augment off, like for speed. |
| `baseAmount` | `1500` | Points an augment holds without capacity upgrades. |
| `maxLevel` | `4.7` | Fortune level of a completely filled augment with all capacity upgrades. Fractions give a chance for one more level. |
| `maxEnergyBonus` | `2.0` | Fortune energy factor of a completely filled augment with all capacity upgrades. |
| `levelLimit` | `4.7` | Highest total fortune level of a quarry. |

#### `augments.silkTouch`

| Option | Default | Meaning |
|---|---|---|
| `disabled` | `false` | Turns the augment off: it has no effect, is hidden from the creative inventory and isn't sold by villagers. |
| `slowness` | `2.0` | The quarry's speed is divided by this value. |
| `tradingEnabled` | `true` | Lets Toolsmiths and Wandering Traders sell the augment. Only checked when a villager's trades are created; existing trades stay. |

#### `augments.chestLooting`

| Option | Default | Meaning |
|---|---|---|
| `disabled` | `false` | Turns the augment off: it has no effect, is hidden from the creative inventory and no longer appears in chest loot. |
| `lootPools` | see [Augments](augments.md#chest-looting-augment) | Loot tables that can contain the augment, see [Loot pools](#loot-pools). |

### `augmentingTemplate`

| Option | Default | Meaning |
|---|---|---|
| `durability` | `18300` | Augment points an Augmenting Template can add before it breaks. Minimum 1. |
| `capacityUpgradeCost` | `1` | Durability one capacity upgrade uses. |

The Celestial Augmenting Template never breaks, no matter what you set here.

### `expansions`

| Option | Default | Meaning |
|---|---|---|
| `interdimensional.disabled` | `false` | Turns the Interdimensional Expansion Card off: it has no effect, is hidden from the creative inventory and can't be crafted. Quarries then only mine in their own dimension. |
| `snakePattern.tradingEnabled` | `true` | Lets Cartographers sell the Snake Pattern Expansion Card. Only checked when a villager's trades are created. |
| `gilbertPattern.lootPools` | Ancient City and End City treasure, 5% each | Loot tables that can contain the Gilbert Pattern Expansion Card, see [Loot pools](#loot-pools). An empty list removes it from all loot. |

### `quarryMonitor`

One entry per monitor: `ironMonitor`, `diamondMonitor`, `netheriteMonitor`, `sculkMonitor`.

| Option | Meaning |
|---|---|
| `range` | Horizontal distance in blocks up to which the monitor reaches its quarry. In other dimensions, coordinates are scaled like portals. |
| `interdimensional` | Whether the monitor reaches quarries in other dimensions. |

| Monitor | `range` | `interdimensional` |
|---|---|---|
| `ironMonitor` | 1,024 | `false` |
| `diamondMonitor` | 4,096 | `false` |
| `netheriteMonitor` | 16,384 | `true` |
| `sculkMonitor` | 65,536 | `true` |

### `quarryDrillStats`

One entry per drill: `copperDrill`, `ironDrill`, `diamondDrill`, `netheriteDrill`, `shulkerDrill`, `netherStarDrill`.

| Option | Meaning |
|---|---|
| `durability` | Blocks the drill can mine before it breaks. Unbreaking makes it last longer. |
| `enchantability` | Like on vanilla tools: higher gives better enchantments. |
| `toolEquivalent` | ID of the tool whose mining rules the drill copies, for example `"minecraft:iron_pickaxe"`. This decides which blocks drop items. Tools from other mods work too. |

| Drill | `durability` | `enchantability` | `toolEquivalent` |
|---|---|---|---|
| `copperDrill` | 649 | 2 | `minecraft:copper_pickaxe` |
| `ironDrill` | 1,284 | 5 | `minecraft:iron_pickaxe` |
| `diamondDrill` | 6,649 | 4 | `minecraft:diamond_pickaxe` |
| `netheriteDrill` | 93,750 | 5 | `minecraft:netherite_pickaxe` |
| `shulkerDrill` | 159,375 | 7 | `minecraft:netherite_pickaxe` |
| `netherStarDrill` | 796,875 | 9 | `minecraft:netherite_pickaxe` |

An unknown `toolEquivalent` falls back to the default tool and is reported in the log. An item that isn't a tool only gets drops that need no tool. For a tool from another mod, the drill's tooltip needs a name for the mining level. Add a language entry like `"mining_level.iron_quarry.somemod:void_pickaxe": "Void"` with a resource pack.

### Loot pools

`augments.chestLooting.lootPools` and `expansions.gilbertPattern.lootPools` are lists of entries like this:

```jsonc
{
  "lootTable": "minecraft:chests/ancient_city", // ID of the loot table
  "rollsMin": 1.0,                              // least rolls of the pool
  "rollsMax": 1.0,                              // most rolls of the pool
  "chance": 0.05                                // chance from 0 to 1 that the pool is rolled at all
}
```

Each entry adds one pool to that loot table. Loot tables from other mods work as well.

## Client config

These options can be changed in-game. On Fabric, this needs [Mod Menu](https://modrinth.com/mod/modmenu): open Mods, select Iron Quarry and click the settings button. On NeoForge, open Mods, select Iron Quarry and click Config.

| Option | Default | Meaning |
|---|---|---|
| `blockBreaking.volume` | `1.0` | Volume of the block breaking sounds of quarries, 0 to 1. |
| `blockBreaking.distance` | `64.0` | Up to this distance in blocks you see particles and hear sounds of quarries, 0 to 64. |
| `quarryMonitorOverlay.alignment` | `"bottom_left"` | Screen corner of the [monitor display](quarry-monitor.md#monitor-display): `top_left`, `top_right`, `bottom_left` or `bottom_right`. |
| `quarryMonitorOverlay.x` | `8` | Horizontal distance from the screen edge, 0 to 128 pixels. |
| `quarryMonitorOverlay.y` | `8` | Vertical distance from the screen edge, 0 to 128 pixels. |

> [!IMPORTANT]
> Edit `client.jsonc` by hand only while the game is closed, because the in-game settings screen overwrites the file.

---

[← Crafting Recipes](crafting.md) · [Overview](README.md) · [Data Packs and Tags →](data-packs.md)
