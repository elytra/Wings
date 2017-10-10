package com.elytradev.wings;

import java.util.Map;

import com.google.common.collect.Maps;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class ConverterRecipes {

	public static final Map<FluidStack, Integer> fluidRecipes = new TCustomHashMap<>(new HashingStrategy<FluidStack>() {
		private static final long serialVersionUID = 160875649131815330L;

		@Override
		public int computeHashCode(FluidStack object) {
			int out = 1;
			out = (31 * out) + object.getFluid().hashCode();
			out = (31 * out) + (object.tag == null ? 0 : object.tag.hashCode());
			return 0;
		}

		@Override
		public boolean equals(FluidStack o1, FluidStack o2) {
			return o1.isFluidEqual(o2);
		}
		
	});
	public static final Map<ItemStack, Integer> itemRecipes = new TCustomHashMap<>(new HashingStrategy<ItemStack>() {
		private static final long serialVersionUID = -7829867874540744821L;

		@Override
		public int computeHashCode(ItemStack object) {
			int out = 1;
			out = (31 * out) + object.getItem().hashCode();
			out = (31 * out) + object.getMetadata();
			out = (31 * out) + (object.hasTagCompound() ? object.getTagCompound().hashCode() : 0);
			return out;
		}

		@Override
		public boolean equals(ItemStack o1, ItemStack o2) {
			return ItemStack.areItemsEqual(o1, o2) && ItemStack.areItemStackTagsEqual(o1, o2);
		}
		
	});
	public static final Map<ItemStack, Integer> itemWildcardRecipes = new TCustomHashMap<>(new HashingStrategy<ItemStack>() {
		private static final long serialVersionUID = -7829867874540744821L;

		@Override
		public int computeHashCode(ItemStack object) {
			int out = 1;
			out = (31 * out) + object.getItem().hashCode();
			out = (31 * out) + (object.hasTagCompound() ? object.getTagCompound().hashCode() : 0);
			return out;
		}

		@Override
		public boolean equals(ItemStack o1, ItemStack o2) {
			return ItemStack.areItemsEqualIgnoreDurability(o1, o2) && ItemStack.areItemStackTagsEqual(o1, o2);
		}
		
	});
	public static final Map<String, Integer> oreRecipes = Maps.newHashMap();
	
	
	public static int getValue(ItemStack item) {
		if (item == null || item.isEmpty()) return 0;
		if (itemRecipes.containsKey(item)) {
			return itemRecipes.get(item);
		}
		if (itemWildcardRecipes.containsKey(item)) {
			return itemWildcardRecipes.get(item);
		}
		int max = 0;
		int[] oreIds = OreDictionary.getOreIDs(item);
		for (int i : oreIds) {
			String name = OreDictionary.getOreName(i);
			if (oreRecipes.containsKey(name)) {
				max = Math.max(oreRecipes.get(name), max);
			}
		}
		return max;
	}
	
	public static int getValue(FluidStack fluid) {
		if (fluid == null) return 0;
		if (fluidRecipes.containsKey(fluid)) {
			return fluidRecipes.get(fluid);
		}
		return 0;
	}
	
	
	
	/**
	 * @param fluid the input fluid
	 * @param amount the amount of jet fuel to produce, in mB, per 1000 mB of the input fluid
	 */
	public static void registerFluid(Fluid fluid, int amount) {
		fluidRecipes.put(new FluidStack(fluid, 1), amount);
	}
	
	public static void registerFluid(FluidStack fluid, int amount) {
		fluidRecipes.put(fluid, amount);
	}
	
	public static void registerFluid(String fluid, int amount) {
		if (FluidRegistry.isFluidRegistered(fluid)) {
			registerFluid(FluidRegistry.getFluid(fluid), amount);
		}
	}
	
	/**
	 * @param item the input item
	 * @param amount the amount of jet fuel to produce, in mB, per 1 item
	 */
	public static void registerItem(Item item, int amount) {
		itemWildcardRecipes.put(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE), amount);
	}
	
	public static void registerItem(Block block, int amount) {
		itemWildcardRecipes.put(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), amount);
	}
	
	public static void registerItem(ItemStack stack, int amount) {
		if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
			itemWildcardRecipes.put(stack, amount);
		} else {
			itemRecipes.put(stack, amount);
		}
	}

	/**
	 * @param ore the OreDictionary name of the input item
	 * @param amount the amount of jet fuel to produce, in mB, per 1 item
	 */
	public static void registerItem(String ore, int amount) {
		oreRecipes.put(ore, amount);
	}
	
}
