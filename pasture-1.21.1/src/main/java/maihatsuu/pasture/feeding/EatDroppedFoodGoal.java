package maihatsuu.pasture.feeding;

import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import maihatsuu.pasture.PastureConfig;
import maihatsuu.pasture.mixin.AbstractHorseAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Animals notice their breeding food lying on the ground (within range and in line of sight), walk
 * over and eat one item from the stack, which puts them in love mode. Only adults off their
 * breeding cooldown and not already in love; love mode then stops them eating more.
 * <p>
 * Added to every {@link Animal} by {@code MobMixin}. Goal-driven animals walk with their navigation;
 * Brain-driven ones (goats, frogs, armadillos, camels...) still tick their goal selector, so this
 * goal runs for them too, steering them through their WALK_TARGET memory instead.
 */
public class EatDroppedFoodGoal extends Goal {
	/** How far (in blocks, with line of sight) animals notice dropped food. */
	private static final double RANGE = 8.0;
	private static final double SPEED = 1.1;
	private static final int SCAN_INTERVAL = 20;
	private static final int REPATH_INTERVAL = 10;
	private static final int GIVE_UP_TICKS = 200;

	private final Animal animal;
	@Nullable
	private ItemEntity target;
	private int nextScan;
	private int ticks;

	public EatDroppedFoodGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (--nextScan > 0) {
			return false;
		}
		// Goals are checked every other tick, so this scans about once every 2 seconds.
		nextScan = reducedTickDelay(SCAN_INTERVAL) + animal.getRandom().nextInt(5);
		if (!wantsFood()) {
			return false;
		}
		target = findFood();
		return target != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (target == null || !target.isAlive() || target.getItem().isEmpty() || ticks > adjustedTickDelay(GIVE_UP_TICKS) || !wantsFood()) {
			return false;
		}
		double maxRange = range() + 4.0;
		return animal.distanceToSqr(target) <= maxRange * maxRange;
	}

	@Override
	public void start() {
		ticks = 0;
		moveToTarget();
	}

	@Override
	public void stop() {
		target = null;
		animal.getNavigation().stop();
		if (usesBrain()) {
			animal.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			animal.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (target == null) {
			return;
		}
		ticks++;
		if (animal.getBoundingBox().inflate(1.0).intersects(target.getBoundingBox())) {
			eat(target);
			return;
		}
		// Brain animals' own wandering would overwrite the walk target, so it's refreshed every tick.
		if (usesBrain() || ticks % REPATH_INTERVAL == 0) {
			moveToTarget();
		}
		if (!usesBrain()) {
			animal.getLookControl().setLookAt(target, 30.0F, animal.getMaxHeadXRot());
		}
	}

	private void moveToTarget() {
		if (usesBrain()) {
			BehaviorUtils.setWalkAndLookTargetMemories(animal, target, (float) SPEED, 0);
		} else {
			animal.getNavigation().moveTo(target, SPEED);
		}
	}

	/** Eats one item from the stack. The thrower, if a player, gets credit for the breeding. */
	private void eat(ItemEntity item) {
		ItemStack stack = item.getItem();
		playFeedingSound(stack);
		animal.gameEvent(GameEvent.EAT);
		// ItemEntity removes itself once its stack is empty.
		stack.shrink(1);
		item.setItem(stack);
		animal.setInLove(item.getOwner() instanceof Player player ? player : null);
		target = null;
	}

	/**
	 * The same sound as feeding by hand, which most animals don't make: goats, foxes, sniffers, cats
	 * and horses (donkeys, mules, llamas, camels) do. Armadillos play theirs from setInLove.
	 */
	private void playFeedingSound(ItemStack stack) {
		if (animal instanceof Goat || animal instanceof Sniffer) {
			animal.level().playSound(
				null, animal, animal.getEatingSound(stack), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(animal.getRandom(), 0.8F, 1.2F)
			);
		} else if (animal instanceof Fox) {
			animal.playSound(animal.getEatingSound(stack), 1.0F, 1.0F);
		} else if (animal instanceof Cat) {
			animal.playSound(SoundEvents.CAT_EAT, 1.0F, 1.0F);
		} else if (animal instanceof AbstractHorse horse) {
			((AbstractHorseAccessor) horse).pasture$eating();
		}
	}

	private boolean wantsFood() {
		return PastureConfig.get().animalsEatDroppedFood
			&& animal.isAlive()
			&& !animal.isBaby()
			&& animal.getAge() == 0
			&& animal.canFallInLove()
			&& canBreed()
			&& !isPanicking();
	}

	/**
	 * Wolves, cats and horses (donkeys, mules, llamas) can't breed until tamed, so wild ones leave the
	 * food alone, and sitting pets stay put. Camels count as always tamed in vanilla.
	 */
	private boolean canBreed() {
		if (animal instanceof TamableAnimal tamable) {
			return tamable.isTame() && !tamable.isOrderedToSit();
		}
		if (animal instanceof AbstractHorse horse) {
			return horse.isTamed();
		}
		return true;
	}

	/** Goal-driven animals' PanicGoal outranks this goal; Brain animals need checking here. */
	private boolean isPanicking() {
		return usesBrain() && animal.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
	}

	private boolean usesBrain() {
		return animal.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
	}

	private double range() {
		return RANGE;
	}

	/** The nearest item of this animal's breeding food within range and in line of sight. */
	@Nullable
	private ItemEntity findFood() {
		double range = range();
		List<ItemEntity> items = animal.level().getEntitiesOfClass(
			ItemEntity.class,
			animal.getBoundingBox().inflate(range),
			item -> item.isAlive() && !item.getItem().isEmpty() && animal.isFood(item.getItem()) && animal.distanceToSqr(item) <= range * range
		);
		ItemEntity nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (ItemEntity item : items) {
			double distance = animal.distanceToSqr(item);
			if (distance < nearestDistance && animal.hasLineOfSight(item)) {
				nearest = item;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
