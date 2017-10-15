package com.elytradev.wings.network;

import javax.vecmath.Quat4d;
import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ReceivedOn(Side.CLIENT)
public class PlayerWingsUpdateMessage extends Message {

	@MarshalledAs("i32")
	private int entityId;
	@MarshalledAs("f32")
	private float thruster;
	
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
	
	public PlayerWingsUpdateMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public PlayerWingsUpdateMessage(WingsPlayer wp) {
		super(Wings.inst.network);
		this.entityId = wp.player.getEntityId();
		if (wp.rotation != null) {
			this.rotationX = wp.rotation.x;
			this.rotationY = wp.rotation.y;
			this.rotationZ = wp.rotation.z;
			this.rotationW = wp.rotation.w;
		}
		if (wp.afterburner) {
			this.thruster = SetThrusterMessage.AFTERBURNER_SPEED;
		} else if (wp.brake) {
			this.thruster = SetThrusterMessage.BRAKE_SPEED;
		} else {
			this.thruster = wp.thruster;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityPlayer player) {
		Entity e = player.world.getEntityByID(entityId);
		if (e instanceof EntityPlayer) {
			EntityPlayer subject = (EntityPlayer)e;
			WingsPlayer wp = WingsPlayer.get(subject);
			if (rotationX == 0 && rotationY == 0 && rotationZ == 0 && rotationW == 0) {
				wp.rotation = null;
			} else {
				wp.rotation = new Quat4d(rotationX, rotationY, rotationZ, rotationW);
				
				wp.motionYaw = motionYaw;
				wp.motionPitch = motionPitch;
				wp.motionRoll = motionRoll;
				
				wp.player.motionX = motionX;
				wp.player.motionY = motionY;
				wp.player.motionZ = motionZ;
			}
			if (thruster == SetThrusterMessage.AFTERBURNER_SPEED) {
				wp.thruster = 0;
				wp.afterburner = true;
				wp.brake = false;
			} else if (thruster == SetThrusterMessage.BRAKE_SPEED) {
				wp.thruster = 0;
				wp.afterburner = false;
				wp.brake = true;
			} else {
				wp.thruster = thruster;
			}
		}
	}

}
