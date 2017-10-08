package com.elytradev.wings;

public class ItemMetalElytra extends ItemWings {

	public ItemMetalElytra() {
		super(1.15);
	}
	
	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/anvil_base";
	}
	
}
