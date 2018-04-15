package com.elytradev.wings.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMetalElectricElytra extends ItemWings {

	public static final int FU_CAPACITY = 40000;
	
	private class CapabilityProvider implements ICapabilityProvider, IEnergyStorage {

		private final ItemStack stack;
		
		public CapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY) {
				return true;
			}
			return false;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY) {
				return (T)this;
			}
			return null;
		}

		@Override
		public boolean canExtract() {
			return false;
		}
		
		@Override
		public boolean canReceive() {
			return false;
		}
		
		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}
		
		@Override
		public int getEnergyStored() {
			return ItemMetalElectricElytra.this.getEnergyStored(stack);
		}
		
		@Override
		public int getMaxEnergyStored() {
			return FU_CAPACITY;
		}
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int amt = Math.min(getMaxEnergyStored()-getEnergyStored(), maxReceive);
			if (!simulate) {
				setEnergyStored(stack, getEnergyStored()+amt);
			}
			return amt;
		}

	}


	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/iron_block";
	}
	
	@Override
	public boolean hasThruster() {
		return true;
	}
	
	@Override
	public boolean hasAfterburner() {
		return false;
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return 0xD01010;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("hint.jet"));
		tooltip.add(I18n.format("hint.unbreakable"));
		tooltip.add(I18n.format("hint.energy"));
		tooltip.add(getEnergyStored(stack)+"/"+FU_CAPACITY+" FU");
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		int content = getEnergyStored(stack);
		if (content <= 0) {
			return 1;
		}
		return 1-(content / (double)FU_CAPACITY);
	}
	
	public int getEnergyStored(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Energy", NBT.TAG_ANY_NUMERIC)) {
			return stack.getTagCompound().getInteger("Energy");
		}
		return 0;
	}
	
	public void setEnergyStored(ItemStack stack, int energy) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("Energy", energy);
	}
	
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
	
	@Override
	public boolean burnFuel(ItemStack stack, int amt, boolean simulate) {
		int energy = getEnergyStored(stack);
		int fuAmt = amt*5;
		if (energy < fuAmt) {
			return false;
		}
		if (!simulate) {
			setEnergyStored(stack, energy-fuAmt);
		}
		return true;
	}
	
	@Override
	public boolean isFuelDepleted(ItemStack stack) {
		return getEnergyStored(stack) < 25;
	}
	
}
