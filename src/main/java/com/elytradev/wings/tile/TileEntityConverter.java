package com.elytradev.wings.tile;

import com.elytradev.concrete.inventory.ConcreteFluidTank;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileEntityConverter extends TileEntity implements IInventory, IFluidHandler, ITickable {

	public ItemStack inputItem = ItemStack.EMPTY;
	
	public ConcreteFluidTank inputTank;
	public ConcreteFluidTank outputTank;
	
	public TileEntityConverter() {
		inputTank = new ConcreteFluidTank(16000);
		outputTank = new ConcreteFluidTank(16000);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		compound.setTag("InputItem", inputItem.serializeNBT());
		compound.setTag("InputTank", inputTank.writeToNBT(new NBTTagCompound()));
		compound.setTag("OutputTank", outputTank.writeToNBT(new NBTTagCompound()));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (compound.hasKey("InputItem", NBT.TAG_COMPOUND)) {
			inputItem = new ItemStack(compound.getCompoundTag("InputItem"));
		} else {
			inputItem = ItemStack.EMPTY;
		}
		inputTank.readFromNBT(compound.getCompoundTag("InputTank"));
		outputTank.readFromNBT(compound.getCompoundTag("OutputTank"));
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		
	}
	
	@Override
	public String getName() {
		return "container.wings.converter";
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(getName());
	}
	
	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return getStackInSlot(0).isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		return inputItem;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		ItemStack rtrn = inputItem.splitStack(count);
		markDirty();
		return rtrn;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		ItemStack i = inputItem;
		inputItem = ItemStack.EMPTY;
		markDirty();
		return i;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		inputItem = stack;
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return player.getDistanceSqToCenter(getPos()) < 16;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		return true;
	}

	@Override
	public int getField(int id) {
		if (id == 0) {
			return (int)((System.currentTimeMillis()%6300)/100);
		}
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 1;
	}

	@Override
	public void clear() {
		inputItem = ItemStack.EMPTY;
		markDirty();
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {
				inputTank.getTankProperties()[0],
				outputTank.getTankProperties()[0]
		};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		int rtrn = inputTank.fill(resource, doFill);
		if (doFill) markDirty();
		return rtrn;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		FluidStack rtrn = outputTank.drain(resource, doDrain);
		if (doDrain) markDirty();
		return rtrn;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		FluidStack rtrn = outputTank.drain(maxDrain, doDrain);
		if (doDrain) markDirty();
		return rtrn;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new InvWrapper(this);
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T)this;
		}
		return super.getCapability(capability, facing);
	}

}
