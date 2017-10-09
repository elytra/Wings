package com.elytradev.wings.client;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class DummyMeshDefinition implements ItemMeshDefinition {

	private final ModelResourceLocation loc;
	
	public DummyMeshDefinition(String s) {
		this(new ModelResourceLocation(s));
	}
	
	public DummyMeshDefinition(ModelResourceLocation loc) {
		this.loc = loc;
	}
	
	@Override
	public ModelResourceLocation getModelLocation(ItemStack stack) {
		return loc;
	}

}
