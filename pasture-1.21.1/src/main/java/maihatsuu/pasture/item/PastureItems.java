package maihatsuu.pasture.item;

import maihatsuu.pasture.Pasture;
import maihatsuu.pasture.PastureConfig;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class PastureItems {
	/** Stack size is read from the config once, here, so changing it needs a restart. */
	public static final Item MILK_BOTTLE = Registry.register(
		BuiltInRegistries.ITEM,
		Pasture.id("milk_bottle"),
		new MilkBottleItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(PastureConfig.get().milkBottleStackSize))
	);

	private PastureItems() {
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> entries.addAfter(Items.MILK_BUCKET, MILK_BOTTLE));
	}
}
