package maihatsuu.pasture.dispenser;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

/**
 * Feeds one item per pulse to the first adult animal in front of the dispenser that would breed
 * on it. If nothing there wants it, the item is dropped as vanilla does. Chickens fed this way
 * drop eggs instead of a chick (see {@code AnimalMixin}).
 */
public class AnimalFeedingBehavior extends DefaultDispenseItemBehavior {
	public static final AnimalFeedingBehavior INSTANCE = new AnimalFeedingBehavior();

	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		BlockPos front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
		List<Animal> animals = source.level().getEntitiesOfClass(
			Animal.class,
			new AABB(front),
			animal -> animal.isAlive() && !animal.isBaby() && animal.getAge() == 0 && animal.canFallInLove() && animal.isFood(stack)
		);
		if (animals.isEmpty()) {
			return super.execute(source, stack);
		}
		animals.getFirst().setInLove(null);
		stack.shrink(1);
		return stack;
	}
}
