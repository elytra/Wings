package com.elytradev.wings.client.sound;

import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Unrealistic. Used on the client-side instead of a normal sound for cool factor.
 */
@SideOnly(Side.CLIENT)
public class SelfSonicBoomStartSound extends MovingSound {
	private EntityPlayer player;
	private int ticks;

	public SelfSonicBoomStartSound(EntityPlayer player) {
		super(Wings.SONIC_BOOM_SELF_START, SoundCategory.PLAYERS);
		this.player = player;
		this.attenuationType = AttenuationType.NONE;
	}

	@Override
	public void update() {
		WingsPlayer wp = WingsPlayer.get(player);
		ticks++;
		
		if (!player.isDead && player.isElytraFlying() && wp.sonicBoom) {
			xPosF = (float) player.posX;
			yPosF = (float) player.posY;
			zPosF = (float) player.posZ;
			if (ticks >= 57) {
				donePlaying = true;
				Minecraft.getMinecraft().getSoundHandler().playDelayedSound(new SelfSonicBoomSound(player), 0);
			}
		} else {
			donePlaying = true;
		}
	}
}