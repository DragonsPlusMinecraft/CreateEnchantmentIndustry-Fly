# Create: Enchantment Industry Fly 2.5.1 — Modpack Notes

This document describes the Fabric/Create Fly build for Minecraft 26.1.2. It does not describe the NeoForge-only Apothic Enchanting or Apotheosis integrations, and no Sable integration is available for this target.

## Tags

### Enchantment

* `create_enchantment_industry:blaze_enchanter/enchanting` controls regular Blaze Enchanter choices and includes `minecraft:in_enchanting_table` by default.
* `create_enchantment_industry:blaze_enchanter/enchanting_exclusive` adds entries only to regular enchanting.
* `create_enchantment_industry:blaze_enchanter/super_enchanting` controls Super Enchanting choices.
* `create_enchantment_industry:blaze_enchanter/super_enchanting_exclusive` adds entries only to Super Enchanting; the generated tag includes treasure enchantments and excludes curses.
* `create_enchantment_industry:blaze_enchanter/penalty_curses` is the allow-list for curses applied when Super Enchanting lightning is blocked.
* `create_enchantment_industry:blaze_enchanter/penalty_curses_deny` removes entries from that penalty pool and takes precedence over the allow-list.
* `create_enchantment_industry:printer/deny` prevents matching enchantments from being copied onto Enchanted Books.

For example, a datapack can add a penalty curse without replacing generated defaults:

```json
{
  "replace": false,
  "values": [
    "examplemod:custom_curse"
  ]
}
```

The file path is `data/create_enchantment_industry/tags/enchantment/blaze_enchanter/penalty_curses.json`. A penalty entry must also be in `minecraft:curse`, must not be denied, and must support the target item unless the target is a Book.

### Fluid

* `create:bottomless/deny` contains Liquid Experience so Create does not treat it as a bottomless fluid.

### Item

* `c:buckets` includes Bucket o' Enchanting.
* `c:nuggets` includes Nugget of Super Experience.
* `c:storage_blocks` includes Block of Super Experience.
* `create:upright_on_belt` includes Cake Base o' Enchanting and Cake o' Enchanting.

### Block and point of interest

* `create:fan_transparent` and `create:fan_processing_catalysts/smoking` include Blaze Enchanter, Blaze Forger, and Classic Blaze Enchanter.
* `minecraft:mineable/pickaxe` includes CEI machinery.
* `minecraft:beacon_base_blocks` and `c:storage_blocks` include Block of Super Experience.
* `c:lightning_rods` for blocks marks targets that can redirect CEI-created lightning into an adjacent Experience Block.
* `c:lightning_rods` for point-of-interest types lets compatible rod POIs attract CEI-created lightning efficiently.

For block-only lightning-rod compatibility, add the block to `data/c/tags/block/lightning_rods.json`. A state using the vanilla lightning-rod `facing` property targets the block opposite that direction; other tagged blocks target the block immediately below. Adding a matching POI type is optional because the block-tag fallback remains available.

## Data Maps

CEI loads 14 resource-pack data-map types. Files use this layout:

```text
data/create_enchantment_industry/data_maps/<registry>/<map path>.json
```

Each root object accepts `replace`, `values`, and `remove`. Keys in `values` and `remove` can be registry IDs or `#tag` IDs. Higher-priority packs merge over lower-priority packs unless `replace` is true; `remove` is applied after `values`. Value wrappers may use Fabric resource conditions. Legacy Forge/NeoForge condition wrappers are also read for datapack compatibility, but this mod remains a Fabric mod.

### Experience Fuel

`create_enchantment_industry:experience_fuel` maps an item to normal or Super Experience fuel. Its file is `data/create_enchantment_industry/data_maps/item/experience_fuel.json`.

```json
{
  "values": {
    "examplemod:experience_shard": 3,
    "examplemod:condensed_experience": {
      "experience": 27,
      "special": true,
      "using_convert_to": {
        "id": "minecraft:glass_bottle"
      }
    }
  }
}
```

The integer shorthand creates normal fuel. In the object form, `experience` is positive, `special` defaults to false, and `using_convert_to` is optional.

### Fluid Experience Units

`create_enchantment_industry:unit/experience` maps a fluid or fluid tag to the positive amount of experience represented by one mB. Its file is `data/create_enchantment_industry/data_maps/fluid/unit/experience.json`.

```json
{
  "values": {
    "examplemod:liquid_xp": 20
  }
}
```

### Printer Ingredients and Styles

The six positive-integer fluid maps below select fluids and mB costs for built-in Printer behaviours:

* `create_enchantment_industry:printing/address/ingredient`
* `create_enchantment_industry:printing/pattern/ingredient`
* `create_enchantment_industry:printing/copy/ingredient`
* `create_enchantment_industry:printing/custom_name/ingredient`
* `create_enchantment_industry:printing/written_book/ingredient`
* `create_enchantment_industry:printing/banner_pattern/ingredient`

Example for `data/create_enchantment_industry/data_maps/fluid/printing/copy/ingredient.json`:

```json
{
  "values": {
    "#c:dyes/black": 10,
    "create_enchantment_industry:experience": 25
  }
}
```

`create_enchantment_industry:printing/custom_name/style` maps fluids to Minecraft text styles:

```json
{
  "values": {
    "#c:dyes/red": {
      "color": "#FF0000"
    },
    "examplemod:royal_ink": {
      "color": "#663399",
      "bold": true
    }
  }
}
```

Banner-pattern printing additionally requires the fluid to resolve to a Minecraft `DyeColor`. Vanilla and Create: Dragons Plus dye fluids are supported; an unknown custom dye is rejected instead of silently becoming black.

### Enchanted Book Printing Costs

`create_enchantment_industry:printing/enchanted_book/custom_cost` maps an enchantment to level/cost pairs. Its file is under the `enchantment` registry directory.

```json
{
  "values": {
    "minecraft:mending": [
      {
        "level": 1,
        "value": 100
      }
    ]
  }
}
```

When no pair matches, the Printer uses its normal formula and server multiplier.

### Enchantment Processing Rules

`create_enchantment_industry:enchantment_processing/rules` controls per-enchantment level extensions and cost multipliers for Blaze Enchanter and Blaze Forger.

```json
{
  "values": {
    "minecraft:sharpness": {
      "level_extension": {
        "blaze_enchanter": 2,
        "blaze_forger": 4
      },
      "cost_multiplier": {
        "blaze_enchanter": {
          "normal": 1.0,
          "super": 1.5,
          "direct": 1.0,
          "template": 1.2
        },
        "blaze_forger": {
          "normal": 1.0,
          "super": 2.0,
          "merge": 1.0,
          "apply": 1.25,
          "extract": 0.75
        }
      }
    }
  }
}
```

Every field is optional. Missing level extensions use `blazeEnchanterMaxLevelExtension` or `blazeForgerMaxLevelExtension`; missing multipliers use `1.0`. Generated rules set the Mending and Infinity level extensions to zero.

The following three IDs remain supported as legacy compatibility maps. New packs should use `enchantment_processing/rules`:

* `create_enchantment_industry:forging/cost_multiplier`
* `create_enchantment_industry:forging/split_enchantment_cost_multiplier`
* `create_enchantment_industry:super_enchanting/custom_level_extension`

## Java Integration API

### Printer Behaviour Providers

Fabric addons can register custom Printer behaviours during mod initialization, before the first server starts and freezes provider order:

```java
PrintingBehaviour.register(
        Identifier.fromNamespaceAndPath(MOD_ID, "wax_seal"),
        MyPrintingBehaviour::create);
```

The default priority is `PrintingBehaviourRegistry.DEFAULT_PRIORITY`. Built-ins use `BUILTIN_PRIORITY`. A deliberate override can register with an explicit priority:

```java
PrintingBehaviour.register(
        Identifier.fromNamespaceAndPath(MOD_ID, "wax_seal_override"),
        PrintingBehaviourRegistry.BUILTIN_PRIORITY + 1,
        MyPrintingBehaviour::create);
```

IDs must be unique. Registration after the first server-start freeze throws an error. A provider returns:

* `Optional.empty()` when it does not claim the supplied template.
* `Optional.of(DataResult.success(behaviour))` when it accepts the template.
* `Optional.of(DataResult.error(...))` when it recognizes but rejects the template; this stops lookup.

Higher priority runs first; equal priority is ordered by provider ID for deterministic results. Recipe-based printing remains the final fallback.

## Recipes

### Printing

The recipe type is `create_enchantment_industry:printing`. It requires exactly two item ingredients, exactly one fluid ingredient, and exactly one item result. It can also be used as a Sequenced Assembly step.

```json
{
  "type": "create_enchantment_industry:printing",
  "ingredients": [
    "minecraft:wheat",
    "minecraft:cookie"
  ],
  "fluid_ingredients": [
    {
      "type": "fluid_stack",
      "amount": 250,
      "fluid": "create_enchantment_industry:experience"
    }
  ],
  "results": [
    {
      "id": "minecraft:cookie"
    }
  ],
  "sound": "minecraft:entity.generic.eat",
  "volume": 1.0,
  "minimum_pitch": 0.9,
  "maximum_pitch": 1.1
}
```

`sound`, `volume`, `minimum_pitch`, and `maximum_pitch` are optional. The two ingredients are the base item followed by the template or printing material.

### Grinding

The recipe type is `create_enchantment_industry:grinding`. It accepts one item ingredient, up to four probabilistic item results, optional `processing_time`, and either one fluid input or one fluid output. It can be used in Sequenced Assembly and automatically adapts automatable Sandpaper Polishing recipes.

Fluid output example:

```json
{
  "type": "create_enchantment_industry:grinding",
  "ingredients": [
    "create:experience_nugget"
  ],
  "fluid_results": [
    {
      "amount": 243,
      "id": "create_enchantment_industry:experience"
    }
  ]
}
```

Fluid input example:

```json
{
  "type": "create_enchantment_industry:grinding",
  "ingredients": [
    "examplemod:rough_gem"
  ],
  "fluid_ingredients": [
    {
      "type": "fluid_stack",
      "amount": 100,
      "fluid": "create_enchantment_industry:experience"
    }
  ],
  "results": [
    {
      "id": "examplemod:polished_gem"
    }
  ],
  "processing_time": 100
}
```

## Configuration

Configuration is JSON under `config/create_enchantment_industry/`:

* `client.json` contains client-only visual settings.
* `common.json` contains feature flags and aliases.
* `server.json` contains kinetics, fluid capacities, Printer behaviour toggles and costs, enchantment rules, processing costs, and stress values. Its gameplay snapshot and loaded data maps are synchronized to clients.

The Classic Blaze Enchanter is controlled by `processing/classic_blaze_enchanter`; `classic_blaze_enchanter` remains an alias. It is disabled by default and requires a restart when changed. Other entries marked as requiring restart should also be changed while the game or server is stopped.

## 26.1.2 Migration Boundary

This release preserves the Fabric 1.21.11 Fly feature set and stable mod, registry, recipe, tag, data-map, network, NBT, and configuration IDs. It does not guarantee opening a 1.21.11 save directly on 26.1.2. Test with a new world and retain backups.

Apothic Enchanting and Apotheosis only provide NeoForge builds for this target, and Sable does not provide a compatible 26.1.2 Fabric build. Infusing, Bulk Salvaging, Blaze Composer, Affix Templates, and their integration-only tags/configuration are consequently not part of this release.
