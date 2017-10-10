package com.elytradev.wings.inventory;


import java.util.Collections;

import com.elytradev.wings.Wings;
import com.elytradev.wings.tile.TileEntityConverter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ContainerConverter extends Container {

	public final TileEntityConverter te;
	public final IInventory playerInventory;
	
	public int progress;
	
	private FluidStack lastInputFluid;
	private FluidStack lastOutputFluid;
	
	public ContainerConverter(InventoryPlayer playerInventory, TileEntityConverter te) {
		this.te = te;
		this.playerInventory = playerInventory;
		
		addSlotToContainer(new Slot(te, 0, 34, 32));
		addSlotToContainer(new Slot(te, 1, 126, 32) {
			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public void updateProgressBar(int id, int data) {
		if (id == 0) {
			progress = data;
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getDistanceSqToCenter(te.getPos()) < 16;
	}
	
	@Override
	public void detectAndSendChanges() {
		if (progress != te.getField(0)) {
			progress = te.getField(0);
			for (IContainerListener icl : listeners) {
				icl.sendWindowProperty(this, 0, te.getField(0));
			}
		}
		if ((lastInputFluid == null ? te.inputTank.getFluid() != null : !lastInputFluid.isFluidStackIdentical(te.inputTank.getFluid())) ||
				(lastOutputFluid == null ? te.outputTank.getFluid() != null : !lastOutputFluid.isFluidStackIdentical(te.outputTank.getFluid()))) {
			lastInputFluid = te.inputTank.getFluid() == null ? null : te.inputTank.getFluid().copy();
			lastOutputFluid = te.outputTank.getFluid() == null ? null : te.outputTank.getFluid().copy();
			te.resync(listeners);
		}
		super.detectAndSendChanges();
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		te.resync(Collections.singletonList(listener));
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack cur = slot.getStack();
			ItemStack copy = cur.copy();

			glass: {
				if (index == 0 || index == 1) {
					if (!mergeItemStack(cur, 1, 37, true)) {
						return ItemStack.EMPTY;
					}
					slot.onSlotChange(cur, copy);
				} else {
					// bucket support
					ItemStack singleCopy = cur.copy();
					singleCopy.setCount(1);
					if (singleCopy.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
						IFluidHandlerItem ifhi = singleCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						if (ifhi.fill(new FluidStack(Wings.JET_FUEL, 90000), false) > 0) {
							if (mergeItemStack(cur, 1, 2, false)) break glass;
						}
						if (ifhi.drain(90000, false) != null) {
							if (mergeItemStack(cur, 0, 1, false)) break glass;
						}
					}
					if (!mergeItemStack(cur, 0, 2, false)) return ItemStack.EMPTY;
				}
			}

			if (cur.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (cur.getCount() == copy.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, cur);
			return copy;
		}

		return ItemStack.EMPTY;
	}
	
}
