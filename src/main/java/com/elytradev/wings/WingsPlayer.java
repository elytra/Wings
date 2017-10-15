package com.elytradev.wings;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;

import javax.vecmath.Quat4d;
import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.wings.item.ItemWings;
import com.elytradev.wings.network.PlayerWingsUpdateMessage;
import com.elytradev.wings.network.SonicBoomEffectMessage;
import com.google.common.base.Objects;
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
	public Quat4d rotation;
	
	public float motionRoll;
	public float motionYaw;
	public float motionPitch;
	
	public int updatesThisTick;
	public FlightState flightState;
	
	public float thruster;
	public boolean afterburner;
	public boolean brake;
	
	public boolean sonicBoom;
	
	public Quat4d prevRotation;
	public float lastTickThruster;
	public boolean lastTickAfterburner;
	public boolean lastTickBrake;
	public boolean lastTickSonicBoom;
	
	private WingsPlayer(EntityPlayer player) {
		this.player = player;
	}
	
	public void update() {
		if (player.isDead) {
			flightState = FlightState.NONE;
			return;
		}
		prevRotation = rotation == null ? null : (Quat4d)rotation.clone();
		lastTickThruster = thruster;
		lastTickAfterburner = afterburner;
		lastTickBrake = brake;
		lastTickSonicBoom = sonicBoom;
		updatesThisTick = 0;
		
		if (!player.world.isRemote || player.isUser()) {
			if (flightState != FlightState.FLYING_ADVANCED) {
				rotation = null;
				motionRoll = 0;
				motionYaw = 0;
				motionPitch = 0;
			} else if (rotation == null) {
				rotation = WMath.fromEuler(WMath.deg2rad(player.rotationYaw-180), WMath.deg2rad(player.rotationPitch), 0);
			}
			
			if (rotation != null) {
				rotation.mul(WMath.fromEuler(WMath.deg2rad(motionYaw), WMath.deg2rad(motionPitch), WMath.deg2rad(motionRoll)), rotation);
			}
			
			motionRoll *= 0.92f;
			motionYaw *= 0.92f;
			motionPitch *= 0.92f;
		}
		
		double speed = (player.motionX * player.motionX) + (player.motionY * player.motionY) + (player.motionZ * player.motionZ);
		
		if (flightState != FlightState.NONE) {
			player.fallDistance = 0;
			if (player.isCollidedHorizontally || player.isCollidedVertically) {
				int dmg = (int)(speed*10)-3;
				if (dmg > 0) {
					player.playSound(dmg > 3 ? SoundEvents.ENTITY_GENERIC_BIG_FALL : SoundEvents.ENTITY_GENERIC_SMALL_FALL, 1, 1);
					player.attackEntityFrom(DamageSource.FLY_INTO_WALL, dmg);
				}
			}
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
					double thrust = 0;
					if (afterburner && wings.burnFuel(chest, 14, player.world.isRemote)) {
						thrust = 0.1;
					} else if (thruster > 0 && wings.burnFuel(chest, (int)Math.ceil(3*thruster), player.world.isRemote)) {
						thrust = thruster*0.05;
					}
					
					if (thrust > 0) {
						Vec3d look = player.getLookVec().scale(thrust);
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
					
					if (player.onGround) {
						player.motionX *= 0.75;
						player.motionY *= 1.5;
						player.motionZ *= 0.75;
					}
				}
			} else {
				setFlag.invoke(player, 7, false);
			}
		}
		
		if (!player.isSneaking() && flightState == FlightState.FLYING_ADVANCED) {
			player.motionX = player.motionY = player.motionZ = 0;
			player.setLocationAndAngles((Math.floor(player.posX/5)+0.5)*5, (Math.floor(player.posY/5)+0.5)*5, (Math.floor(player.posZ/5)+0.5)*5, player.rotationYaw, player.rotationPitch);
			allowFlight();
		}

		if (sonicBoom) {
			if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() != Wings.GOGGLES) {
				player.attackEntityFrom(Wings.SUPERSONIC_NO_GOGGLES, 5f);
			}
		}
		
		if (speed > SOUND_BARRIER_SQ && !sonicBoom) {
			sonicBoom = true;
			new SonicBoomEffectMessage(player).sendToAllAround(player.world, player, 128);
		} else if (speed < END_BOOM_STATE_SQ) {
			sonicBoom = false;
		}
		
		if (!player.world.isRemote &&
				!Objects.equal(rotation, prevRotation)
				|| thruster != lastTickThruster
				|| afterburner != lastTickAfterburner
				|| brake != lastTickBrake) {
			new PlayerWingsUpdateMessage(this).sendToAllWatching(player);
		}
	}

	private void allowFlight() {
		if (player instanceof EntityPlayerMP) {
			floatingTickCount.set(((EntityPlayerMP)player).connection, 0);
		}
	}
	
	
}
