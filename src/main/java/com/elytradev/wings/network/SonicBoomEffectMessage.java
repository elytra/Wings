package com.elytradev.wings.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.Wings;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class SonicBoomEffectMessage extends Message {

	@MarshalledAs("i32")
	private int entityId;
	
	public SonicBoomEffectMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SonicBoomEffectMessage(Entity e) {
		super(Wings.inst.network);
		this.entityId = e.getEntityId();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		Entity e = player.world.getEntityByID(entityId);
		if (e != null) {
			boolean close = player.getDistanceSq(e) < 1024;
			Minecraft.getMinecraft().player.playSound(Wings.SONIC_BOOM, close ? 1f : 0.5f, 1f);
		}
	}

}
