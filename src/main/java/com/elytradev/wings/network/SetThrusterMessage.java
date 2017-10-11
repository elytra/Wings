package com.elytradev.wings.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SetThrusterMessage extends Message {

	public static final float AFTERBURNER_SPEED = Float.POSITIVE_INFINITY;
	public static final float BRAKE_SPEED = Float.NEGATIVE_INFINITY;
	
	@MarshalledAs("f32")
	private float speed;
	
	public SetThrusterMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetThrusterMessage(float speed) {
		super(Wings.inst.network);
		this.speed = speed;
	}
	
	@Override
	protected void handle(EntityPlayer player) {
		WingsPlayer wp = WingsPlayer.get(player);
		if (speed == AFTERBURNER_SPEED) {
			wp.afterburner = true;
			wp.brake = false;
			wp.thruster = 0;
		} else if (speed == BRAKE_SPEED) {
			wp.afterburner = false;
			wp.brake = true;
			wp.thruster = 0;
		} else if (Float.isNaN(speed)) {
			Wings.log.warn("{} attempted to set thruster speed to NaN", player.getName());
		} else if (speed > 1 || speed < 0) {
			Wings.log.warn("{} attempted to set thruster speed to {}%", player.getName(), (int)(speed*100));
		} else {
			wp.afterburner = false;
			wp.brake = false;
			wp.thruster = speed;
		}
	}

}
