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

/** Feature toggles, saved to config/pasture.json. All on by default except passive egg laying; read live. */
public final class PastureConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Pasture.MOD_ID + ".json");

	private static PastureConfig instance;

	/** Cows, goats and mooshrooms go dry after milking and refill by grazing. */
	public boolean milkNeedsGrazing = true;
	/** Glass bottles milk animals; buckets don't. */
	public boolean milkBottles = true;
	/** Keep vanilla's passive egg laying. */
	public boolean passiveEggLaying = false;
	/** Animals walk over to their breeding food lying on the ground and eat one item. */
	public boolean animalsEatDroppedFood = true;

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
		// Writes back any options missing from an older file.
		config.save();
		return config;
	}


	public void save() {
		try (Writer writer = Files.newBufferedWriter(PATH)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			Pasture.LOGGER.error("Couldn't save {}", PATH, e);
		}
	}
}
