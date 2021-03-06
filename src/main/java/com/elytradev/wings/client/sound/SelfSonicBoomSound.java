package com.elytradev.wings.client.sound;

import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Unrealistic. Used on the client-side instead of a normal sound for cool factor.
 */
@SideOnly(Side.CLIENT)
public class SelfSonicBoomSound extends MovingSound {
	private EntityPlayer player;

	public SelfSonicBoomSound(EntityPlayer player) {
		super(Wings.SONIC_BOOM_SELF, SoundCategory.PLAYERS);
		this.player = player;
		this.attenuationType = AttenuationType.NONE;
		this.repeat = true;
	}

	@Override
	public void update() {
		WingsPlayer wp = WingsPlayer.get(player);
		
		if (!player.isDead && player.isElytraFlying() && wp.sonicBoom) {
			xPosF = (float) player.posX;
			yPosF = (float) player.posY;
			zPosF = (float) player.posZ;
		} else {
			donePlaying = true;
		}
	}
}