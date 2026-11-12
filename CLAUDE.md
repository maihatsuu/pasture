# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

- `Pasture.md` — the design spec for the mod. It is the source of truth for intended behavior, config options, open questions and the manual test checklist. Read it before implementing a feature, and update it when a design decision changes or an open question is resolved.
- `pasture-1.21.1/` — the Gradle project (Fabric mod, Minecraft 1.21.1). All commands below run from here.
- `.github/workflows/build.yml` — CI at the repo root; runs `./gradlew build` inside `pasture-1.21.1/`.

Sibling mods in `~/Programming/Minecraft/Fabric/` (Pantry, Snout, Hamlet, Rebalanced Core) share these conventions; Pantry's config/screen setup was copied here.

## Commands

Run from `pasture-1.21.1/`:

- `./gradlew build` — compile and produce the remapped jar in `build/libs/`
- `./gradlew runClient` / `./gradlew runServer` — launch a dev instance (game dir: `run/`)
- `./gradlew genSources` — decompile Minecraft sources for reading vanilla code (e.g. `Cow`, `Sheep`'s `EatBlockGoal`, `Chicken`, `Armadillo`, `DispenserBlock`)

There is no automated test suite; verification is manual in-game, following the Testing section of `Pasture.md`. A passing build doesn't prove mixins match: targets like `Cow` only load once a world is running, and `defaultRequire: 1` crashes then. `runClient` includes Jade, Mod Menu and Cloth Config (`modLocalRuntime`).

## Architecture

- **Toolchain:** Fabric Loader 0.20.1, Fabric API, Loom (`fabric-loom-remap`), Java 21. Versions live in `gradle.properties`.
- **Mappings are official Mojang mappings** (`loom.officialMojangMappings()`), not Yarn — use Mojang names (`ResourceLocation`, `ServerLevel`, `Cow`, `goalSelector`, etc.) in code and mixin targets.
- **Split source sets** (`splitEnvironmentSourceSets()`):
  - `src/main` — common/server code, entrypoint `maihatsuu.pasture.Pasture`, mixins in `maihatsuu.pasture.mixin` registered in `pasture.mixins.json`.
  - `src/client` — client-only code, entrypoint `maihatsuu.pasture.client.PastureClient`, mixins in `maihatsuu.pasture.client.mixin` registered in `pasture.client.mixins.json`.
  - `main` cannot reference `client` classes. New mixins must be added to the matching JSON config; injectors default to `require = 1`, so a mixin that fails to match crashes at load.
- **Server-authoritative design:** the mod is installed on both sides, but all gameplay rules (milk state, egg drops, dispenser feeding, scute cooldown) live in `main`. The client only adds visuals (e.g. the cow head-lowering animation via a `QuadrupedModel` mixin); anything the player must notice should also come from the server (particles, sounds) so it works without the client side.
- `Pasture.id(path)` builds `pasture:` resource locations; `Pasture.MOD_ID` is `pasture`.

## How the mod fits together

- **Config** (`PastureConfig`): plain Gson, `config/pasture.json`, read live on the server. `milkBottleStackSize` is read once when the item registers (restart; must match on both sides). Optional Cloth screen (`client/ModMenuIntegration` → `PastureConfigScreen`); Cloth classes stay in the screen class, behind `isModLoaded("cloth-config")`.
- **State** (`PastureAttachments`): Fabric persistent data attachments, not synced. `DRY` (present while a cow/goat/mooshroom is dry) and `BRUSHED_UNTIL` (armadillo, game time). The client never knows them; Jade gets them via its server data provider.
- **Milk** (`milk/`): `MilkRules` holds all milking logic (bottle/bucket/bowl, sounds, the Hamlet API behind `Pasture.canMilk`/`milk`). `CowMixin`, `MushroomCowMixin`, `GoatMixin` call `MilkRules.interact` at the head of `mobInteract`; on the client it just returns SUCCESS and the server decides. Only grazing refills, and grazing copies the sheep's `EatBlockGoal` exactly (always on, adults 1/1000 and babies 1/50 per check every other tick, short grass or grass block, babies age up): `GrazeGoal` (added to cows' goalSelector) and an every-other-tick ticker in `GoatMixin` (goats use a Brain) both go through `Grazing`. Feeding never touches milk.
- **Grazing animation**: the server broadcasts entity event 10 (the sheep's byte); `AnimalMixin.handleEntityEvent` records the start tick behind the `GrazingAnimal` duck interface, and `client/mixin/QuadrupedModelMixin` lowers cows' heads with the sheep's curve and tilts goats' heads (own constants: their head pivots in the neck).
- **Eggs**: `ChickenMixin` holds `eggTime` at 6000 unless `passiveEggLaying`; `AnimalMixin.spawnChildFromBreeding` makes chickens drop eggs and calls vanilla `finalizeSpawnChildFromBreeding` (stats, cooldown, XP). Thrown eggs are vanilla.
- **Dispensers**: `DispenserBlockMixin` swaps a plain `DefaultDispenseItemBehavior` for `AnimalFeedingBehavior`, which falls back to dropping. Items with their own behavior, and droppers, are untouched.
- **Armadillo**: `ArmadilloMixin` on `brushOffScute` (dispensers' brushing goes through it too).
- **Item/data**: `item/MilkBottleItem` (`pasture:milk_bottle`), cake recipe override in `data/minecraft/recipe/cake.json`, bottle in `c:drinks/milk`.
- **Jade** (`compat/PastureJadePlugin`, `jade` entrypoint, in `main` because it has a server data provider). Jade is `modCompileOnly`, never referenced outside this class.
