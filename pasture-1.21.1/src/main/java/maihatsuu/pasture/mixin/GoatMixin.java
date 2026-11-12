package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import maihatsuu.pasture.milk.Grazing;
import maihatsuu.pasture.milk.MilkRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;

/** Goats: bottle milking and the milk state. They use a Brain, not goals, so grazing is ticked here. */
@Mixin(Goat.class)
abstract class GoatMixin {
	@Unique
	private int pasture$eatTicks;

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void pasture$milk(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = MilkRules.interact((Goat) (Object) this, player, hand);
		if (result != null) {
			cir.setReturnValue(result);
		}
	}

	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void pasture$graze(CallbackInfo ci) {
		Goat goat = (Goat) (Object) this;
		if (pasture$eatTicks > 0) {
			pasture$eatTicks--;
			// Stand still while eating.
			goat.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			goat.getNavigation().stop();
			if (pasture$eatTicks == Grazing.EAT_AT_TICK) {
				Grazing.eat(goat);
			}
		} else if ((goat.tickCount + goat.getId()) % 2 == 0 && goat.onGround() && !goat.isInWater() && Grazing.wantsToGraze(goat)) {
			// Every other tick, like Mob's goal selector checks a sheep's EatBlockGoal.
			pasture$eatTicks = Grazing.EAT_TICKS;
			Grazing.start(goat);
		}
	}
}
