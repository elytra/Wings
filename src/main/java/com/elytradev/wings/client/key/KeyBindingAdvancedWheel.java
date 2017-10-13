package com.elytradev.wings.client.key;

import org.lwjgl.input.Keyboard;

import net.minecraftforge.client.settings.IKeyConflictContext;

public class KeyBindingAdvancedWheel extends KeyBindingAdvanced {

	private boolean direction;
	
	public KeyBindingAdvancedWheel(String description, IKeyConflictContext keyConflictContext, int keyCode, String category, boolean direction) {
		super(description, keyConflictContext, keyCode, category);
		this.direction = direction;
	}
	
	@Override
	public String getDisplayName() {
		if (getKeyCode() == Keyboard.KEY_NONE) {
			return "Wheel "+(direction ? "Up" : "Down");
		}
		return super.getDisplayName();
	}
	
}
