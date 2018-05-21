package com.elytradev.wings.client.key;

import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindingAdvancedWheel extends KeyBindingAdvanced {

	public static final int WHEEL_UP =   -1000;
	public static final int WHEEL_DOWN = -1001;
	
	public KeyBindingAdvancedWheel(String description, IKeyConflictContext keyConflictContext, int keyCode, String category) {
		super(description, keyConflictContext, keyCode, category);
	}
	
	@Override
	public String getDisplayName() {
		String prefix = "";
		if (getKeyModifier() != KeyModifier.NONE) {
			prefix = getKeyModifier()+" + ";
		}
		if (getKeyCode() == WHEEL_UP) {
			return prefix+"Wheel Up";
		}
		if (getKeyCode() == WHEEL_DOWN) {
			return prefix+"Wheel Down";
		}
		return super.getDisplayName();
	}

	public boolean doesMatchWheel(int dwheel) {
		if (getKeyCode() == WHEEL_UP) {
			return dwheel > 0 && getKeyModifier().isActive(getKeyConflictContext());
		}
		if (getKeyCode() == WHEEL_DOWN) {
			return dwheel < 0 && getKeyModifier().isActive(getKeyConflictContext());
		}
		return false;
	}
	
}
