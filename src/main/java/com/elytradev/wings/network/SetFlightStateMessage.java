package com.elytradev.wings.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.WingsPlayer.FlightState;
import com.elytradev.wings.item.ItemWings;
import com.elytradev.wings.Wings;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.SERVER)
public class SetFlightStateMessage extends Message {

	private FlightState state;
	
	public SetFlightStateMessage(NetworkContext ctx) {
		super(ctx);
	}
	
	public SetFlightStateMessage(WingsPlayer.FlightState state) {
		super(Wings.inst.network);
		this.state = state;
	}
	
	
	@Override
	protected void handle(EntityPlayer player) {
		WingsPlayer epp = WingsPlayer.get(player);
		if (state == FlightState.NONE) {
			// setting none is always safe
			epp.flightState = FlightState.NONE;
			return;
		}
		ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chest.getItem() instanceof ItemWings) {
			ItemWings wings = (ItemWings)chest.getItem();
			if (wings.hasThruster()) {
				// thruster wings can do everything
				epp.flightState = state;
			} else {
				if (state == FlightState.FLYING || state == FlightState.FLYING_FLIGHT_MODE) {
					epp.flightState = state;
				} else {
					Wings.log.warn("{} attempted to set flight state to {} with wings that don't support that state", player.getName(), state);
				}
			}
		} else {
			Wings.log.warn("{} attempted to set flight state to {} while not wearing wings", player.getName(), state);
		}
	}

}
