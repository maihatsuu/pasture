# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

- `Pasture.md` — the design spec for the mod. It is the source of truth for intended behavior, config options, open questions and the manual test checklist. Read it before implementing a feature, and update it when a design decision changes or an open question is resolved.
- `milk_bottle.png` — item texture for `pasture:milk_bottle`, not yet placed in the mod (it belongs at `src/main/resources/assets/pasture/textures/item/milk_bottle.png`).
- `pasture-1.21.1/` — the Gradle project (Fabric mod, Minecraft 1.21.1). All commands below run from here.

The project is still the Fabric example template: `ExampleMixin`/`ExampleClientMixin` are placeholders, and `fabric.mod.json` still has template description/authors/contact.

## Commands

Run from `pasture-1.21.1/`:

- `./gradlew build` — compile and produce the remapped jar in `build/libs/`
- `./gradlew runClient` / `./gradlew runServer` — launch a dev instance (game dir: `run/`)
- `./gradlew genSources` — decompile Minecraft sources for reading vanilla code (e.g. `Cow`, `Sheep`'s `EatBlockGoal`, `Chicken`, `Armadillo`, `DispenserBlock`)

There is no automated test suite; verification is manual in-game, following the Testing section of `Pasture.md`.

## Architecture

- **Toolchain:** Fabric Loader 0.20.1, Fabric API, Loom (`fabric-loom-remap`), Java 21. Versions live in `gradle.properties`.
- **Mappings are official Mojang mappings** (`loom.officialMojangMappings()`), not Yarn — use Mojang names (`ResourceLocation`, `ServerLevel`, `Cow`, `goalSelector`, etc.) in code and mixin targets.
- **Split source sets** (`splitEnvironmentSourceSets()`):
  - `src/main` — common/server code, entrypoint `maihatsuu.pasture.Pasture`, mixins in `maihatsuu.pasture.mixin` registered in `pasture.mixins.json`.
  - `src/client` — client-only code, entrypoint `maihatsuu.pasture.client.PastureClient`, mixins in `maihatsuu.pasture.client.mixin` registered in `pasture.client.mixins.json`.
  - `main` cannot reference `client` classes. New mixins must be added to the matching JSON config; injectors default to `require = 1`, so a mixin that fails to match crashes at load.
- **Server-authoritative design:** the mod is installed on both sides, but all gameplay rules (milk state, egg drops, dispenser feeding, scute cooldown) live in `main`. The client only adds visuals (e.g. the cow head-lowering animation via a `CowModel` mixin); anything the player must notice should also come from the server (particles, sounds) so it works without the client side.
- `Pasture.id(path)` builds `pasture:` resource locations; `Pasture.MOD_ID` is `pasture`.
- **Planned integrations** (per `Pasture.md`): config file `config/pasture.json`; Jade tooltip lines; a public API (`Pasture.canMilk(entity)`, `Pasture.milk(entity)`) used by the Hamlet mod's Shepherd; milk bottle added to the `c:` milk tag for cross-mod recipes.
