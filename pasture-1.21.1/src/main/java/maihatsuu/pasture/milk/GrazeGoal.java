package maihatsuu.pasture.milk;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

/**
 * The sheep's {@code EatBlockGoal} for cows and mooshrooms, using {@link Grazing}'s rules (which add
 * mycelium for mooshrooms).
 */
public class GrazeGoal extends Goal {
	private final Animal animal;
	private int eatTicks;

	public GrazeGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		return Grazing.wantsToGraze(animal);
	}

	@Override
	public void start() {
		eatTicks = adjustedTickDelay(Grazing.EAT_TICKS);
		Grazing.start(animal);
	}

	@Override
	public void stop() {
		eatTicks = 0;
	}

	@Override
	public boolean canContinueToUse() {
		return eatTicks > 0;
	}

	@Override
	public void tick() {
		eatTicks = Math.max(0, eatTicks - 1);
		if (eatTicks == adjustedTickDelay(Grazing.EAT_AT_TICK)) {
			Grazing.eat(animal);
		}
	}
}
