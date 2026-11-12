package maihatsuu.pasture.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import maihatsuu.pasture.milk.Grazing;
import maihatsuu.pasture.milk.GrazingAnimal;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;

/**
 * Cows, mooshrooms and goats have no eating animation, so this lowers the head while grazing. Cows
 * use the sheep's curve ({@code Sheep.getHeadEatPositionScale} / {@code getHeadEatAngleScale}). CowModel
 * doesn't override setupAnim, so the hook is on QuadrupedModel, filtered to cows and goats.
 */
@Mixin(QuadrupedModel.class)
abstract class QuadrupedModelMixin {
	/** Goat head tilt while grazing, in radians (about 70 degrees), and how far the neck drops. */
	@Unique
	private static final float GOAT_HEAD_TILT = 1.2F;
	@Unique
	private static final float GOAT_HEAD_DROP = 1.0F;

	@Shadow
	@Final
	protected ModelPart head;

	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void pasture$grazeHead(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, CallbackInfo ci) {
		boolean goat = entity instanceof Goat;
		if (!(entity instanceof Cow || goat) || !(entity instanceof GrazingAnimal grazer)) {
			return;
		}
		// The model is shared by every animal of its kind, so the head height is set on every frame.
		head.y = head.getInitialPose().y;

		int start = grazer.pasture$eatStartTick();
		if (start == Integer.MIN_VALUE) {
			return;
		}
		// Ticks left in the animation, counting down from 40 like the sheep's eatAnimationTick.
		float left = Grazing.EAT_TICKS - (ageInTicks - start);
		if (left <= 0.0F || left > Grazing.EAT_TICKS) {
			return;
		}

		float position;
		if (left >= 4.0F && left <= 36.0F) {
			position = 1.0F;
		} else {
			position = left < 4.0F ? left / 4.0F : (Grazing.EAT_TICKS - left) / 4.0F;
		}
		float angle = (float) (Math.PI / 5);
		if (left > 4.0F && left <= 36.0F) {
			angle += 0.21991149F * Mth.sin((left - 4.0F) / 32.0F * 28.7F);
		}

		if (goat) {
			// The goat's head pivots low in its neck, with the head above and in front of it, so it
			// needs no drop and a much deeper tilt to reach the ground. Eased in and out from the
			// vanilla pose. GoatModel applies its ram tilt after this, so ramming still wins.
			float bob = left > 4.0F && left <= 36.0F ? 0.12F * Mth.sin((left - 4.0F) / 32.0F * 28.7F) : 0.0F;
			head.y += position * GOAT_HEAD_DROP;
			head.xRot = Mth.lerp(position, head.xRot, GOAT_HEAD_TILT + bob);
			return;
		}
		head.y += position * 9.0F;
		head.xRot = angle;
	}
}
