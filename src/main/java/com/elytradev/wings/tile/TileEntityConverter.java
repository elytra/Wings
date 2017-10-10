package com.elytradev.wings.tile;

import com.elytradev.wings.Wings;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileEntityConverter extends TileEntity implements IInventory, IFluidHandler, ITickable {

	public static final ImmutableMap<String, Double> FLUID_CONVERSION_RATES = ImmutableMap.<String, Double>builder()
			.put("lava", 0.15)
			.put("oil", 0.5)
			.put("fuel", 0.75)
			.put("gasoline", 0.75)
			.put("pyrotheum", 0.65)
			.put("crude_oil", 0.35)
			.put("tree_oil", 0.25)
			.put("creosote", 0.1)
			.put("refined_oil", 0.65)
			.put("refined_fuel", 0.85)
			.put("coal", 0.5)
			.put("canolaoil", 0.25)
			.put("crystaloil", 0.5)
			.put("empoweredoil", 0.75)
			.put("kerosene", 0.95)
			.put("wings.jetfuel", 1.0)
			.put("pain", 0.01)
			.put("soylent", 0.005)
			.build();
	
	public static final ImmutableMap<String, Double> ITEM_ORE_CONVERSION_RATES = ImmutableMap.<String, Double>builder()
			.build();
	
	public static final double MB_PER_FURNACE_TICK = 0.05;
	public static final int OPERATION_TIME = 160;
	
	public ItemStack inputItem = ItemStack.EMPTY;
	
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
		inputTank.readFromNBT(compound.getCompoundTag("InputTank"));
		outputTank.readFromNBT(compound.getCompoundTag("OutputTank"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("InputItem", inputItem.serializeNBT());
		compound.setTag("InputTank", inputTank.writeToNBT(new NBTTagCompound()));
		compound.setTag("OutputTank", outputTank.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if (hasWorld() && !getWorld().isRemote) {
			if (!inputItem.isEmpty()) {
				FluidActionResult far = FluidUtil.tryEmptyContainer(inputItem, inputTank, inputTank.getCapacity(), null, true);
				if (far.success) {
					inputItem = far.result;
					markDirty();
				}
			}
			if (operationProgress <= 0) {
				int burnTime = TileEntityFurnace.getItemBurnTime(inputItem);
				if (burnTime > 0 && inputItem.getItem() != Items.LAVA_BUCKET) {
					operationProgress = 1;
				}
				if (inputTank.getFluidAmount() > 0 && outputTank.getFluidAmount() < outputTank.getCapacity()
						&& FLUID_CONVERSION_RATES.containsKey(FluidRegistry.getFluidName(inputTank.getFluid()))) {
					operationProgress = 1;
				}
			} else {
				operationProgress++;
				if (inputTank.getFluidAmount() <= 0 && (inputItem.isEmpty() || TileEntityFurnace.getItemBurnTime(inputItem) == 0)) {
					operationProgress = 0;
					return;
				}
				if (inputItem.isEmpty() && inputTank.getFluid() != null && inputTank.getFluid().getFluid() == Wings.JET_FUEL) {
					operationProgress = OPERATION_TIME;
				}
				if (operationProgress >= OPERATION_TIME) {
					operationProgress = 0;
					if (!inputItem.isEmpty() && inputItem.getItem() != Items.LAVA_BUCKET) {
						int burnTime = TileEntityFurnace.getItemBurnTime(inputItem);
						if (burnTime > 0) {
							outputTank.fill(new FluidStack(Wings.JET_FUEL, (int) (burnTime*MB_PER_FURNACE_TICK)), true);
							inputItem.shrink(1);
							markDirty();
							return;
						}
					}
					if (inputTank.getFluidAmount() > 0) {
						double rate = FLUID_CONVERSION_RATES.getOrDefault(FluidRegistry.getFluidName(inputTank.getFluid()), 0D);
						FluidStack out = inputTank.drain(1000, true);
						if (out != null) {
							outputTank.fill(new FluidStack(Wings.JET_FUEL, (int)(out.amount*rate)), true);
							markDirty();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		if (hasWorld() && getWorld() instanceof WorldServer) {
			WorldServer ws = (WorldServer)getWorld();
			Chunk c = getWorld().getChunkFromBlockCoords(getPos());
			SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
			for (EntityPlayerMP player : getWorld().getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
				if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.x, c.z)) {
					player.connection.sendPacket(packet);
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
		operationProgress = 0;
		markDirty();
		return i;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException(Integer.toString(index));
		inputItem = stack;
		operationProgress = 0;
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
