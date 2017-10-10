package com.elytradev.wings.compat;

import java.util.List;
import java.util.Map;

import com.elytradev.wings.ConverterRecipes;
import com.elytradev.wings.Wings;
import com.elytradev.wings.client.gui.GuiConverter;
import com.google.common.collect.Lists;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@JEIPlugin
public class WingsJEIPlugin implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new FuelConverterCategory(registry.getJeiHelpers().getGuiHelper()));
	}
	
	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCatalyst(new ItemStack(Wings.CONVERTER), "wings.fuelConverter");
		List<FuelConverterRecipeWrapper> recipes = Lists.newArrayList();
		for (Map.Entry<ItemStack, Integer> en : ConverterRecipes.itemRecipes.entrySet()) {
			recipes.add(new FuelConverterRecipeWrapper(en.getKey(), en.getValue()));
		}
		for (Map.Entry<ItemStack, Integer> en : ConverterRecipes.itemWildcardRecipes.entrySet()) {
			recipes.add(new FuelConverterRecipeWrapper(en.getKey(), en.getValue()));
		}
		for (Map.Entry<String, Integer> en : ConverterRecipes.oreRecipes.entrySet()) {
			recipes.add(new FuelConverterRecipeWrapper(en.getKey(), en.getValue()));
		}
		for (Map.Entry<FluidStack, Integer> en : ConverterRecipes.fluidRecipes.entrySet()) {
			recipes.add(new FuelConverterRecipeWrapper(en.getKey(), en.getValue()));
		}
		registry.addRecipes(recipes, "wings.fuelConverter");
		
		registry.addRecipeClickArea(GuiConverter.class, 59, 34, 59, 13, "wings.fuelConverter");
	}
	
}
