package com.elytradev.wings.compat;

import java.util.List;

import com.elytradev.wings.Wings;
import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class FuelConverterRecipeWrapper implements IRecipeWrapper {

	private final FluidStack fluid;
	private final ItemStack stack;
	private final String ore;
	
	private final int value;
	
	public FuelConverterRecipeWrapper(FluidStack fluid, int value) {
		this.fluid = fluid.copy();
		this.fluid.amount = 1000;
		this.stack = null;
		this.ore = null;
		this.value = value;
	}
	
	public FuelConverterRecipeWrapper(ItemStack stack, int value) {
		this.fluid = null;
		this.stack = stack;
		this.ore = null;
		this.value = value;
	}
	
	public FuelConverterRecipeWrapper(String ore, int value) {
		this.fluid = null;
		this.stack = null;
		this.ore = ore;
		this.value = value;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		if (fluid != null) {
			ingredients.setInput(FluidStack.class, fluid);
			ingredients.setOutput(FluidStack.class, new FluidStack(Wings.JET_FUEL, value));
		} else if (stack != null) {
			if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				List<List<ItemStack>> li = Lists.newArrayList();
				NonNullList<ItemStack> subItems = NonNullList.create();
				stack.getItem().getSubItems(stack.getItem().getCreativeTab(), subItems);
				li.add(subItems);
				ingredients.setInputLists(ItemStack.class, li);
			} else {
				ingredients.setInput(ItemStack.class, stack);
			}
			ingredients.setOutput(FluidStack.class, new FluidStack(Wings.JET_FUEL, value));
		} else {
			List<List<ItemStack>> li = Lists.newArrayList();
			li.add(OreDictionary.getOres(ore));
			ingredients.setInputLists(ItemStack.class, li);
			ingredients.setOutput(FluidStack.class, new FluidStack(Wings.JET_FUEL, value));
		}
	}
	
	public boolean isFluid() {
		return fluid != null;
	}

}
