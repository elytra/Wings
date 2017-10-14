package com.elytradev.wings.item;

import java.util.List;

import com.elytradev.wings.Wings;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
			return getFluidContents(stack).getTankProperties();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource.getFluid() != Wings.JET_FUEL) return 0;
			FluidTank tank = getFluidContents(stack);
			int rtrn = tank.fill(resource, doFill);
			setFluidContents(stack, tank);
			return rtrn;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			FluidTank tank = getFluidContents(stack);
			FluidStack rtrn = tank.drain(resource, doDrain);
			setFluidContents(stack, tank);
			return rtrn;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			FluidTank tank = getFluidContents(stack);
			FluidStack rtrn = tank.drain(maxDrain, doDrain);
			setFluidContents(stack, tank);
			return rtrn;
		}

		@Override
		public ItemStack getContainer() {
			return stack;
		}

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
		return 0xEEFF00;
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
		tooltip.add(getFluidContents(stack).getFluidAmount()+"/"+FUEL_CAPACITY+" mB");
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		FluidTank content = getFluidContents(stack);
		if (content.getFluidAmount() <= 0) {
			return 1;
		}
		return 1-(content.getFluidAmount() / (double)content.getCapacity());
	}
	
	public FluidTank getFluidContents(ItemStack stack) {
		FluidTank tank = new FluidTank(FUEL_CAPACITY);
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Fluid", NBT.TAG_COMPOUND)) {
			tank.readFromNBT(stack.getTagCompound().getCompoundTag("Fluid"));
		}
		return tank;
	}
	
	public void setFluidContents(ItemStack stack, FluidTank fluid) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setTag("Fluid", fluid.writeToNBT(new NBTTagCompound()));
	}
	
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new CapabilityProvider(stack);
	}
	
	@Override
	public boolean burnFuel(ItemStack stack, int amt, boolean simulate) {
		FluidTank tank = getFluidContents(stack);
		int mbAmt = amt;
		FluidStack res = tank.drain(mbAmt, true);
		if (res == null || res.amount != mbAmt) {
			return false;
		}
		if (!simulate) {
			setFluidContents(stack, tank);
		}
		return true;
	}
	
	@Override
	public boolean isFuelDepleted(ItemStack stack) {
		return getFluidContents(stack).getFluidAmount() < 5;
	}
	
}
