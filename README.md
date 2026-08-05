# Create: Enchantment Industry Fly

Create: Enchantment Industry Fly ports the core experience and enchantment automation from Create: Enchantment Industry to the Fabric-based Create Fly ecosystem.

This branch targets the following exact development baseline:

- Minecraft 1.21.11 and Java 21
- Fabric Loader 0.18.4 or newer
- Fabric API 0.141.3+1.21.11 or newer
- Create Fly 1.21.11-6.0.9-5 (`>=6.0.9-5 <6.1.0`)
- Create: Dragons Plus Fly 1.11.4-p2
- JEI 27.4.0.15 is optional on the client

## Included in the first Fly release

- Mechanical Grindstone and Grindstone Drain
- Experience Hatch, Printer, and Experience Lantern
- Blaze Enchanter, Blaze Forger, and Classic Blaze Enchanter
- Liquid Experience, Super Experience, templates, handbook, and experience cake
- Create moving-contraption storage, Mechanical Arm interactions, statistics, advancements, and Ponder scenes
- Resource-pack data maps under `data/<namespace>/data_maps/...`
- Optional JEI categories and recipes

The mod keeps the `create_enchantment_industry` namespace and all core registry IDs. Its development artifact is `create-enchantment-industry-fly-1.21.11-2.5.1.jar`.

## Compatibility scope

Apothic Enchanting, Apotheosis, Sable, Sable-Apotheosis, and Touhou Little Maid integrations are not part of this project. Their legacy source and resources have been removed.

This port supports newly created Minecraft 1.21.11 worlds only. It does not migrate NeoForge 1.21.1 worlds, old block-entity NBT, TOML files, or third-party Java APIs. Configuration now uses JSON files:

- `config/create_enchantment_industry/common.json`
- `config/create_enchantment_industry/client.json`
- `config/create_enchantment_industry/server.json`

## Development

Run the complete automated checks with:

```shell
./gradlew clean build
./gradlew runDatagen
./gradlew spotlessCheck
```

Generated resources in `src/generated/resources` are committed and must be reproducible. CI builds the remapped mod JAR and sources JAR. External Modrinth, CurseForge, and Maven publication remain disabled until the Fly project has its own release coordinates.

The planned release display name is **Create: Enchantment Industry Fly 2.5.1 for Create Fly 1.21.11-6.0.9-5**.

## License

Create: Enchantment Industry Fly is licensed under LGPL-3.0-or-later. The port consumes public Create: Dragons Plus Fly APIs and does not copy its Blaze or stress base implementations.
