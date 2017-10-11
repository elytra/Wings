package com.elytradev.wings;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.wings.item.ItemWings;
import com.elytradev.wings.network.PlayerRollMessage;
import com.google.common.collect.MapMaker;

public final class WingsPlayer {

	public enum FlightState {
		NONE,
		
		FLYING,
		FLYING_ADVANCED
	}

	// weakKeys implies identity comparison
	private static final Map<EntityPlayer, WingsPlayer> map = new MapMaker().weakKeys().concurrencyLevel(1).makeMap();
	
	private static final Accessor<Integer> floatingTickCount = Accessors.findField(NetHandlerPlayServer.class, "field_147365_f", "floatingTickCount");
	private static final Invoker setFlag = Invokers.findMethod(Entity.class, "setFlag", "func_70052_a", int.class, boolean.class);
	
	public static WingsPlayer get(EntityPlayer entity) {
		return map.computeIfAbsent(entity, WingsPlayer::new);
	}
	
	public static Optional<WingsPlayer> getIfExists(EntityPlayer entity) {
		return map.containsKey(entity) ? Optional.of(map.get(entity)) : Optional.empty();
	}
	
	public final EntityPlayer player;
	public float rotationRoll;
	
	public float lastTickRotationRoll;
	
	public int rollUpdatesThisTick;
	public FlightState flightState;
	
	public float thruster;
	public boolean afterburner;
	public boolean brake;
	
	private WingsPlayer(EntityPlayer player) {
		this.player = player;
	}
	
	public void update() {
		if (player.isDead) return;
		rollUpdatesThisTick = 0;
		
		if (flightState != FlightState.FLYING_ADVANCED) {
			rotationRoll = 0;
		}
		
		ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chest.getItem() instanceof ItemWings) {
			ItemWings wings = (ItemWings)chest.getItem();
			if (player.onGround) {
				flightState = FlightState.NONE;
			}
			
			if (flightState == FlightState.FLYING) {
				setFlag.invoke(player, 7, true);
				player.fallDistance = 0;
				if (wings.hasThruster()) {
					if (afterburner && wings.burnFuel(chest, 5, player.world.isRemote)) {
						Vec3d look = player.getLookVec().scale(0.03);
						player.motionX += look.x;
						player.motionY += look.y;
						player.motionZ += look.z;
					}
					if (brake && wings.burnFuel(chest, 1, !player.world.isRemote)) {
						player.motionX *= 0.98;
						if (player.motionY > 0) {
							player.motionY *= 0.98;
						}
						player.motionZ *= 0.98;
					}
				}
			} else {
				setFlag.invoke(player, 7, false);
			}
		}
		
		if (rotationRoll != lastTickRotationRoll && !player.world.isRemote) {
			new PlayerRollMessage(player, rotationRoll).sendToAllWatching(player);
		}
		lastTickRotationRoll = rotationRoll;
	}

	private void allowFlight() {
		if (player instanceof EntityPlayerMP) {
			floatingTickCount.set(((EntityPlayerMP)player).connection, 0);
		}
	}
	
	
}
