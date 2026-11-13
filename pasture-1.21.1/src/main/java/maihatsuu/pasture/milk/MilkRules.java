package maihatsuu.pasture.milk;

import org.jetbrains.annotations.Nullable;

import maihatsuu.pasture.PastureAttachments;
import maihatsuu.pasture.PastureConfig;
import maihatsuu.pasture.item.PastureItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

/**
 * The milk state of cows, goats and mooshrooms: full or dry. Milking makes the animal dry; only
 * grazing ({@link Grazing}) makes it full again. Mooshroom milk and stew share the one state.
 * Everything here runs on the server; the client doesn't know the state.
 */
public final class MilkRules {
	private MilkRules() {
	}

	/** Cows, mooshrooms (a kind of cow) and goats. */
	public static boolean isMilkable(Entity entity) {
		return entity instanceof Cow || entity instanceof Goat;
	}

	public static boolean isDry(Entity entity) {
		return PastureConfig.get().milkNeedsGrazing && entity.hasAttached(PastureAttachments.DRY);
	}

	public static void setDry(Entity entity, boolean dry) {
		if (dry && PastureConfig.get().milkNeedsGrazing) {
			entity.setAttached(PastureAttachments.DRY, true);
		} else {
			entity.removeAttached(PastureAttachments.DRY);
		}
	}

	/** True for an adult cow, goat or mooshroom that has milk. */
	public static boolean canMilk(Entity entity) {
		return isMilkable(entity) && entity instanceof Animal animal && !animal.isBaby() && !isDry(entity);
	}

	/**
	 * Milks the animal into a milk bottle, as a player with a glass bottle would (sound included),
	 * and returns the bottle. Returns {@link ItemStack#EMPTY} if it can't be milked right now.
	 */
	public static ItemStack milk(Entity entity) {
		if (!canMilk(entity) || entity.level().isClientSide) {
			return ItemStack.EMPTY;
		}
		playMilkingSounds(entity, true);
		setDry(entity, true);
		return new ItemStack(PastureItems.MILK_BOTTLE);
	}

	/** Uses up a mooshroom's milk for stew, with the milking sound. False if it can't be milked right now. */
	public static boolean milkForStew(Entity entity) {
		if (!(entity instanceof MushroomCow) || !canMilk(entity) || entity.level().isClientSide) {
			return false;
		}
		entity.level().playSound(null, entity, SoundEvents.MOOSHROOM_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
		setDry(entity, true);
		return true;
	}

	/**
	 * A player using an item on a cow, goat or mooshroom. Returns the result to use instead of
	 * vanilla's, or null to let vanilla handle it.
	 */
	@Nullable
	public static InteractionResult interact(Animal animal, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean bottle = stack.is(Items.GLASS_BOTTLE);
		boolean bucket = stack.is(Items.BUCKET);
		boolean bowl = stack.is(Items.BOWL) && animal instanceof MushroomCow;
		if (animal.isBaby() || !(bottle || bucket || bowl)) {
			return null;
		}

		// On the client this uses the client's config, which normally matches the server's.
		PastureConfig config = PastureConfig.get();
		if (bottle && !config.milkBottles) {
			return null;
		}
		if (bucket && config.milkBottles) {
			// Buckets no longer milk anything: no arm swing either.
			return InteractionResult.PASS;
		}

		// The client doesn't know the milk state: it just swings and lets the server decide (the
		// server updates the held item).
		if (animal.level().isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (isDry(animal)) {
			// Nothing to give: no sound, the held item stays as it is.
			return InteractionResult.CONSUME;
		}

		if (bowl) {
			// Vanilla hands out the (suspicious) stew; it just uses up the milk.
			setDry(animal, true);
			return null;
		}

		ItemStack result = bottle ? new ItemStack(PastureItems.MILK_BOTTLE) : new ItemStack(Items.MILK_BUCKET);
		playMilkingSounds(animal, bottle);
		player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
		setDry(animal, true);
		return InteractionResult.SUCCESS;
	}

	/**
	 * The animal's milking sound, plus the bottle-filling sound when milked into a bottle. Played
	 * from the server to everyone, the milker included (the client skips milking, so it plays nothing).
	 */
	private static void playMilkingSounds(Entity entity, boolean bottle) {
		entity.level().playSound(null, entity, milkingSound(entity), SoundSource.PLAYERS, 1.0F, 1.0F);
		if (bottle) {
			entity.level().playSound(null, entity, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	private static SoundEvent milkingSound(Entity entity) {
		if (entity instanceof Goat goat) {
			return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
		}
		return SoundEvents.COW_MILK;
	}
}
