package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import maihatsuu.pasture.milk.GrazeGoal;
import maihatsuu.pasture.milk.MilkRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Cows and mooshrooms: bottle milking, the milk state, and sheep-style grazing. */
@Mixin(Cow.class)
abstract class CowMixin extends Animal {
	protected CowMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void pasture$addGrazeGoal(CallbackInfo ci) {
		// Same priority as the sheep's EatBlockGoal.
		this.goalSelector.addGoal(5, new GrazeGoal(this));
	}

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void pasture$milk(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = MilkRules.interact(this, player, hand);
		if (result != null) {
			cir.setReturnValue(result);
		}
	}
}
