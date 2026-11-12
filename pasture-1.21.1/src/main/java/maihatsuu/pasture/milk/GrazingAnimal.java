package maihatsuu.pasture.milk;

/**
 * Duck interface added to {@code Animal} by {@code AnimalMixin}: the client-side start of the
 * grazing animation, set when the server sends {@link Grazing#EAT_EVENT}.
 */
public interface GrazingAnimal {
	/** {@code tickCount} when the animation started, or {@link Integer#MIN_VALUE} if never. */
	int pasture$eatStartTick();
}
