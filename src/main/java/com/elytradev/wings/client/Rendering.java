package com.elytradev.wings.client;

import java.nio.DoubleBuffer;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Rendering {

	private static final Vector3d VEC3_ZERO = new Vector3d();
	private static final DoubleBuffer matrixBuffer = BufferUtils.createDoubleBuffer(16);
	
	public static void rotate(Quat4d prevRotation, Quat4d rotation, double partialTicks) {
		Quat4d prev = prevRotation == null ? rotation : prevRotation;
		Quat4d cur = rotation == null ? new Quat4d(0, 0, 0, 1) : rotation;
		Quat4d lerp = new Quat4d();
		lerp.interpolate(prev, cur, partialTicks);
		
		Matrix4d mat = new Matrix4d(lerp, VEC3_ZERO, 1);
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				matrixBuffer.put(mat.getElement(y, x));
			}
		}
		matrixBuffer.flip();
		GL11.glMultMatrix(matrixBuffer);
	}

}
