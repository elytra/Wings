package com.elytradev.wings.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.Wings;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class PlayerRollMessage extends Message {

	@MarshalledAs("i32")
	private int entityId;
	@MarshalledAs("f32")
	private float roll;
	
	public PlayerRollMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public PlayerRollMessage(EntityPlayer player, float roll) {
		super(Wings.inst.network);
		this.entityId = player.getEntityId();
		this.roll = roll;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		Entity e = player.world.getEntityByID(entityId);
		if (e instanceof EntityPlayer) {
			EntityPlayer subject = (EntityPlayer)e;
		}
	}

}
