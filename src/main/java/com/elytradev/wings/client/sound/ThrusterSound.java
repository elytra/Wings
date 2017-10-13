package com.elytradev.wings.client.sound;

import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.WingsPlayer.FlightState;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThrusterSound extends MovingSound {
	private EntityPlayer player;

	public ThrusterSound(EntityPlayer player) {
		super(Wings.THRUST, SoundCategory.PLAYERS);
		this.player = player;
		this.repeat = true;
		this.volume = 0.1f;
	}

	@Override
	public void update() {
		WingsPlayer wp = WingsPlayer.get(player);
		
		if (!player.isDead && player.isElytraFlying()) {
			xPosF = (float) player.posX;
			yPosF = (float) player.posY;
			zPosF = (float) player.posZ;
			
			if (wp.afterburner && wp.flightState == FlightState.FLYING_ADVANCED) {
				volume = 0;
			} else if (wp.thruster >= 0.01) {
				volume = wp.thruster;
			} else {
				volume = 0;
			}

			if (volume > 0.8f) {
				pitch = 1 + (volume - 0.8f);
			} else {
				pitch = 1;
			}
		} else {
			donePlaying = true;
		}
	}
}