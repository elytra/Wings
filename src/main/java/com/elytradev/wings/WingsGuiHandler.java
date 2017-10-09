package com.elytradev.wings;

import com.elytradev.wings.client.gui.GuiConverter;
import com.elytradev.wings.inventory.ContainerConverter;
import com.elytradev.wings.tile.TileEntityConverter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class WingsGuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof TileEntityConverter) {
				return new ContainerConverter(player.inventory, (TileEntityConverter)te);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 0) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof TileEntityConverter) {
				return new GuiConverter(new ContainerConverter(player.inventory, (TileEntityConverter)te));
			}
		}
		return null;
	}

}
