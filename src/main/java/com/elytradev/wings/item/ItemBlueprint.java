package com.elytradev.wings.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBlueprint extends Item {

	public ItemBlueprint() {
		setMaxStackSize(1);
	}
	
	@Override
	public boolean hasContainerItem() {
		return true;
	}
	
	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return !stack.isEmpty();
	}
	
	@Override
	public Item getContainerItem() {
		return this;
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack.copy();
	}
	
}
