package maihatsuu.pasture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import maihatsuu.pasture.item.PastureItems;
import maihatsuu.pasture.milk.MilkRules;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.item.ItemStack;

public class Pasture implements ModInitializer {
	public static final String MOD_ID = "pasture";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PastureConfig.get();
		PastureAttachments.init();
		PastureItems.init();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	// API for other mods (Hamlet's Shepherd): milking through the same rules as a player.

	/** True if the entity is an adult cow, goat or mooshroom that has milk right now. */
	public static boolean canMilk(Entity entity) {
		return MilkRules.canMilk(entity);
	}

	/**
	 * Milks the entity as a player with a glass bottle would: plays the sound, makes it dry and
	 * returns a milk bottle (the caller uses up its own glass bottle). Returns
	 * {@link ItemStack#EMPTY} if {@link #canMilk} is false. Server side only.
	 */
	public static ItemStack milk(Entity entity) {
		return MilkRules.milk(entity);
	}

	/**
	 * Uses up a mooshroom's milk for mushroom stew, as a player with a bowl would: plays the sound and
	 * makes it dry (milk and stew share one state). The caller uses up its own bowl and makes the stew.
	 * Returns false, doing nothing, if it isn't a mooshroom with milk. Server side only.
	 */
	public static boolean milkForStew(Entity entity) {
		return MilkRules.milkForStew(entity);
	}

	/**
	 * True if the entity is an adult armadillo that isn't still brushed, so brushing it
	 * ({@code Armadillo.brushOffScute}) would give scutes.
	 */
	public static boolean canBrush(Entity entity) {
		if (!(entity instanceof Armadillo armadillo) || armadillo.isBaby()) {
			return false;
		}
		Long until = armadillo.getAttached(PastureAttachments.BRUSHED_UNTIL);
		return until == null || armadillo.level().getGameTime() >= until;
	}
}
