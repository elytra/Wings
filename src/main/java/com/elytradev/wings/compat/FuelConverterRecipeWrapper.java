package com.elytradev.wings.compat;

import com.elytradev.wings.Wings;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FuelConverterRecipeWrapper implements IRecipeWrapper {

	private final Fluid fluid;
	private final ItemStack stack;
	
	private final double rate;
	
	public FuelConverterRecipeWrapper(Fluid fluid, double rate) {
		this.fluid = fluid;
		this.stack = null;
		this.rate = rate;
	}
	
	public FuelConverterRecipeWrapper(ItemStack stack, double amount) {
		this.fluid = null;
		this.stack = stack;
		this.rate = amount;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		if (fluid != null) {
			ingredients.setInput(FluidStack.class, new FluidStack(fluid, 1000));
			ingredients.setOutput(FluidStack.class, new FluidStack(Wings.JET_FUEL, (int)(1000*rate)));
		} else {
			ingredients.setInput(ItemStack.class, stack);
			ingredients.setOutput(FluidStack.class, new FluidStack(Wings.JET_FUEL, (int)rate));
		}
	}
	
	public boolean isFluid() {
		return fluid != null;
	}

}
