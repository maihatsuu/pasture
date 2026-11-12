package maihatsuu.pasture.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Works like a milk bucket (clears all effects), but stacks and leaves a glass bottle. */
public class MilkBottleItem extends Item {
	private static final int DRINK_DURATION = 32;

	public MilkBottleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		if (!level.isClientSide) {
			entity.removeAllEffects();
		}

		if (entity instanceof Player player) {
			// Like potions: in creative the milk bottle isn't used up and no glass bottle is added.
			if (player.hasInfiniteMaterials()) {
				return stack;
			}
			return ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE), false);
		}
		stack.consume(1, entity);
		return stack;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return DRINK_DURATION;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(level, player, hand);
	}
}
