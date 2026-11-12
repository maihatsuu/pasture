package maihatsuu.pasture.compat;

import maihatsuu.pasture.Pasture;
import maihatsuu.pasture.PastureAttachments;
import maihatsuu.pasture.PastureConfig;
import maihatsuu.pasture.milk.MilkRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.goat.Goat;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade tooltip lines "Milk: ready / dry" and "Scute: ready / regrowing". Loaded only through the
 * {@code jade} entrypoint, so Jade stays optional. It lives in main because the server sends the
 * state (the client doesn't have it).
 */
public class PastureJadePlugin implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerEntityDataProvider(AnimalProducts.INSTANCE, Cow.class);
		registration.registerEntityDataProvider(AnimalProducts.INSTANCE, Goat.class);
		registration.registerEntityDataProvider(AnimalProducts.INSTANCE, Armadillo.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerEntityComponent(AnimalProducts.INSTANCE, Cow.class);
		registration.registerEntityComponent(AnimalProducts.INSTANCE, Goat.class);
		registration.registerEntityComponent(AnimalProducts.INSTANCE, Armadillo.class);
	}

	enum AnimalProducts implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
		INSTANCE;

		private static final ResourceLocation UID = Pasture.id("animal_products");
		private static final String MILK_DRY = "PastureMilkDry";
		private static final String SCUTE_REGROWING = "PastureScuteRegrowing";

		@Override
		public void appendServerData(CompoundTag data, EntityAccessor accessor) {
			Entity entity = accessor.getEntity();
			if (entity instanceof Animal animal && animal.isBaby()) {
				return;
			}
			if (MilkRules.isMilkable(entity) && PastureConfig.get().milkNeedsGrazing) {
				data.putBoolean(MILK_DRY, MilkRules.isDry(entity));
			}
			if (entity instanceof Armadillo) {
				Long until = entity.getAttached(PastureAttachments.BRUSHED_UNTIL);
				data.putBoolean(SCUTE_REGROWING, until != null && entity.level().getGameTime() < until);
			}
		}

		@Override
		public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
			CompoundTag data = accessor.getServerData();
			if (data.contains(MILK_DRY)) {
				String state = data.getBoolean(MILK_DRY) ? "dry" : "ready";
				tooltip.add(Component.translatable("jade.pasture.milk", Component.translatable("jade.pasture.milk." + state)));
			}
			if (data.contains(SCUTE_REGROWING)) {
				String state = data.getBoolean(SCUTE_REGROWING) ? "regrowing" : "ready";
				tooltip.add(Component.translatable("jade.pasture.scute", Component.translatable("jade.pasture.scute." + state)));
			}
		}

		@Override
		public ResourceLocation getUid() {
			return UID;
		}
	}
}
