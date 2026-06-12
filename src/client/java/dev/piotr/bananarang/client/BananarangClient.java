package dev.piotr.bananarang.client;

import dev.piotr.bananarang.Bananarang;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class BananarangClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(Bananarang.BANANA_ENTITY, ThrownItemRenderer::new);
	}
}
