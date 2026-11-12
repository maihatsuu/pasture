package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import maihatsuu.pasture.milk.MilkRules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;

/** Mooshrooms handle bowls before calling Cow's mobInteract, so stew needs its own hook. */
@Mixin(MushroomCow.class)
abstract class MushroomCowMixin {
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void pasture$milk(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = MilkRules.interact((Animal) (Object) this, player, hand);
		if (result != null) {
			cir.setReturnValue(result);
		}
	}
}
