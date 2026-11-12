package maihatsuu.pasture.milk;

import maihatsuu.pasture.PastureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cows, mooshrooms and goats graze exactly like sheep ({@code EatBlockGoal}): whether or not they
 * have milk, adults with a 1-in-1000 chance per check and babies 1 in 50, eating a short grass plant
 * (which breaks) or else the grass block below (which turns to dirt). Mooshrooms can also eat
 * mycelium. Eating refills a dry animal, like a sheep regrowing wool, and speeds up a baby's growth.
 * Driven by {@link GrazeGoal} for cows and mooshrooms and by a ticker in {@code GoatMixin} for goats
 * (they use a Brain instead of goals). Off when {@code milkNeedsGrazing} is off.
 */
public final class Grazing {
	/** Same as the sheep's eat animation. */
	public static final int EAT_TICKS = 40;
	/** Tick (counting down) at which the block is eaten, as in {@code EatBlockGoal}. */
	public static final int EAT_AT_TICK = 4;
	/** Entity event that starts the head-lowering animation on the client (the sheep's byte). */
	public static final byte EAT_EVENT = 10;

	private Grazing() {
	}

	/** Rolls the sheep's chance, then checks for something to eat. */
	public static boolean wantsToGraze(Animal animal) {
		return PastureConfig.get().milkNeedsGrazing
			&& animal.getRandom().nextInt(animal.isBaby() ? 50 : 1000) == 0
			&& hasFood(animal);
	}

	private static boolean hasFood(Animal animal) {
		BlockPos pos = animal.blockPosition();
		Level level = animal.level();
		return level.getBlockState(pos).is(Blocks.SHORT_GRASS) || canEatBlock(animal, level.getBlockState(pos.below()));
	}

	private static boolean canEatBlock(Animal animal, BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || (animal instanceof MushroomCow && state.is(Blocks.MYCELIUM));
	}

	public static void start(Animal animal) {
		animal.level().broadcastEntityEvent(animal, EAT_EVENT);
		animal.getNavigation().stop();
	}

	/** Eats the short grass here or the block below (if still there), then refills the animal. */
	public static void eat(Animal animal) {
		Level level = animal.level();
		BlockPos pos = animal.blockPosition();
		// Like the sheep: without mob griefing the block stays, but the animal still eats.
		boolean griefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
		if (level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
			if (griefing) {
				level.destroyBlock(pos, false);
			}
		} else {
			BlockPos below = pos.below();
			BlockState state = level.getBlockState(below);
			if (!canEatBlock(animal, state)) {
				return;
			}
			if (griefing) {
				level.levelEvent(2001, below, Block.getId(state));
				level.setBlock(below, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		MilkRules.setDry(animal, false);
		animal.ate();
		if (animal.isBaby()) {
			animal.ageUp(60);
		}
	}
}
