package com.elytradev.wings.client.sound;

import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AfterburnerStartSound extends MovingSound {
	private EntityPlayer player;
	private int ticks;

	public AfterburnerStartSound(EntityPlayer player) {
		super(Wings.AFTERBURNER_START, SoundCategory.PLAYERS);
		this.player = player;
	}

	@Override
	public void update() {
		WingsPlayer wp = WingsPlayer.get(player);
		ticks++;
		
		if (!player.isDead && player.isElytraFlying() && wp.afterburner) {
			xPosF = (float) player.posX;
			yPosF = (float) player.posY;
			zPosF = (float) player.posZ;
			if (ticks >= 32) {
				donePlaying = true;
				Minecraft.getMinecraft().getSoundHandler().playDelayedSound(new AfterburnerSound(player), 0);
			}
		} else {
			donePlaying = true;
		}
	}
}