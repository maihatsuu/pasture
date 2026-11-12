package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import maihatsuu.pasture.PastureConfig;
import maihatsuu.pasture.milk.Grazing;
import maihatsuu.pasture.milk.GrazingAnimal;
import maihatsuu.pasture.milk.MilkRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(Animal.class)
abstract class AnimalMixin implements GrazingAnimal {
	@Unique
	private int pasture$eatStartTick = Integer.MIN_VALUE;

	@Override
	public int pasture$eatStartTick() {
		return pasture$eatStartTick;
	}

	/** Client: the server says a cow or goat started grazing. Starts the head-lowering animation. */
	@Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
	private void pasture$startEating(byte event, CallbackInfo ci) {
		Animal self = (Animal) (Object) this;
		if (event == Grazing.EAT_EVENT && MilkRules.isMilkable(self)) {
			pasture$eatStartTick = self.tickCount;
			ci.cancel();
		}
	}

	/** Two chickens breeding drop eggs instead of a chick, then go on the normal cooldown. */
	@Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
	private void pasture$breedEggs(ServerLevel level, Animal partner, CallbackInfo ci) {
		Animal self = (Animal) (Object) this;
		if (!(self instanceof Chicken)) {
			return;
		}
		PastureConfig config = PastureConfig.get();
		int eggs = config.eggsPerBreedMin + self.getRandom().nextInt(config.eggsPerBreedMax - config.eggsPerBreedMin + 1);
		double x = (self.getX() + partner.getX()) / 2.0;
		double y = (self.getY() + partner.getY()) / 2.0 + 0.25;
		double z = (self.getZ() + partner.getZ()) / 2.0;
		for (int i = 0; i < eggs; i++) {
			ItemEntity item = new ItemEntity(level, x, y, z, new ItemStack(Items.EGG));
			item.setDefaultPickUpDelay();
			level.addFreshEntity(item);
		}
		// Stats, advancement trigger, cooldowns, hearts and XP, exactly as for a chick.
		self.finalizeSpawnChildFromBreeding(level, partner, null);
		ci.cancel();
	}
}
