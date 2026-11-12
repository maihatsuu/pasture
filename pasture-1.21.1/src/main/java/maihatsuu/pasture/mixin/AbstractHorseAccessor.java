package maihatsuu.pasture.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** Horses, donkeys, mules, llamas and camels open their mouth and play their eating sound when fed. */
@Mixin(AbstractHorse.class)
public interface AbstractHorseAccessor {
	@Invoker("eating")
	void pasture$eating();
}
