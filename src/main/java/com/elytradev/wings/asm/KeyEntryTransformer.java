package com.elytradev.wings.asm;

import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.elytradev.mini.MiniTransformer;
import com.elytradev.mini.PatchContext;
import com.elytradev.mini.annotation.Patch;


@Patch.Class("net.minecraft.client.gui.GuiKeyBindingList$KeyEntry")
public class KeyEntryTransformer extends MiniTransformer {

	@Patch.Method(
			srg="func_192634_a",
			mcp="drawEntry",
			descriptor="(IIIIIIIZF)V"
		)
	public void patchDrawEntry(PatchContext ctx) {
		ctx.jumpToStart();
		// flag1 = true;
		ctx.search(new InsnNode(ICONST_1), new VarInsnNode(ISTORE, 11)).jumpBefore();
		// Hooks.drawEntryPreConflict(this, keybinding, ..., flag1);
		ctx.add(new VarInsnNode(ALOAD, 0));
		ctx.add(new VarInsnNode(ALOAD, 16));
		ctx.add(new VarInsnNode(ILOAD, 1));
		ctx.add(new VarInsnNode(ILOAD, 2));
		ctx.add(new VarInsnNode(ILOAD, 3));
		ctx.add(new VarInsnNode(ILOAD, 4));
		ctx.add(new VarInsnNode(ILOAD, 5));
		ctx.add(new VarInsnNode(ILOAD, 6));
		ctx.add(new VarInsnNode(ILOAD, 7));
		ctx.add(new VarInsnNode(ILOAD, 8));
		ctx.add(new VarInsnNode(FLOAD, 9));
		ctx.add(new VarInsnNode(ILOAD, 11));
		ctx.add(new MethodInsnNode(INVOKESTATIC, "com/elytradev/wings/asm/Hooks", "drawEntryPreConflict", "(Lnet/minecraft/client/gui/GuiKeyBindingList$KeyEntry;Lnet/minecraft/client/settings/KeyBinding;IIIIIIIZFZ)V", false));
		
		ctx.jumpToStart();
		// flag1 = true;
		ctx.search(new InsnNode(ICONST_1), new VarInsnNode(ISTORE, 11)).jumpAfter();
		// flag1 = Hooks.drawEntryPostConflict(this, keybinding, ...);
		ctx.add(new VarInsnNode(ALOAD, 0));
		ctx.add(new VarInsnNode(ALOAD, 16));
		ctx.add(new VarInsnNode(ILOAD, 1));
		ctx.add(new VarInsnNode(ILOAD, 2));
		ctx.add(new VarInsnNode(ILOAD, 3));
		ctx.add(new VarInsnNode(ILOAD, 4));
		ctx.add(new VarInsnNode(ILOAD, 5));
		ctx.add(new VarInsnNode(ILOAD, 6));
		ctx.add(new VarInsnNode(ILOAD, 7));
		ctx.add(new VarInsnNode(ILOAD, 8));
		ctx.add(new VarInsnNode(FLOAD, 9));
		ctx.add(new MethodInsnNode(INVOKESTATIC, "com/elytradev/wings/asm/Hooks", "drawEntryPostConflict", "(Lnet/minecraft/client/gui/GuiKeyBindingList$KeyEntry;Lnet/minecraft/client/settings/KeyBinding;IIIIIIIZF)Z", false));
		ctx.add(new VarInsnNode(ISTORE, 11));
	}
	
}
