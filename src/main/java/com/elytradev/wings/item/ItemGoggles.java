package com.elytradev.wings.item;

import java.util.UUID;

import com.elytradev.wings.Wings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemGoggles extends ItemArmor {

	public static final UUID FLIGHT_SPEED_BONUS_UUID = UUID.fromString("1ba11424-6e21-49e9-967c-c2b706dca2b2");
	
	public ItemGoggles() {
		super(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		setMaxStackSize(1);
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "wings:textures/models/armor/goggles.png";
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> mm = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.HEAD) {
			mm.put(Wings.FLIGHT_SPEED.getName(), new AttributeModifier(FLIGHT_SPEED_BONUS_UUID, "Goggles flight speed bonus", 0.15, 1));
		}
		return mm;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}
	
}
