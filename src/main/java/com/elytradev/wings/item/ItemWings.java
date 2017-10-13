package com.elytradev.wings.item;

import java.util.UUID;

import com.elytradev.wings.Wings;
import com.elytradev.wings.client.render.DummyModel;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemWings extends ItemArmor {

	public static final UUID FLIGHT_SPEED_UUID = UUID.fromString("216a152b-0fc1-4481-bc79-720b15c6a0f5");
	
	private final double flightSpeed;
	
	public ItemWings(double flightSpeed) {
		super(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.CHEST);
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		
		this.flightSpeed = flightSpeed;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
		ItemStack is = player.getHeldItem(handIn);
		EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(is);
		ItemStack cur = player.getItemStackFromSlot(slot);

		if (cur.isEmpty()) {
			player.setItemStackToSlot(slot, is.copy());
			is.setCount(0);
			return new ActionResult<>(EnumActionResult.SUCCESS, is);
		} else {
			return new ActionResult<>(EnumActionResult.FAIL, is);
		}
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> mm = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.CHEST) {
			mm.put(Wings.FLIGHT_SPEED.getName(), new AttributeModifier(FLIGHT_SPEED_UUID, "Flight speed", flightSpeed, 1));
		}
		return mm;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
		return DummyModel.INSTANCE;
	}

	public String getBaseMaterial() {
		return "missingno";
	}
	
	public boolean hasThruster() {
		return false;
	}

	/**
	 * Burn N units of fuel. The input amount does not need to directly map to
	 * an amount of fuel consumed by the implementation.
	 * @param stack The stack to affect
	 * @param amt The amount of fuel units to burn
	 * @param simulate If {@code true}, just check if enough fuel is present, and don't burn it
	 * @return {@code true} if enough fuel was present (and burned, if simulate was false), {@code false} otherwise
	 */
	public boolean burnFuel(ItemStack stack, int amt, boolean simulate) { return false; }

	public boolean isFuelDepleted(ItemStack stack) { return true; }
	
}
