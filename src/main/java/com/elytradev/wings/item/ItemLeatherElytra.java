package com.elytradev.wings.item;

public class ItemLeatherElytra extends ItemWings {

	public ItemLeatherElytra() {
		super(1);
	}
	
	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/log_oak";
	}
	
}
