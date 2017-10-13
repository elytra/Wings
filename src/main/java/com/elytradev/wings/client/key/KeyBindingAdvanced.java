package com.elytradev.wings.client.key;

import com.elytradev.wings.client.ClientProxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingAdvanced extends KeyBinding {

	public KeyBindingAdvanced(String description, IKeyConflictContext keyConflictContext, int keyCode, String category) {
		super(description, keyConflictContext, keyCode, category);
		ClientProxy.advancedFlightKeybinds.add(this);
	}

	public KeyBindingAdvanced(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, String category) {
		super(description, keyConflictContext, keyModifier, keyCode, category);
		ClientProxy.advancedFlightKeybinds.add(this);
	}

	public KeyBindingAdvanced(String description, int keyCode, String category) {
		super(description, keyCode, category);
		ClientProxy.advancedFlightKeybinds.add(this);
	}
	
	@Override
	public boolean conflicts(KeyBinding other) {
		if (other instanceof KeyBindingAdvanced) {
			if (getKeyConflictContext().conflicts(other.getKeyConflictContext())) {
				KeyModifier keyModifier = getKeyModifier();
				KeyModifier otherKeyModifier = other.getKeyModifier();
				if (keyModifier.matches(other.getKeyCode()) || otherKeyModifier.matches(getKeyCode())) {
					return true;
				} else if (getKeyCode() == other.getKeyCode()) {
					return keyModifier == otherKeyModifier;
				}
			}
			return false;
		} else if (ClientProxy.advancedFlightKeybinds.contains(other)) {
			return other.conflicts(this);
		} else {
			return false;
		}
	}

	
}
