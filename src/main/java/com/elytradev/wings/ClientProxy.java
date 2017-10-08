package com.elytradev.wings;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;

public class ClientProxy extends Proxy {

	@Override
	public void postInit() {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		Map<String, RenderPlayer> renders = manager.getSkinMap();
		for (RenderPlayer render : renders.values()) {
			render.addLayer(new LayerWings(render));
}
	}
	
}
