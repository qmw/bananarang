package dev.piotr.bananarang;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bananarang implements ModInitializer {
	public static final String MOD_ID = "bananarang";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceKey<Item> BANANA_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "banana"));
	public static final Item BANANA = new BananaItem(new Item.Properties()
			.setId(BANANA_KEY)
			.stacksTo(16));

	public static final ResourceKey<EntityType<?>> BANANA_ENTITY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "banana"));
	public static final EntityType<BananarangEntity> BANANA_ENTITY =
			EntityType.Builder.<BananarangEntity>of(BananarangEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(8)
					.updateInterval(2)
					.build(BANANA_ENTITY_KEY);

	private static final ResourceKey<CreativeModeTab> TOOLS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, BANANA_KEY, BANANA);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, BANANA_ENTITY_KEY, BANANA_ENTITY);
		CreativeModeTabEvents.modifyOutputEvent(TOOLS_TAB).register(output -> output.accept(BANANA));

		LOGGER.info("Bananarang loaded. Duck.");
	}
}
