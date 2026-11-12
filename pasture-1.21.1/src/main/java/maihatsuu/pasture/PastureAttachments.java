package maihatsuu.pasture;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * Per-entity state, saved with the entity. Server-side only: nothing is synced to the client
 * (Jade gets it through its own server data provider).
 */
public final class PastureAttachments {
	/** Present (true) while a cow, goat or mooshroom is dry. Removed when it's full again. */
	public static final AttachmentType<Boolean> DRY = AttachmentRegistry.createPersistent(Pasture.id("dry"), Codec.BOOL);
	/** Game time until which an armadillo can't be brushed again. */
	public static final AttachmentType<Long> BRUSHED_UNTIL = AttachmentRegistry.createPersistent(Pasture.id("brushed_until"), Codec.LONG);

	private PastureAttachments() {
	}

	static void init() {
		// Loads the class so the attachment types are registered.
	}
}
