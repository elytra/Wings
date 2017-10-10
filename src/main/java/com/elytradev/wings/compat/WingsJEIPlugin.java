package com.elytradev.wings.compat;

import java.util.List;
import java.util.Map;

import com.elytradev.wings.Wings;
import com.elytradev.wings.tile.TileEntityConverter;
import com.google.common.collect.Lists;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidRegistry;

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
		for (Map.Entry<String, Double> en : TileEntityConverter.FLUID_CONVERSION_RATES.entrySet()) {
			if (FluidRegistry.isFluidRegistered(en.getKey())) {
				recipes.add(new FuelConverterRecipeWrapper(FluidRegistry.getFluid(en.getKey()), en.getValue()));
			}
		}
		for (ItemStack is : registry.getIngredientRegistry().getIngredients(ItemStack.class)) {
			if (TileEntityFurnace.isItemFuel(is) && is.getItem() != Items.LAVA_BUCKET) {
				recipes.add(new FuelConverterRecipeWrapper(is, TileEntityFurnace.getItemBurnTime(is) * TileEntityConverter.MB_PER_FURNACE_TICK));
			}
		}
		registry.addRecipes(recipes, "wings.fuelConverter");
	}
	
}
