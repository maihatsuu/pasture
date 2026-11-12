package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import maihatsuu.pasture.PastureAttachments;
import maihatsuu.pasture.PastureConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

/**
 * Brushing gives a scute, then the armadillo is "brushed" for a while: brushing it again only plays
 * the sound. Returning false means the brush takes no durability. Dispensers brush through the same
 * method, so the rule covers them too.
 */
@Mixin(Armadillo.class)
abstract class ArmadilloMixin {
	@Inject(method = "brushOffScute", at = @At("HEAD"), cancellable = true)
	private void pasture$stillBrushed(CallbackInfoReturnable<Boolean> cir) {
		Armadillo self = (Armadillo) (Object) this;
		if (self.level().isClientSide || self.isBaby()) {
			return;
		}
		Long until = self.getAttached(PastureAttachments.BRUSHED_UNTIL);
		if (until != null && self.level().getGameTime() < until) {
			self.playSound(SoundEvents.ARMADILLO_BRUSH);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "brushOffScute", at = @At("RETURN"))
	private void pasture$markBrushed(CallbackInfoReturnable<Boolean> cir) {
		Armadillo self = (Armadillo) (Object) this;
		if (cir.getReturnValueZ() && !self.level().isClientSide) {
			self.setAttached(PastureAttachments.BRUSHED_UNTIL, self.level().getGameTime() + PastureConfig.get().scuteCooldownTicks);
		}
	}
}
