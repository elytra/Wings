package com.elytradev.wings.client.sound;

import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.WingsPlayer.FlightState;

import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.entity.EntityPlayerSP;

public class QuietElytraSound extends ElytraSound {

	private final EntityPlayerSP player;
	
	public QuietElytraSound(EntityPlayerSP player) {
		super(player);
		this.player = player;
	}
	
	@Override
	public void update() {
		super.update();
		WingsPlayer wp = WingsPlayer.get(player);
		if (wp.flightState == FlightState.FLYING_ADVANCED) {
			this.volume /= 2;
		}
	}

}
