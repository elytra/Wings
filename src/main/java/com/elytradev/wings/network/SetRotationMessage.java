package com.elytradev.wings.network;

import javax.vecmath.Quat4f;

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
public class SetRotationMessage extends Message {

	@MarshalledAs("f32")
	private float rotationX;
	@MarshalledAs("f32")
	private float rotationY;
	@MarshalledAs("f32")
	private float rotationZ;
	@MarshalledAs("f32")
	private float rotationW;
	
	public SetRotationMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetRotationMessage(Quat4f rotation) {
		super(Wings.inst.network);
		this.rotationX = rotation.x;
		this.rotationY = rotation.y;
		this.rotationZ = rotation.z;
		this.rotationW = rotation.w;
	}
	
	@Override
	protected void handle(EntityPlayer _playerIn) {
		if (!(_playerIn instanceof EntityPlayerMP)) return;
		EntityPlayerMP ep = (EntityPlayerMP)_playerIn;
		
		boolean playerUntrusted = !ep.world.getMinecraftServer().isSinglePlayer() && !ep.world.getMinecraftServer().getServerOwner().equals(ep.getName());
		
		WingsPlayer wp = WingsPlayer.get(ep);
		if (!Float.isFinite(rotationX) ||
				!Float.isFinite(rotationY) ||
				!Float.isFinite(rotationZ) ||
				!Float.isFinite(rotationW)) {
			Wings.log.warn("{} sent invalid (non-finite) rotations", ep.getName());
			ep.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.wings.invalid_roll"));
			return;
		}
		wp.updatesThisTick++;
		if (wp.updatesThisTick > 5 && playerUntrusted) {
			Wings.log.warn("{} is sending updates too frequently ({} packets since last tick)", ep.getName(), wp.updatesThisTick);
			new PlayerWingsUpdateMessage(wp).sendTo(ep);
			return;
		}
		wp.rotation = new Quat4f(rotationX, rotationY, rotationZ, rotationW);
	}

}
