# Pasture

A Fabric mod for Minecraft 1.21.1 that gives animal products upkeep. Milk, eggs and scutes are no
longer infinite: animals need grass, food and time. Farms still work, but they need pasture and
supplies instead of a cramped pen. Installed on both sides (the pack does this anyway); the server
side holds all the rules, the client only adds visuals.

## Milk: cows graze to refill
- A cow has a **milk state**: full or dry. Milking makes it dry.
- A dry cow refills by **eating grass**, like a sheep regrowing wool: it lowers its head, eats, and
  it's full again. Cows, mooshrooms and goats graze **exactly like sheep**: whether or not they have
  milk, adults about every 100 seconds on average (1 in 1000 per check, checked every other tick),
  babies far more often (1 in 50), and each meal speeds up a baby's growth. They eat a short grass
  plant if they're standing in one (it breaks), otherwise the grass block below (it turns to dirt).
- **Feeding doesn't refill milk**, just as feeding a sheep doesn't regrow its wool. Wheat breeds a
  cow whether it's dry or full; only grazing refills it. Stone pens give no milk.
- Feedback: milking a dry cow does nothing (no sound). Milking into a bottle plays the milking sound
  and the bottle-filling sound. Jade line "Milk: ready / dry".
- Visual: cows have no eating animation. Add a head-lowering animation on the client (mixin on
  `CowModel`), with grass-break particles and sound from the server so it reads even without it.
- **Goats**: same rules (they graze too).
- **Mooshrooms**: milk and stew share one state. They refill by eating grass or **mycelium**
  (mycelium turns to dirt), which sheep can't.

## Milk bottles replace milk buckets
- Use a **glass bottle** on a full cow, goat or mooshroom to get a **milk bottle**
  (`pasture:milk_bottle`). One milking = one bottle, so stacking doesn't make milk more plentiful.
- Milk bottles stack to **16**, take as long to drink as a potion, clear all effects, and return a
  glass bottle.
- Buckets no longer milk anything. Existing milk buckets still work (old worlds), but nothing makes
  new ones, and they are hidden from the creative inventory. A bucket on a cow does nothing (no arm
  swing).
- Recipes: cake uses 3 milk bottles (old milk buckets are still accepted). Swap milk buckets for bottles anywhere else (other mods' recipes
  via the `c:` milk tag: add the bottle to it).
- Watch: Pantry or other mods that check for the milk bucket item directly.

## Eggs: breeding drops eggs
- Chickens **no longer lay eggs passively**.
- Feeding two chickens seeds works as usual, but instead of a chick they **drop 2–4 eggs** (config), with a 5% chance of a dozen (12)
  and both go on the normal 5-minute breeding cooldown.
- Chicks come from **throwing eggs** (vanilla odds: 1 in 8 for a chick, 1 in 32 of those for four).
- Result: egg supply is paced by seeds and the breeding cooldown, and chicken populations grow
  slower.

## Dropped food
- Animals eat their breeding food when it's lying on the ground: an adult that's off its breeding
  cooldown and not already in love notices the food within **8 blocks** (config) in **line of
  sight**, walks over and eats **one item** from the stack, which puts it in love mode. Love mode
  stops it eating more, so a stack feeds one animal per item.
- Works for every animal, including Brain-driven ones (goats, frogs, armadillos, camels...).
  Animals that must be tamed to breed (wolves, cats, horses, donkeys, llamas) ignore food while
  untamed, and sitting pets stay put. Chickens that eat seeds drop eggs when they breed.
- If a player threw the item, they get the breeding stat and advancement.
- Sounds match hand feeding: silent for most animals; goats, foxes, sniffers, cats, armadillos and
  horses (donkeys, mules, llamas, camels) play their eating sound.
- Automatic farms can breed animals by dropping food (droppers, hoppers) in sight of a pen.

## Armadillo scutes
- Brushing an armadillo gives **2–6 scutes** (config) and makes it **"brushed"** for one in-game day (config).
  Brushing a brushed armadillo does nothing (brush still plays its sound, no durability used).
- Natural shedding is unchanged.
- Jade line "Scute: ready / regrowing".

## Hamlet compatibility
- Expose a small API (`Pasture.canMilk(entity)`, `Pasture.milk(entity)`) so Hamlet's Shepherd can
  milk and collect eggs through the same rules. The Shepherd uses a glass bottle and gets a milk
  bottle, exactly like a player.
- Egg drops from breeding are normal item entities, so a Shepherd can pick them up.

## Config (`config/pasture.json`)
| Option | Default | Description |
|---|---|---|
| `milkNeedsGrazing` | true | Cows, goats and mooshrooms go dry after milking. |
| `milkBottles` | true | Glass bottles milk animals; buckets don't. |
| `milkBottleStackSize` | 16 | Stack size of milk bottles. |
| `passiveEggLaying` | false | Keep vanilla egg laying. |
| `eggsPerBreedMin` / `eggsPerBreedMax` | 2 / 4 | Eggs dropped when two chickens breed. |
| `dozenEggsChance` | 0.05 | Chance that a breeding drops a dozen (12) eggs instead. |
| `animalsEatDroppedFood` | true | Animals eat breeding food dropped near them. |
| `dropFoodRange` | 8 | How far (in line of sight) animals notice dropped food. |
| `scuteCooldownTicks` | 24000 | Time before a brushed armadillo can be brushed again. |
| `scutesPerBrushMin` / `scutesPerBrushMax` | 2 / 6 | Scutes from one brushing. |

## Open questions
- Should sheep follow the same pattern (they already regrow wool from grass, so probably nothing)?
- Does Hamlet's Shepherd also feed/breed chickens to get eggs, or only collect them?
- Hamlet.md's Shepherd still milks with buckets and collects eggs every 5 minutes; it should switch
  to `Pasture.milk` with glass bottles and pick up the eggs that breeding drops.

## Decided
- Feeding a dry cow (goat, mooshroom) wheat breeds it and leaves it dry; only grazing refills milk.

## Testing
- Milk a cow with a bottle → milk bottle, cow is dry; milking again fails; cow eats grass (grass
  becomes dirt) → full again. Wheat on a dry cow breeds it and it stays dry.
- Buckets on cows do nothing; old milk buckets still drinkable; cake crafts with 3 bottles.
- Goats and mooshrooms (milk + stew share state, mycelium refills).
- Chickens never lay on their own; breeding drops eggs, no chick; thrown eggs still hatch chicks.
- Drop 5 wheat near two cows: each walks over and eats one (3 left), and they breed. A cow behind a
  wall or 10 blocks away, a baby, or a cow on breeding cooldown ignores it. A wild wolf ignores meat.
- Brush an armadillo twice: second brush gives nothing until a day passes.
