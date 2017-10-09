package com.elytradev.wings.client.render;

import com.elytradev.wings.item.ItemWings;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class WingsTileEntityItemStackRenderer extends TileEntityItemStackRenderer {

	private final TileEntityItemStackRenderer delegate;
	
	public WingsTileEntityItemStackRenderer(TileEntityItemStackRenderer delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void renderByItem(ItemStack stack) {
		renderByItem(stack, 1);
	}
	
	@Override
	public void renderByItem(ItemStack stack, float partialTicks) {
		if (stack.getItem() instanceof ItemWings) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(1f, 1.35f, 0);
			GlStateManager.rotate(-10f, 0, 0, 1);
			GlStateManager.rotate(125f, 0, 1, 0);
			GlStateManager.rotate(180f, 1, 0, 0);
			LayerWings.drawWings(stack, 10f);
			GlStateManager.popMatrix();
		} else {
			delegate.renderByItem(stack, partialTicks);
		}
	}
	
}
