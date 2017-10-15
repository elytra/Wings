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
public class SetRotationAndSpeedMessage extends Message {

	@MarshalledAs("f64")
	private double rotationX;
	@MarshalledAs("f64")
	private double rotationY;
	@MarshalledAs("f64")
	private double rotationZ;
	@MarshalledAs("f64")
	private double rotationW;
	
	@MarshalledAs("f64")
	private double motionX;
	@MarshalledAs("f64")
	private double motionY;
	@MarshalledAs("f64")
	private double motionZ;
	
	@MarshalledAs("f32")
	private float motionYaw;
	@MarshalledAs("f32")
	private float motionPitch;
	@MarshalledAs("f32")
	private float motionRoll;
	
	public SetRotationAndSpeedMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetRotationAndSpeedMessage(WingsPlayer wp) {
		super(Wings.inst.network);
		if (wp.rotation != null) {
			this.rotationX = wp.rotation.x;
			this.rotationY = wp.rotation.y;
			this.rotationZ = wp.rotation.z;
			this.rotationW = wp.rotation.w;
		}
		this.motionX = wp.player.motionX;
		this.motionY = wp.player.motionY;
		this.motionZ = wp.player.motionZ;
		this.motionYaw = wp.motionYaw;
		this.motionPitch = wp.motionPitch;
		this.motionRoll = wp.motionRoll;
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
