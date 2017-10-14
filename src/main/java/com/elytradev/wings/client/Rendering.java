package com.elytradev.wings.client;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import net.minecraft.client.renderer.GlStateManager;

public class Rendering {

	private static final Vector3f VEC3_ZERO = new Vector3f();
	private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	public static void rotate(Quat4f prevRotation, Quat4f rotation, float partialTicks) {
		Quat4f prev = prevRotation == null ? rotation : prevRotation;
		Quat4f cur = rotation == null ? new Quat4f(0, 0, 0, 1) : rotation;
		Quat4f lerp = new Quat4f();
		lerp.interpolate(prev, cur, partialTicks);
		
		Matrix4f mat = new Matrix4f(lerp, VEC3_ZERO, 1);
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				matrixBuffer.put(mat.getElement(y, x));
			}
		}
		matrixBuffer.flip();
		GlStateManager.multMatrix(matrixBuffer);
	}

}
