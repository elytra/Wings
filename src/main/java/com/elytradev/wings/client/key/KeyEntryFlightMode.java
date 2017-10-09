package com.elytradev.wings.client.key;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;

public class KeyEntryFlightMode extends KeyEntry {

	private static final Accessor<GuiButton> btnReset = Accessors.findField(KeyEntry.class, "field_148281_e", "btnReset");
	private static final Accessor<GuiButton> btnChangeKeyBinding = Accessors.findField(KeyEntry.class, "field_148280_d", "btnChangeKeyBinding");
	private static final Accessor<String> keyDesc = Accessors.findField(KeyEntry.class, "field_148283_c", "keyDesc");
	
	private static final Accessor<GuiControls> controlsScreen = Accessors.findField(GuiKeyBindingList.class, "field_148191_k", "controlsScreen");
	private static final Accessor<Integer> maxListLabelWidth = Accessors.findField(GuiKeyBindingList.class, "field_148188_n", "maxListLabelWidth");
	
	private final GuiKeyBindingList li;
	private final KeyBindingFlightMode keybinding;
	
	public KeyEntryFlightMode(GuiKeyBindingList li, KeyBindingFlightMode keybinding) {
		li.super(keybinding);
		this.li = li;
		this.keybinding = keybinding;
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		
		boolean rebinding = controlsScreen.get(li).buttonId == this.keybinding;
		mc.fontRenderer.drawString(keyDesc.get(this), x + 90 - maxListLabelWidth.get(li), y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, -1);
		
		btnReset.get(this).x = x + 210;
		btnReset.get(this).y = y;
		btnReset.get(this).enabled = !this.keybinding.isSetToDefaultValue();
		btnReset.get(this).drawButton(mc, mouseX, mouseY, partialTicks);
		
		btnChangeKeyBinding.get(this).x = x + 105;
		btnChangeKeyBinding.get(this).y = y;
		btnChangeKeyBinding.get(this).displayString = this.keybinding.getDisplayName();
		
		boolean conflict = false;
		boolean modifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
		if (this.keybinding.getKeyCode() != 0) {
			for (KeyBinding kb : mc.gameSettings.keyBindings) {
				// Here's the change: keybinding.conflicts instead of kb.conflicts
				if (kb != this.keybinding && keybinding.conflicts(kb)) {
					conflict = true;
					modifierConflict &= kb.hasKeyCodeModifierConflict(this.keybinding);
				}
			}
		}

		if (rebinding) {
			btnChangeKeyBinding.get(this).displayString = TextFormatting.WHITE + "> " + TextFormatting.YELLOW + btnChangeKeyBinding.get(this).displayString + TextFormatting.WHITE + " <";
		} else if (conflict) {
			btnChangeKeyBinding.get(this).displayString = (modifierConflict ? TextFormatting.GOLD : TextFormatting.RED) + btnChangeKeyBinding.get(this).displayString;
		}

		btnChangeKeyBinding.get(this).drawButton(mc, mouseX, mouseY, partialTicks);
	}

}
