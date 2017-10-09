package com.elytradev.wings.item;

import com.elytradev.wings.Wings;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ItemMetalJetElytra extends ItemWings {

	public static final int FUEL_CAPACITY = 16000;
	
	private class CapabilityProvider implements ICapabilityProvider, IFluidHandlerItem {

		private final ItemStack stack;
		
		public CapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
				return true;
			}
			return false;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
				return (T)this;
			}
			return null;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return new IFluidTankProperties[] {
					new FluidTankProperties(getFluidContents(stack), FUEL_CAPACITY)
			};
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource.getFluid() == Wings.JET_FUEL) {
				FluidStack cur = getFluidContents(stack);
				if (FluidStack.areFluidStackTagsEqual(resource, cur)) {
					int amt = Math.min(FUEL_CAPACITY-cur.amount, resource.amount);
					if (doFill) {
						cur.amount -= amt;
						setFluidContents(stack, cur);
					}
					return amt;
				}
			}
			return 0;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			return null;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			return null;
		}

		@Override
		public ItemStack getContainer() {
			return stack;
		}

	}


	public ItemMetalJetElytra() {
		super(0.85);
	}

	@Override
	public String getBaseMaterial() {
		return "minecraft:blocks/anvil_base";
	}
	
	@Override
	public boolean hasThruster() {
		return true;
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return 0xAF9404;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		FluidStack content = getFluidContents(stack);
		if (content == null) {
			return 1;
		}
		return 1-(content.amount / (double)FUEL_CAPACITY);
	}
	
	public FluidStack getFluidContents(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Fluid", NBT.TAG_COMPOUND)) {
			return FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("Fluid"));
		}
		return null;
	}
	
	public void setFluidContents(ItemStack stack, FluidStack fluid) {
		if (fluid != null) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setTag("Fluid", fluid.writeToNBT(new NBTTagCompound()));
		} else {
			if (stack.hasTagCompound()) {
				stack.getTagCompound().removeTag("Fluid");
			}
		}
	}
	
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
	
}
