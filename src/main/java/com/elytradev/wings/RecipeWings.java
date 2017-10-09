package com.elytradev.wings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elytradev.wings.item.ItemWings;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeWings extends ShapedOreRecipe {

	private static final Logger log = LogManager.getLogger("Wings/RecipeWings");
	
	public RecipeWings(ResourceLocation group, Block result, Object... recipe) {
		super(group, result, recipe);
	}

	public RecipeWings(ResourceLocation group, Item result, Object... recipe) {
		super(group, result, recipe);
	}

	public RecipeWings(ResourceLocation group, ItemStack result,
			Object... recipe) {
		super(group, result, recipe);
	}

	public RecipeWings(ResourceLocation group, ItemStack result,
			ShapedPrimer primer) {
		super(group, result, primer);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		System.out.println("yo");
		ItemStack leftBanner = null;
		ItemStack rightBanner = null;
		ItemStack previousWings = null;
		for (int i = 0; i < 9; i++) {
			ItemStack is = inv.getStackInSlot(i);
			if (is.getItem() instanceof ItemBanner) {
				if (leftBanner == null) {
					leftBanner = is;
				} else if (rightBanner == null) {
					rightBanner = is;
				} else {
					log.error("Too many banners in wing recipe, returning dirt");
					return new ItemStack(Blocks.DIRT);
				}
			} else if (is.getItem() instanceof ItemWings) {
				previousWings = is;
				break;
			}
		}
		System.out.println(leftBanner);
		System.out.println(rightBanner);
		System.out.println(previousWings);
		if (previousWings == null && (leftBanner == null || rightBanner == null)) {
			log.error("Not enough banners in wing recipe, returning dirt");
			return new ItemStack(Blocks.DIRT);
		}
		ItemStack out = super.getCraftingResult(inv);
		if (previousWings != null) {
			out.setTagCompound(previousWings.getTagCompound().copy());
		} else if (leftBanner != null && rightBanner != null) {
			if (!out.hasTagCompound()) {
				out.setTagCompound(new NBTTagCompound());
			}
			if (leftBanner.hasTagCompound()) {
				NBTTagCompound nbt = leftBanner.getTagCompound().getCompoundTag("BlockEntityTag");
				out.getTagCompound().setTag("LeftWing", nbt);
			}
			if (rightBanner.hasTagCompound()) {
				NBTTagCompound nbt = rightBanner.getTagCompound().getCompoundTag("BlockEntityTag");
				out.getTagCompound().setTag("RightWing", nbt);
			}
		}
		return out;
	}

}
