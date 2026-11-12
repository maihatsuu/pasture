package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import maihatsuu.pasture.PastureConfig;
import maihatsuu.pasture.dispenser.AnimalFeedingBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Items that a dispenser would just drop are offered to the animal in front first. Items with their
 * own behavior (bone meal, eggs, buckets...) keep it. Droppers override getDispenseMethod, so they
 * still only drop.
 */
@Mixin(DispenserBlock.class)
abstract class DispenserBlockMixin {
	@ModifyReturnValue(method = "getDispenseMethod", at = @At("RETURN"))
	private DispenseItemBehavior pasture$feedAnimals(DispenseItemBehavior original, Level level, ItemStack stack) {
		if (original.getClass() == DefaultDispenseItemBehavior.class && PastureConfig.get().dispenserFeeding) {
			return AnimalFeedingBehavior.INSTANCE;
		}
		return original;
	}
}
