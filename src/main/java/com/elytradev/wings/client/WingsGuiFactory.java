package com.elytradev.wings.client;

import java.util.Set;

import com.elytradev.wings.client.gui.GuiWingsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class WingsGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new GuiWingsConfig(parentScreen);
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

}
