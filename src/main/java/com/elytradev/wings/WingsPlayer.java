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

	// this isn't 100% accurate, but we want to make it attainable
	// we're pretending it's 12 m/t (240 m/s, 864 km/h) but it's really 17.15 m/t (343 m/s, 1234.8 km/h)
	
	public static final double SOUND_BARRIER = 12;
	public static final double SOUND_BARRIER_SQ = SOUND_BARRIER*SOUND_BARRIER;
	
	public static final double END_BOOM_STATE = 11;
	public static final double END_BOOM_STATE_SQ = END_BOOM_STATE*END_BOOM_STATE;
	
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
	
	public int rollUpdatesThisTick;
	public FlightState flightState;
	
	public float thruster;
	public boolean afterburner;
	public boolean brake;
	
	public boolean sonicBoom;
	
	public float lastTickRotationRoll;
	public float lastTickThruster;
	public boolean lastTickAfterburner;
	public boolean lastTickBrake;
	public boolean lastTickSonicBoom;
	
	private WingsPlayer(EntityPlayer player) {
		this.player = player;
	}
	
	public void update() {
		if (player.isDead) return;
		lastTickRotationRoll = rotationRoll;
		lastTickThruster = thruster;
		lastTickAfterburner = afterburner;
		lastTickBrake = brake;
		lastTickSonicBoom = sonicBoom;
		rollUpdatesThisTick = 0;
		
		if (flightState != FlightState.FLYING_ADVANCED) {
			rotationRoll = 0;
		}
		
		ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chest.getItem() instanceof ItemWings) {
			ItemWings wings = (ItemWings)chest.getItem();
			
			if (wings.isFuelDepleted(chest)) {
				thruster = 0;
				afterburner = false;
			}
			
			if (flightState == FlightState.FLYING) {
				if (player.onGround) {
					flightState = FlightState.NONE;
				}
				setFlag.invoke(player, 7, true);
				player.fallDistance = 0;
				if (wings.hasThruster()) {
					if (afterburner && wings.burnFuel(chest, 1, player.world.isRemote)) {
						Vec3d look = player.getLookVec().scale(0.03);
						player.motionX += look.x;
						player.motionY += look.y;
						player.motionZ += look.z;
					}
					if (brake) {
						player.motionX *= 0.98;
						if (player.motionY > 0) {
							player.motionY *= 0.98;
						}
						player.motionZ *= 0.98;
					}
				}
			} else if (flightState == FlightState.FLYING_ADVANCED) {
				setFlag.invoke(player, 7, true);
				if (wings.hasThruster()) {
					double speed = 0;
					if (afterburner && wings.burnFuel(chest, 14, player.world.isRemote)) {
						speed = 0.1;
					} else if (thruster > 0 && wings.burnFuel(chest, (int)Math.ceil(3*thruster), player.world.isRemote)) {
						speed = thruster*0.05;
					}
					
					if (speed > 0) {
						Vec3d look = player.getLookVec().scale(speed);
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
					
					if (player.onGround) {
						player.motionX *= 0.75;
						player.motionY *= 1.5;
						player.motionZ *= 0.75;
					}
				}
				player.fallDistance = 0;
			} else {
				setFlag.invoke(player, 7, false);
			}
		}
		
		double speed = (player.motionX * player.motionX) + (player.motionY * player.motionY) + (player.motionZ * player.motionZ);
		if (speed > SOUND_BARRIER_SQ && !sonicBoom) {
			sonicBoom = true;
			player.playSound(Wings.SONIC_BOOM, 1f, 1f);
		} else if (speed < END_BOOM_STATE_SQ) {
			sonicBoom = false;
		}
		
		if (rotationRoll != lastTickRotationRoll && !player.world.isRemote) {
			new PlayerRollMessage(player, rotationRoll).sendToAllWatching(player);
		}
	}

	private void allowFlight() {
		if (player instanceof EntityPlayerMP) {
			floatingTickCount.set(((EntityPlayerMP)player).connection, 0);
		}
	}
	
	
}
