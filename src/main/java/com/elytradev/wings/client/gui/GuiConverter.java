package com.elytradev.wings.client.gui;

import com.elytradev.wings.inventory.ContainerConverter;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiConverter extends GuiContainer {

	private static final ResourceLocation BG = new ResourceLocation("wings", "textures/gui/converter.png");
	
	private ContainerConverter container;
	
	public GuiConverter(ContainerConverter container) {
		super(container);
		this.container = container;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(BG);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = container.te.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
		
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(BG);
		drawTexturedModalRect(8, 9, 176, 18, 16, 60);
		drawTexturedModalRect(152, 9, 176, 18, 16, 60);
		
		drawTexturedModalRect(68, 32, 176, 0, container.progress, 17);
	}

}
