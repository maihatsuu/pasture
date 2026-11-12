# Pasture

A Fabric mod for Minecraft 1.21.1 that gives animal products upkeep. Milk, eggs and scutes are no
longer infinite: animals need grass, food and time. Farms still work, but they need pasture and
supplies instead of a cramped pen. Installed on both sides (the pack does this anyway); the server
side holds all the rules, the client only adds visuals.

## Milk: cows graze to refill
- A cow has a **milk state**: full or dry. Milking makes it dry.
- A dry cow refills by **eating grass**, like a sheep regrowing wool: it lowers its head, the grass
  block turns to dirt, and it's full again. Uses a goal like the sheep's `EatBlockGoal`, only active
  while dry, so full cows don't strip pastures.
- **Wheat** refills a dry cow instantly (a dry adult eats the wheat instead of entering love mode;
  full cows breed as normal). This keeps stone pens possible, at the cost of wheat.
- Feedback: milking a dry cow does nothing and plays a low moo. Jade line "Milk: ready / dry".
- Visual: cows have no eating animation. Add a head-lowering animation on the client (mixin on
  `CowModel`), with grass-break particles and sound from the server so it reads even without it.
- **Goats**: same rules (they graze too).
- **Mooshrooms**: milk and stew share one state. They refill by eating grass or **mycelium**
  (mycelium turns to dirt), or from wheat.

## Milk bottles replace milk buckets
- Use a **glass bottle** on a full cow, goat or mooshroom to get a **milk bottle**
  (`pasture:milk_bottle`). One milking = one bottle, so stacking doesn't make milk more plentiful.
- Milk bottles stack to **16**, take as long to drink as a potion, clear all effects, and return a
  glass bottle.
- Buckets no longer milk anything. Existing milk buckets still work (old worlds), but nothing makes
  new ones.
- Recipes: cake uses 3 milk bottles. Swap milk buckets for bottles anywhere else (other mods' recipes
  via the `c:` milk tag: add the bottle to it).
- Watch: Pantry or other mods that check for the milk bucket item directly.

## Eggs: breeding drops eggs
- Chickens **no longer lay eggs passively**.
- Feeding two chickens seeds works as usual, but instead of a chick they **drop 2–4 eggs** (config)
  and both go on the normal 5-minute breeding cooldown.
- Chicks come from **throwing eggs** (vanilla odds: 1 in 8 for a chick, 1 in 32 of those for four).
- Result: egg supply is paced by seeds and the breeding cooldown, and chicken populations grow
  slower.

## Dispenser feeding
- A dispenser holding an animal's food feeds the first adult animal in front of it that wants it:
  breeding food puts it in love mode, wheat refills a dry cow, seeds make chickens breed.
- One item per pulse. If nothing in front wants the food, it falls back to dropping the item as
  vanilla does.
- This keeps automatic farms possible, but they have to be supplied with food.

## Armadillo scutes
- Brushing an armadillo gives a scute and makes it **"brushed"** for one in-game day (config).
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
| `wheatRefillsMilk` | true | Feeding wheat refills a dry animal. |
| `milkBottles` | true | Glass bottles milk animals; buckets don't. |
| `milkBottleStackSize` | 16 | Stack size of milk bottles. |
| `passiveEggLaying` | false | Keep vanilla egg laying. |
| `eggsPerBreed` | 2–4 | Eggs dropped when two chickens breed. |
| `dispenserFeeding` | true | Dispensers feed animals. |
| `scuteCooldownTicks` | 24000 | Time before a brushed armadillo can be brushed again. |

## Open questions
- Can a dry cow still be bred (with wheat it refills instead)? Or breed and refill at once?
- Should sheep follow the same pattern (they already regrow wool from grass, so probably nothing)?
- Does Hamlet's Shepherd also feed/breed chickens to get eggs, or only collect them?
- Milk bottle texture: vanilla-style white bottle (needs one new item texture).

## Testing
- Milk a cow with a bottle → milk bottle, cow is dry; milking again fails; cow eats grass (grass
  becomes dirt) → full again. Wheat on a dry cow refills it; wheat on a full cow breeds.
- Buckets on cows do nothing; old milk buckets still drinkable; cake crafts with 3 bottles.
- Goats and mooshrooms (milk + stew share state, mycelium refills).
- Chickens never lay on their own; breeding drops eggs, no chick; thrown eggs still hatch chicks.
- Dispenser with wheat facing a dry cow refills it; facing nothing drops wheat.
- Brush an armadillo twice: second brush gives nothing until a day passes.
