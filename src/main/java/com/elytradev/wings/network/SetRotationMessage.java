package com.elytradev.wings.network;

import javax.vecmath.Quat4d;
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

	@MarshalledAs("f64")
	private double rotationX;
	@MarshalledAs("f64")
	private double rotationY;
	@MarshalledAs("f64")
	private double rotationZ;
	@MarshalledAs("f64")
	private double rotationW;
	
	public SetRotationMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetRotationMessage(Quat4d rotation) {
		super(Wings.inst.network);
		if (rotation != null) {
			this.rotationX = rotation.x;
			this.rotationY = rotation.y;
			this.rotationZ = rotation.z;
			this.rotationW = rotation.w;
		}
	}
	
	@Override
	protected void handle(EntityPlayer _playerIn) {
		if (!(_playerIn instanceof EntityPlayerMP)) return;
		EntityPlayerMP ep = (EntityPlayerMP)_playerIn;
		
		WingsPlayer wp = WingsPlayer.get(ep);
		if (!Double.isFinite(rotationX) ||
				!Double.isFinite(rotationY) ||
				!Double.isFinite(rotationZ) ||
				!Double.isFinite(rotationW)) {
			Wings.log.warn("{} sent invalid (non-finite) rotations", ep.getName());
			ep.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.wings.invalid_roll"));
			return;
		}
		wp.updatesThisTick++;
		if (wp.updatesThisTick > 5) {
			Wings.log.warn("{} is sending updates too frequently ({} packets since last tick)", ep.getName(), wp.updatesThisTick);
			new PlayerWingsUpdateMessage(wp).sendTo(ep);
			return;
		}
		if (rotationX == 0 && rotationY == 0 && rotationZ == 0 && rotationW == 0) {
			wp.rotation = null;
			if (wp.prevRotation != null) {
				new PlayerWingsUpdateMessage(wp).sendToAllWatching(wp.player);
			}
		} else {
			wp.rotation = new Quat4d(rotationX, rotationY, rotationZ, rotationW);
			if (wp.prevRotation == null || !wp.rotation.epsilonEquals(wp.prevRotation, 0.001)) {
				new PlayerWingsUpdateMessage(wp).sendToAllWatching(wp.player);
			}
		}
	}

}
