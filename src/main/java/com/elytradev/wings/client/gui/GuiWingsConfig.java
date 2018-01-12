package com.elytradev.wings.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiWingsConfig extends GuiScreen {

	private final GuiScreen parent;
	
	public GuiWingsConfig(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiButton(0, (width-200)/2, height-26, I18n.format("gui.done")));
		
		addButton(new GuiButton(1, (width/2)-85, 24, 80, 20, I18n.format("gui.wings.config.camera.cameraspace")));
		addButton(new GuiButton(2, (width/2)-85, 48, 80, 20, I18n.format("gui.wings.config.camera.worldspace")));
		addButton(new GuiButton(3, (width/2)-85, 72, 80, 20, I18n.format("gui.wings.config.eager")));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 0) {
			mc.displayGuiScreen(parent);
			if (parent == null) {
				mc.setIngameFocus();
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		String title = I18n.format("gui.wings.config.title");
		fontRenderer.drawString(title, (width-fontRenderer.getStringWidth(title))/2, 8, -1);
		
		drawButtonTitle(1, I18n.format("gui.wings.config.keyboardCam"));
		drawButtonTitle(2, I18n.format("gui.wings.config.mouseCam"));
		drawButtonTitle(3, I18n.format("gui.wings.config.startGliding"));
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void drawButtonTitle(int id, String str) {
		for (GuiButton gb : buttonList) {
			if (gb.id == id) {
				fontRenderer.drawString(str, gb.x-fontRenderer.getStringWidth(str)-4, gb.y+6, -1);
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(parent);
			if (parent == null) {
				mc.setIngameFocus();
			}
		}
	}
	
}
