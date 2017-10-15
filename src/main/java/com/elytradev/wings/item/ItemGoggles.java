package com.elytradev.wings.item;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("item.wings.goggles.hint"));
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers( EntityEquipmentSlot slot, ItemStack stack) {
		return HashMultimap.create();
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}
	
}
