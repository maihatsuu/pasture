# Pasture

A Fabric mod for Minecraft 1.21.1 that gives animal products upkeep. Milk, eggs and scutes are no
longer free: animals need grass, food and time. Farms still work, but they need pasture and
supplies instead of a cramped pen.

## Features

### Milk comes from grazing
- Cows, goats and mooshrooms are **full** or **dry**. Milking makes them dry.
- A dry animal refills by **grazing**, exactly like a sheep regrowing its wool. It lowers its head
  and eats a short grass plant or the grass block it stands on, which turns to dirt. Mooshrooms can
  also eat mycelium.
- Feeding doesn't refill milk. Wheat only breeds. A stone pen gives no milk; a pasture does.
- Mooshroom milk and stew come from the same supply.

### Milk bottles
- Use a **glass bottle** on a full cow, goat or mooshroom to get a **Milk Bottle**. Buckets no
  longer milk anything.
- Milk bottles stack to 16, clear all effects like milk, and give the glass bottle back.
- Cake takes 3 milk bottles. Milk buckets from old worlds still work, in cake too.

### Eggs come from breeding
- Chickens no longer lay eggs on their own.
- Breeding two chickens drops **2–4 eggs** instead of a chick, with a 5% chance of a dozen.
- Thrown eggs still hatch chicks.

### Animals eat dropped food
- Drop an animal's breeding food on the ground: an adult that's ready to breed and can see it
  within 8 blocks walks over, eats one item and goes into love mode.
- One item per animal, so a stack feeds that many animals.
- Works with droppers and hoppers, so automatic farms can breed animals if you keep them supplied.
- Wild wolves, cats, horses, donkeys and llamas leave food alone until they're tamed.

### Armadillo scutes
- Brushing an armadillo gives **2–6 scutes**, then it needs a day to regrow. Brushing it again
  before then gives nothing and costs no durability.
- Natural shedding is unchanged.

### Jade
With [Jade](https://modrinth.com/mod/jade) installed, looking at an animal shows "Milk: ready /
dry" or "Scute: ready / regrowing".

## Installation
Install on **both the client and the server**, together with
[Fabric API](https://modrinth.com/mod/fabric-api). The server holds all the rules; the client adds
the grazing animation and the milk bottle item.

Optional:
- [Jade](https://modrinth.com/mod/jade): milk and scute tooltip lines.
- [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config):
  an in-game config screen.

## Configuration
Settings are in `config/pasture.json`, or in the config screen if Mod Menu and Cloth Config are
installed. All of them apply right away except the stack size, which needs a restart and must match
between client and server.

| Option | Default | Description |
|---|---|---|
| `milkNeedsGrazing` | `true` | Cows, goats and mooshrooms go dry after milking and refill by grazing. |
| `milkBottles` | `true` | Glass bottles milk animals; buckets don't. |
| `milkBottleStackSize` | `16` | Stack size of milk bottles (restart). |
| `passiveEggLaying` | `false` | Keep vanilla's egg laying. |
| `eggsPerBreedMin` / `eggsPerBreedMax` | `2` / `4` | Eggs dropped when two chickens breed. |
| `dozenEggsChance` | `0.05` | Chance that a breeding drops 12 eggs instead. |
| `animalsEatDroppedFood` | `true` | Animals eat breeding food dropped near them. |
| `dropFoodRange` | `8` | How far (in blocks, in line of sight) animals notice dropped food. |
| `scuteCooldownTicks` | `24000` | Time before a brushed armadillo can be brushed again (24000 = one day). |
| `scutesPerBrushMin` / `scutesPerBrushMax` | `2` / `6` | Scutes from one brushing. |

## For mod developers
Other mods can milk through Pasture's rules:

```java
if (Pasture.canMilk(entity)) {        // adult cow, goat or mooshroom with milk
	ItemStack milk = Pasture.milk(entity); // plays the sounds, makes it dry, returns a milk bottle
}
```

`milk` runs on the server and doesn't use up a glass bottle; that's up to the caller. The milk
bottle (`pasture:milk_bottle`) is in the `c:drinks/milk` tag.

## Building
From `pasture-1.21.1/`, with Java 21 or newer:

```sh
./gradlew build      # jar in build/libs/
./gradlew runClient  # dev client, with Jade, Mod Menu and Cloth Config
```

## License
CC0-1.0
