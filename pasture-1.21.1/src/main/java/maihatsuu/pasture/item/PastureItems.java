package maihatsuu.pasture.item;

import maihatsuu.pasture.Pasture;
import maihatsuu.pasture.PastureConfig;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class PastureItems {
	public static final Item MILK_BOTTLE = Registry.register(
		BuiltInRegistries.ITEM,
		Pasture.id("milk_bottle"),
		new MilkBottleItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16))
	);

	private PastureItems() {
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.addAfter(Items.MILK_BUCKET, MILK_BOTTLE);
			hideMilkBucket(entries);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(PastureItems::hideMilkBucket);
	}

	/** Nothing makes milk buckets while bottles milk, so creative doesn't offer them either (uses the client's config). */
	private static void hideMilkBucket(FabricItemGroupEntries entries) {
		if (PastureConfig.get().milkBottles) {
			entries.getDisplayStacks().removeIf(stack -> stack.is(Items.MILK_BUCKET));
			entries.getSearchTabStacks().removeIf(stack -> stack.is(Items.MILK_BUCKET));
		}
	}
}
