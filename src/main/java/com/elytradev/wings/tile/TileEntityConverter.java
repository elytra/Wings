package com.elytradev.wings.tile;

import java.util.List;

import com.elytradev.wings.ConverterRecipes;
import com.elytradev.wings.Wings;
import com.google.common.base.Predicates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityConverter extends TileEntity implements IInventory, IFluidHandler, ITickable, ISidedInventory {
	
	public static final int OPERATION_TIME = 80;
	
	public ItemStack inputItem = ItemStack.EMPTY;
	public ItemStack outputItem = ItemStack.EMPTY;
	
	private ItemStack inputItemAtOperationStart = ItemStack.EMPTY;
	
	public FluidTank inputTank;
	public FluidTank outputTank;
	
	public int operationProgress;
	
	public TileEntityConverter() {
		inputTank = new FluidTank(16000);
		outputTank = new FluidTank(16000);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("InputItem", NBT.TAG_COMPOUND)) {
			inputItem = new ItemStack(compound.getCompoundTag("InputItem"));
		} else {
			inputItem = ItemStack.EMPTY;
		}
		if (compound.hasKey("OutputItem", NBT.TAG_COMPOUND)) {
			outputItem = new ItemStack(compound.getCompoundTag("OutputItem"));
		} else {
			outputItem = ItemStack.EMPTY;
		}
		inputTank.readFromNBT(compound.getCompoundTag("InputTank"));
		outputTank.readFromNBT(compound.getCompoundTag("OutputTank"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("InputItem", inputItem.serializeNBT());
		compound.setTag("OutputItem", outputItem.serializeNBT());
		compound.setTag("InputTank", inputTank.writeToNBT(new NBTTagCompound()));
		compound.setTag("OutputTank", outputTank.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if (hasWorld() && !getWorld().isRemote) {
			if (getWorld().isBlockPowered(getPos())) return;
			if (inputTank.getFluid() != null && inputTank.getFluid().getFluid() == Wings.JET_FUEL) {
				FluidUtil.tryFluidTransfer(outputTank, inputTank, inputTank.getCapacity(), true);
			}
			if (!inputItem.isEmpty()) {
				FluidActionResult far = FluidUtil.tryEmptyContainer(inputItem, this, inputTank.getCapacity(), null, true);
				if (far.success) {
					inputItem = far.result;
					markDirty();
				}
			}
			if (operationProgress <= 0) {
				boolean checkFluid = true;
				int value = ConverterRecipes.getValue(inputItem);
				if (value > 0 && outputTank.getFluidAmount() <= outputTank.getCapacity()-value) {
					operationProgress = 1;
					inputItemAtOperationStart = inputItem;
					checkFluid = false;
				}
				if (checkFluid) {
					value = ConverterRecipes.getValue(inputTank.getFluid());
					if (value > 0 && outputTank.getFluidAmount() <= outputTank.getCapacity()-value) {
						operationProgress = 1;
						inputItemAtOperationStart = inputItem;
					}
				}
			} else {
				operationProgress++;
				if (inputTank.getFluidAmount() <= 0 && (inputItem.isEmpty() || TileEntityFurnace.getItemBurnTime(inputItem) == 0)) {
					operationProgress = 0;
					return;
				}
				if (!ItemStack.areItemsEqual(inputItem, inputItemAtOperationStart) || !ItemStack.areItemStackTagsEqual(inputItem, inputItemAtOperationStart)) {
					operationProgress = 0;
					return;
				}
				if (operationProgress >= OPERATION_TIME) {
					operationProgress = 0;
					boolean checkFluid = true;
					int value = ConverterRecipes.getValue(inputItem);
					if (!inputItem.isEmpty()) {
						outputTank.fill(new FluidStack(Wings.JET_FUEL, value), true);
						inputItem.shrink(1);
						markDirty();
						checkFluid = false;
					}
					if (checkFluid && inputTank.getFluidAmount() > 0) {
						value = ConverterRecipes.getValue(inputTank.getFluid());
						if (value > 0) {
							FluidStack out = inputTank.drain(1000, true);
							if (out != null) {
								outputTank.fill(new FluidStack(Wings.JET_FUEL, (int)(value*(out.amount/1000D))), true);
								markDirty();
							}
						}
					}
				}
			}
			if (!outputItem.isEmpty()) {
				FluidActionResult far = FluidUtil.tryFillContainer(outputItem, outputTank, outputTank.getFluidAmount(), null, true);
				if (far.success) {
					outputItem = far.result;
					markDirty();
				}
			}
		}
	}
	
	public void resync(List<? super EntityPlayerMP> players) {
		if (hasWorld() && getWorld() instanceof WorldServer) {
			WorldServer ws = (WorldServer)getWorld();
			Chunk c = getWorld().getChunkFromBlockCoords(getPos());
			SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
			if (players == null) {
				for (EntityPlayerMP player : getWorld().getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
					if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.x, c.z)) {
						player.connection.sendPacket(packet);
					}
				}
			} else {
				for (Object o : players) {
					if (o instanceof EntityPlayerMP) {
						((EntityPlayerMP)o).connection.sendPacket(packet);
					}
				}
			}
		}
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
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
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return inputItem.isEmpty() && outputItem.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		switch (index) {
			case 0: return inputItem;
			case 1: return outputItem;
			default: throw new IndexOutOfBoundsException(Integer.toString(index));
		}
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack rtrn = getStackInSlot(index).splitStack(count);
		markDirty();
		return rtrn;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack i;
		switch (index) {
			case 0:
				i = inputItem;
				inputItem = ItemStack.EMPTY;
				break;
			case 1:
				i = outputItem;
				outputItem = ItemStack.EMPTY;
				break;
			default:
				throw new IndexOutOfBoundsException(Integer.toString(index));
		}
		markDirty();
		return i;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		switch (index) {
			case 0:
				inputItem = stack;
				break;
			case 1:
				outputItem = stack;
				break;
			default:
				throw new IndexOutOfBoundsException(Integer.toString(index));
		}
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
		if (index < 0 || index > 1) throw new IndexOutOfBoundsException(Integer.toString(index));
		return true;
	}

	@Override
	public int getField(int id) {
		if (id == 0) {
			return operationProgress;
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
		outputItem = ItemStack.EMPTY;
		markDirty();
	}
	
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index == 1 && direction == EnumFacing.DOWN;
	}
	
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index == 0 && direction == EnumFacing.UP;
	}
	
	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (side == EnumFacing.UP) {
			return new int[] { 0 };
		}
		if (side == EnumFacing.DOWN) {
			return new int[] { 1 };
		}
		return new int[0];
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
		if (resource == null) return 0;
		if (ConverterRecipes.getValue(resource) <= 0) return 0;
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
			if (facing == null) return (T)new InvWrapper(this);
			return (T)new SidedInvWrapper(this, facing);
		} else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T)this;
		}
		return super.getCapability(capability, facing);
	}

}
