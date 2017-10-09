package com.elytradev.wings.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingFlightMode extends KeyBinding {

	public KeyBindingFlightMode(String description, IKeyConflictContext keyConflictContext, int keyCode, String category) {
		super(description, keyConflictContext, keyCode, category);
	}

	public KeyBindingFlightMode(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, String category) {
		super(description, keyConflictContext, keyModifier, keyCode, category);
	}

	public KeyBindingFlightMode(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}
	
	@Override
	public boolean conflicts(KeyBinding other) {
		// Just check our context, not the other
		if (getKeyConflictContext().conflicts(other.getKeyConflictContext())) {
			KeyModifier keyModifier = getKeyModifier();
			KeyModifier otherKeyModifier = other.getKeyModifier();
			if (keyModifier.matches(other.getKeyCode()) || otherKeyModifier.matches(getKeyCode())) {
				return true;
			} else if (getKeyCode() == other.getKeyCode()) {
				// Don't check all the strange ingame exceptions, we'll never use
				// that category (and ingame->flightmode conflicts can't exist)
				return keyModifier == otherKeyModifier;
			}
		}
		return false;
	}

	
}
