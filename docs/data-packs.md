# Data Packs and Tags

Many parts of Iron Quarry can be changed with a data pack, without touching the config. This page lists what you can change. Numbers like quarry speed, energy and slot counts are set in the [config](configuration.md) instead.

If you have never made a data pack, the [Minecraft Wiki](https://minecraft.wiki/w/Data_pack) explains the basics. All file paths below are inside the `data/` folder of your data pack.

## Augment enhancers and capacity items

These item tags decide which items fill augments and which items are capacity upgrades.

| Tag | Default | Points per item |
|---|---|---|
| `iron_quarry:augment_speed_tier1_enhancers` | Redstone Dust | 1 speed point |
| `iron_quarry:augment_speed_tier2_enhancers` | Block of Redstone | 9 speed points |
| `iron_quarry:augment_speed_tier3_enhancers` | Core of Redstone | 81 speed points |
| `iron_quarry:augment_fortune_tier1_enhancers` | Lapis Lazuli | 1 fortune point |
| `iron_quarry:augment_fortune_tier2_enhancers` | Block of Lapis Lazuli | 9 fortune points |
| `iron_quarry:augment_fortune_tier3_enhancers` | Core of Lapis Lazuli | 81 fortune points |

The points per tier are fixed. To make an item worth more or less, move it to another tier tag.

| Tag | Default |
|---|---|
| `iron_quarry:augment_capacity_slot1_enhancers` | Block of Diamond |
| `iron_quarry:augment_capacity_slot2_enhancers` | Nautilus Shell |
| `iron_quarry:augment_capacity_slot3_enhancers` | Echo Shard |
| `iron_quarry:augment_capacity_slot4_enhancers` | Flow Armor Trim Smithing Template |
| `iron_quarry:augment_capacity_slot5_enhancers` | Enchanted Golden Apple |
| `iron_quarry:augment_capacity_slot6_enhancers` | Nether Star |
| `iron_quarry:augment_capacity_slot7_enhancers` | Deepslate Emerald Ore |
| `iron_quarry:augment_capacity_enhancers` | all seven slot tags; used by the recipe and the recipe viewers |

Things to know about the capacity slots:

- A slot tag can hold several items. Any one of them fills the slot.
- An item that is in several slot tags fills the first free one.
- A slot whose tag is empty can never be filled. Augments then can't reach their full strength.
- A filled slot stays filled when its item is later removed from the tag. Existing augments don't lose capacity.

For example, to accept a Block of Diamond or a Block of Emerald for slot 1, `iron_quarry/tags/item/augment_capacity_slot1_enhancers.json` would contain:

```json
{
  "replace": true,
  "values": ["minecraft:diamond_block", "minecraft:emerald_block"]
}
```

| Tag | Default | Meaning |
|---|---|---|
| `iron_quarry:augmenting_templates` | Augmenting Template, Celestial Augmenting Template | Templates that the augment recipes accept |

## Blocks

| Block tag | Default | Meaning |
|---|---|---|
| `iron_quarry:ignored_by_quarry` | `#minecraft:wither_immune` (bedrock, reinforced deepslate, end portal frames and more) | Quarries skip these blocks and leave them in place. |
| `iron_quarry:modifiable_without_break_check` | Lootr containers (only if Lootr is installed) | See below. |

About `modifiable_without_break_check`: before a quarry changes a block, it asks claim and protection mods whether its owner may break it. Some mods always say no to machines; Lootr, for example, does this for its chests. For blocks in this tag, the quarry doesn't ask. This only applies to blocks with stored data, like chests, which the quarry empties but never breaks.

Blocks that can't be broken in survival (like bedrock) are always skipped, whatever the tags say.

## Wrenches

Any item in the common tag `c:tools/wrench` works as a [wrench](quarries.md#side-settings-io) on quarries. Most wrenches of other mods are already in this tag. To add your own, put it into `c/tags/item/tools/wrench.json`.

## Villager trades

| Trade | Sold by (villager trade tag) | Turn off in config |
|---|---|---|
| `iron_quarry:silk_touch_augment` | `minecraft:toolsmith/level_3`, `minecraft:wandering_trader/uncommon` | `augments.silkTouch.tradingEnabled` |
| `iron_quarry:snake_pattern_expansion_card` | `minecraft:cartographer/level_5` | `expansions.snakePattern.tradingEnabled` |

- To change the price, override the trade file, for example `iron_quarry/villager_trade/silk_touch_augment.json`.
- To change who sells it, edit the villager trade tags above.
- The config switches only turn the default trades on or off. They are checked when a villager's trades are created. Trades a villager already has stay.

## Loot

The [Chest Looting Augment](augments.md#chest-looting-augment) and the [Gilbert Pattern Expansion Card](expansion-cards.md#mining-pattern-cards) are added to loot tables through the config, not through data packs. See [Loot pools](configuration.md#loot-pools).

## Recipes

All recipes are in `iron_quarry/recipe/` and can be overridden or removed like any other recipe. Some use special recipe types of the mod:

| Recipe type | Used for |
|---|---|
| `iron_quarry:handled_crafting_recipe` | A shaped recipe that copies everything stored in one ingredient to the result. Used to upgrade quarries and monitors. The field `"source"` names the key of that ingredient, which must appear exactly once in the pattern. |
| `iron_quarry:smithing_augment` | Fills an augment with enhancers. |
| `iron_quarry:smithing_augment_capacity` | Adds a capacity upgrade to an augment. |
| `iron_quarry:smithing_blueprint_seal` | Seals a Blueprint. |

The upgrade recipe of the Iron Quarry (`iron_quarry/recipe/quarry_iron.json`, shortened) as an example:

```json
{
  "type": "iron_quarry:handled_crafting_recipe",
  "pattern": ["BCB", "CMC", "BCB"],
  "key": {
    "M": "iron_quarry:copper_quarry",
    "C": "iron_quarry:circuit_mk2",
    "B": "#c:storage_blocks/iron"
  },
  "source": "M",
  "result": { "id": "iron_quarry:iron_quarry" }
}
```

> [!TIP]
> Turning off an augment or the Interdimensional Expansion Card in the [config](configuration.md#augments) already disables its recipes; no data pack is needed for that.

---

[← Configuration](configuration.md) · [Overview](README.md) · [Compatibility →](compatibility.md)
