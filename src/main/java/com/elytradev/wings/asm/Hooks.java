package com.elytradev.wings.asm;

import java.util.Optional;

import javax.vecmath.Quat4d;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.client.Rendering;
import com.elytradev.wings.client.key.KeyBindingAdvanced;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;

public class Hooks {

	public static boolean applyRotations(RenderPlayer render, AbstractClientPlayer player, float p_77043_2_, float rotationYaw, float partialTicks) {
		Optional<WingsPlayer> opt = WingsPlayer.getIfExists(player);
		if (opt.isPresent()) {
			WingsPlayer wp = opt.get();
			if (wp.rotation != null) {
				Quat4d prev = wp.prevRotation == null ? new Quat4d(0, 0, 0, 1) : (Quat4d)wp.prevRotation.clone();
				Quat4d cur = (Quat4d)wp.rotation.clone();
				prev.conjugate();
				cur.conjugate();
				Rendering.rotate(prev, cur, partialTicks);
				
				GlStateManager.rotate(270, 1, 0, 0);
				GlStateManager.translate(0, -0.8f, 0);
				return false;
			}
		}
		return true;
	}
	
	private static final Accessor<KeyBinding> keybinding = Accessors.findField(KeyEntry.class, "field_148282_b", "keybinding");
	
	private static boolean lastConflictValue = false;
	
	public static void drawEntryPreConflict(KeyEntry entry, KeyBinding subject, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, boolean currentFlag) {
		lastConflictValue = currentFlag;
	}
	
	public static boolean drawEntryPostConflict(KeyEntry entry, KeyBinding subject, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		KeyBinding kb = keybinding.get(entry);
		if (kb instanceof KeyBindingAdvanced) {
			return kb.conflicts(subject) ? true : lastConflictValue;
		}
		return true;
	}
	
}
