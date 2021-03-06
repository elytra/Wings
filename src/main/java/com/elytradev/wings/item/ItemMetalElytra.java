package com.elytradev.wings.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMetalElytra extends ItemWings {

	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/anvil_base";
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(I18n.format("hint.glide"));
		tooltip.add(I18n.format("hint.unbreakable"));
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}
	
}
