package com.elytradev.wings;

public class ItemMetalJetElytra extends ItemWings {

	public ItemMetalJetElytra() {
		super(0.85);
	}

	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/anvil_base";
	}
	
	@Override
	public boolean hasBooster() {
		return true;
	}
	
}
