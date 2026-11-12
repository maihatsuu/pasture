package maihatsuu.pasture.client;

import java.util.function.Consumer;

import maihatsuu.pasture.PastureConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The Cloth Config screen behind Mod Menu's config button. */
final class PastureConfigScreen {
	private PastureConfigScreen() {
	}

	static Screen create(Screen parent) {
		PastureConfig config = PastureConfig.get();
		PastureConfig defaults = new PastureConfig();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("config.pasture.title"))
			.setSavingRunnable(config::save);
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.pasture.general"));

		toggle(general, entries, "milkNeedsGrazing", config.milkNeedsGrazing, defaults.milkNeedsGrazing, value -> config.milkNeedsGrazing = value);
		toggle(general, entries, "milkBottles", config.milkBottles, defaults.milkBottles, value -> config.milkBottles = value);
		toggle(general, entries, "passiveEggLaying", config.passiveEggLaying, defaults.passiveEggLaying, value -> config.passiveEggLaying = value);
		toggle(general, entries, "animalsEatDroppedFood", config.animalsEatDroppedFood, defaults.animalsEatDroppedFood, value -> config.animalsEatDroppedFood = value);

		return builder.build();
	}

	private static void toggle(ConfigCategory category, ConfigEntryBuilder entries, String key, boolean value, boolean defaultValue, Consumer<Boolean> save) {
		BooleanListEntry entry = entries.startBooleanToggle(Component.translatable("config.pasture." + key), value)
			.setDefaultValue(defaultValue)
			.setTooltip(Component.translatable("config.pasture." + key + ".tooltip"))
			.setSaveConsumer(save)
			.build();
		category.addEntry(entry);
	}

}
