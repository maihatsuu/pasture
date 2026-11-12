package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import maihatsuu.pasture.feeding.EatDroppedFoodGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/**
 * Gives every animal (vanilla or modded, goal- or Brain-driven) the goal of eating dropped breeding
 * food. Added after registerGoals, server side only, as vanilla does for its own goals.
 */
@Mixin(Mob.class)
abstract class MobMixin {
	@Shadow
	@Final
	protected GoalSelector goalSelector;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void pasture$addEatDroppedFoodGoal(EntityType<? extends Mob> entityType, Level level, CallbackInfo ci) {
		if (level != null && !level.isClientSide && (Object) this instanceof Animal animal) {
			// Same priority as TemptGoal: panicking and breeding come first, and a player tempting wins.
			goalSelector.addGoal(3, new EatDroppedFoodGoal(animal));
		}
	}
}
