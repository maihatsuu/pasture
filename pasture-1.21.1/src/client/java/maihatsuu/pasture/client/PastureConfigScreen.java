package maihatsuu.pasture.client;

import java.util.function.Consumer;

import maihatsuu.pasture.PastureConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
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
			.setSavingRunnable(() -> {
				config.clamp();
				config.save();
			});
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.pasture.general"));

		toggle(general, entries, "milkNeedsGrazing", config.milkNeedsGrazing, defaults.milkNeedsGrazing, value -> config.milkNeedsGrazing = value);
		toggle(general, entries, "milkBottles", config.milkBottles, defaults.milkBottles, value -> config.milkBottles = value);
		number(general, entries, "milkBottleStackSize", config.milkBottleStackSize, defaults.milkBottleStackSize, 1, 99, true, value -> config.milkBottleStackSize = value);
		toggle(general, entries, "passiveEggLaying", config.passiveEggLaying, defaults.passiveEggLaying, value -> config.passiveEggLaying = value);
		number(general, entries, "eggsPerBreedMin", config.eggsPerBreedMin, defaults.eggsPerBreedMin, 0, 16, false, value -> config.eggsPerBreedMin = value);
		number(general, entries, "eggsPerBreedMax", config.eggsPerBreedMax, defaults.eggsPerBreedMax, 0, 16, false, value -> config.eggsPerBreedMax = value);
		general.addEntry(entries.startDoubleField(Component.translatable("config.pasture.dozenEggsChance"), config.dozenEggsChance)
			.setDefaultValue(defaults.dozenEggsChance)
			.setMin(0.0)
			.setMax(1.0)
			.setTooltip(Component.translatable("config.pasture.dozenEggsChance.tooltip"))
			.setSaveConsumer(value -> config.dozenEggsChance = value)
			.build());
		toggle(general, entries, "dispenserFeeding", config.dispenserFeeding, defaults.dispenserFeeding, value -> config.dispenserFeeding = value);
		number(general, entries, "scuteCooldownTicks", config.scuteCooldownTicks, defaults.scuteCooldownTicks, 0, 240000, false, value -> config.scuteCooldownTicks = value);
		number(general, entries, "scutesPerBrushMin", config.scutesPerBrushMin, defaults.scutesPerBrushMin, 1, 64, false, value -> config.scutesPerBrushMin = value);
		number(general, entries, "scutesPerBrushMax", config.scutesPerBrushMax, defaults.scutesPerBrushMax, 1, 64, false, value -> config.scutesPerBrushMax = value);

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

	/** The stack size is only read at startup, so it asks for a restart. */
	private static void number(
		ConfigCategory category, ConfigEntryBuilder entries, String key, int value, int defaultValue, int min, int max, boolean needsRestart, Consumer<Integer> save
	) {
		IntegerListEntry entry = entries.startIntField(Component.translatable("config.pasture." + key), value)
			.setDefaultValue(defaultValue)
			.setMin(min)
			.setMax(max)
			.setTooltip(Component.translatable("config.pasture." + key + ".tooltip"))
			.setSaveConsumer(save)
			.build();
		entry.setRequiresRestart(needsRestart);
		category.addEntry(entry);
	}
}
