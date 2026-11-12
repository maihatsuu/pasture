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
		entity.level().playSound(null, entity, milkingSound(entity), SoundSource.NEUTRAL, 1.0F, 1.0F);
		setDry(entity, true);
		return new ItemStack(PastureItems.MILK_BOTTLE);
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

		// The client doesn't know the milk state or the server's config: it just swings and lets
		// the server decide (the server updates the held item).
		if (animal.level().isClientSide) {
			return InteractionResult.SUCCESS;
		}

		PastureConfig config = PastureConfig.get();
		if (bottle && !config.milkBottles) {
			return null;
		}
		if (bucket && config.milkBottles) {
			// Buckets no longer milk anything.
			return InteractionResult.PASS;
		}

		if (isDry(animal)) {
			playDryFeedback(animal);
			return InteractionResult.CONSUME;
		}

		if (bowl) {
			// Vanilla hands out the (suspicious) stew; it just uses up the milk.
			setDry(animal, true);
			return null;
		}

		ItemStack result = bottle ? new ItemStack(PastureItems.MILK_BOTTLE) : new ItemStack(Items.MILK_BUCKET);
		player.playSound(milkingSound(animal), 1.0F, 1.0F);
		player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result));
		setDry(animal, true);
		return InteractionResult.SUCCESS;
	}

	/** A low-pitched call: "I've got nothing for you." */
	public static void playDryFeedback(Animal animal) {
		SoundEvent sound;
		if (animal instanceof Goat goat) {
			sound = goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
		} else {
			sound = SoundEvents.COW_AMBIENT;
		}
		animal.level().playSound(null, animal, sound, SoundSource.NEUTRAL, 1.0F, 0.6F);
	}

	private static SoundEvent milkingSound(Entity entity) {
		if (entity instanceof Goat goat) {
			return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
		}
		return SoundEvents.COW_MILK;
	}
}
