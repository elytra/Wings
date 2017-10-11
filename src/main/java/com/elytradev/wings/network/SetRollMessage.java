package com.elytradev.wings.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.Wings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SetRollMessage extends Message {

	@MarshalledAs("f32")
	private float roll;
	
	public SetRollMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetRollMessage(float roll) {
		super(Wings.inst.network);
		this.roll = roll;
	}
	
	@Override
	protected void handle(EntityPlayer _playerIn) {
		if (!(_playerIn instanceof EntityPlayerMP)) return;
		EntityPlayerMP ep = (EntityPlayerMP)_playerIn;
		
		boolean playerUntrusted = !ep.world.getMinecraftServer().isSinglePlayer() && !ep.world.getMinecraftServer().getServerOwner().equals(ep.getName());
		
		WingsPlayer epp = WingsPlayer.get(ep);
		if (!Float.isFinite(roll)) {
			Wings.log.warn("{} rolled wrongly", ep.getName());
			ep.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.wings.invalid_roll"));
			return;
		}
		epp.rollUpdatesThisTick++;
		if (epp.rollUpdatesThisTick > 5 && playerUntrusted) {
			Wings.log.warn("{} is sending roll updates too frequently ({} packets since last tick)", ep.getName(), epp.rollUpdatesThisTick);
			new PlayerRollMessage(ep, epp.rotationRoll).sendTo(ep);
			return;
		}
		float diff = Math.abs(epp.rotationRoll - roll);
		if (diff > 15 && playerUntrusted) {
			Wings.log.warn("{} rolled too quickly! ({})", ep.getName(), roll);
			return;
		}
	}

}
