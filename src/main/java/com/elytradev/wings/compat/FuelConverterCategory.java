package com.elytradev.wings.compat;

import com.elytradev.wings.tile.TileEntityConverter;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class FuelConverterCategory implements IRecipeCategory<FuelConverterRecipeWrapper> {

	private IDrawable bg;
	private IDrawable fluidOverlay;
	
	private IDrawableAnimated progress;
	
	public FuelConverterCategory(IGuiHelper helper) {
		ResourceLocation res = new ResourceLocation("wings", "textures/gui/converter.png");
		bg = helper.createDrawable(res, 7, 8, 162, 62);
		fluidOverlay = helper.createDrawable(res, 176, 18, 16, 60);
		
		progress = helper.createAnimatedDrawable(helper.createDrawable(res, 176, 0, 63, 17), TileEntityConverter.OPERATION_TIME, StartDirection.LEFT, false);
	}
	
	@Override
	public String getUid() {
		return "wings.fuelConverter";
	}

	@Override
	public String getTitle() {
		return I18n.format("gui.jei.category.wings.fuelConverter");
	}

	@Override
	public String getModName() {
		return "Wings";
	}

	@Override
	public IDrawable getBackground() {
		return bg;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FuelConverterRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiFluidStackGroup fluid = recipeLayout.getFluidStacks();
		IGuiItemStackGroup item = recipeLayout.getItemStacks();
		if (recipeWrapper.isFluid()) {
			fluid.init(0, true, 1, 1, 16, 60, 16000, false, null);
		} else {
			item.init(0, true, 26, 23);
		}
		fluid.init(1, false, 145, 1, 16, 60, 16000, false, null);
		
		fluid.set(ingredients);
		item.set(ingredients);
	}
	
	@Override
	public void drawExtras(Minecraft mc) {
		progress.draw(mc, 49, 24);
		fluidOverlay.draw(mc, 1, 1);
		fluidOverlay.draw(mc, 145, 1);
	}
	
}
