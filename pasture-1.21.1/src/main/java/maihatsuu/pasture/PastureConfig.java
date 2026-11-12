package maihatsuu.pasture;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Settings, saved to config/pasture.json. Gameplay rules are read live on the server.
 * {@link #milkBottleStackSize} is applied once at startup, so it needs a restart and has to match
 * between client and server.
 */
public final class PastureConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Pasture.MOD_ID + ".json");

	private static PastureConfig instance;

	/** Cows, goats and mooshrooms go dry after milking and refill by grazing. */
	public boolean milkNeedsGrazing = true;
	/** Glass bottles milk animals; buckets don't. */
	public boolean milkBottles = true;
	public int milkBottleStackSize = 16;
	/** Keep vanilla's passive egg laying. */
	public boolean passiveEggLaying = false;
	/** Eggs dropped when two chickens breed (inclusive range). */
	public int eggsPerBreedMin = 2;
	public int eggsPerBreedMax = 4;
	/** Chance (0-1) that a breeding drops a dozen eggs instead. */
	public double dozenEggsChance = 0.05;
	/** Animals walk over to their breeding food lying on the ground and eat one item. */
	public boolean animalsEatDroppedFood = true;
	/** How far (in blocks, with line of sight) animals notice dropped food. */
	public int dropFoodRange = 8;
	/** Ticks before a brushed armadillo can be brushed again (24000 = one day). */
	public int scuteCooldownTicks = 24000;
	/** Scutes from one brushing (inclusive range). */
	public int scutesPerBrushMin = 2;
	public int scutesPerBrushMax = 6;

	public static PastureConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static PastureConfig load() {
		PastureConfig config = null;
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				config = GSON.fromJson(reader, PastureConfig.class);
			} catch (IOException | JsonParseException e) {
				Pasture.LOGGER.error("Couldn't read {}, using defaults", PATH, e);
			}
		}
		if (config == null) {
			config = new PastureConfig();
		}
		config.clamp();
		// Writes back any options missing from an older file.
		config.save();
		return config;
	}

	/** Keeps hand-edited values in a usable range. */
	public void clamp() {
		milkBottleStackSize = Math.clamp(milkBottleStackSize, 1, 99);
		eggsPerBreedMin = Math.max(0, eggsPerBreedMin);
		eggsPerBreedMax = Math.max(eggsPerBreedMin, eggsPerBreedMax);
		dozenEggsChance = Math.clamp(dozenEggsChance, 0.0, 1.0);
		dropFoodRange = Math.clamp(dropFoodRange, 1, 32);
		scuteCooldownTicks = Math.max(0, scuteCooldownTicks);
		scutesPerBrushMin = Math.clamp(scutesPerBrushMin, 1, 64);
		scutesPerBrushMax = Math.clamp(scutesPerBrushMax, scutesPerBrushMin, 64);
	}

	public void save() {
		try (Writer writer = Files.newBufferedWriter(PATH)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			Pasture.LOGGER.error("Couldn't save {}", PATH, e);
		}
	}
}
