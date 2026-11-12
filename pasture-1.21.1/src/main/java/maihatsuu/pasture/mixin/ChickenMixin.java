package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import maihatsuu.pasture.PastureConfig;
import net.minecraft.world.entity.animal.Chicken;

/** Chickens no longer lay eggs on their own: the egg timer is held up before it can run out. */
@Mixin(Chicken.class)
abstract class ChickenMixin {
	@Shadow
	public int eggTime;

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void pasture$noPassiveEggs(CallbackInfo ci) {
		if (!PastureConfig.get().passiveEggLaying && eggTime < 6000) {
			// The shortest vanilla delay, so turning laying back on resumes within 5-10 minutes.
			eggTime = 6000;
		}
	}
}
