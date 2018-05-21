package com.elytradev.wings.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.elytradev.mini.MiniTransformer;
import com.elytradev.mini.PatchContext;
import com.elytradev.mini.annotation.Patch;

@Patch.Class("net.minecraft.client.renderer.entity.RenderPlayer")
public class RenderPlayerTransformer extends MiniTransformer {
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		byte[] sup = super.transform(name, transformedName, basicClass);
		if (sup != basicClass) {
			ClassReader reader = new ClassReader(sup);
			ClassNode clazz = new ClassNode();
			reader.accept(clazz, 0);
			
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			clazz.accept(writer);
			return writer.toByteArray();
		} else {
			return basicClass;
		}
	}
	
	@Patch.Method(
			srg="func_77043_a",
			mcp="applyRotations",
			descriptor="(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V"
		)
	public void patchApplyRotations(PatchContext ctx) {
		// if (!Hooks.applyRotations(this, ...)) return;
		ctx.jumpToStart();
		ctx.add(new VarInsnNode(ALOAD, 0));
		ctx.add(new VarInsnNode(ALOAD, 1));
		ctx.add(new VarInsnNode(FLOAD, 2));
		ctx.add(new VarInsnNode(FLOAD, 3));
		ctx.add(new VarInsnNode(FLOAD, 4));
		ctx.add(new MethodInsnNode(INVOKESTATIC, "com/elytradev/wings/asm/Hooks", "applyRotations", "(Lnet/minecraft/client/renderer/entity/RenderPlayer;Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)Z", false));
		LabelNode label = new LabelNode();
		ctx.add(new JumpInsnNode(IFNE, label));
		ctx.add(new InsnNode(RETURN));
		ctx.add(label);
	}

}
