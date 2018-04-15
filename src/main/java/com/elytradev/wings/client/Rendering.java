package com.elytradev.wings.client;

import java.nio.DoubleBuffer;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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
	
	public static void drawRect(float left, float top, float right, float bottom, int color) {
		if (left < right) {
			float swp = left;
			left = right;
			right = swp;
		}

		if (top < bottom) {
			float swp = top;
			top = bottom;
			bottom = swp;
		}

		float a = (color >> 24 & 0xFF) / 255f;
		float r = (color >> 16 & 0xFF) / 255f;
		float g = (color >> 8 & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder bb = tess.getBuffer();
		GlStateManager.color(r, g, b, a);
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		bb.pos(left , bottom, 0).endVertex();
		bb.pos(right, bottom, 0).endVertex();
		bb.pos(right, top   , 0).endVertex();
		bb.pos(left , top   , 0).endVertex();
		tess.draw();
	}
	
	public static void drawTexturedRect(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
		float uPx = 1f / textureWidth;
		float vPx = 1f / textureHeight;
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder bb = tess.getBuffer();
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x,         y + height, 0).tex((u * uPx)        , (v + height) * vPx).endVertex();
		bb.pos(x + width, y + height, 0).tex((u + width) * uPx, (v + height) * vPx).endVertex();
		bb.pos(x + width, y         , 0).tex((u + width) * uPx, (v * vPx)         ).endVertex();
		bb.pos(x        , y         , 0).tex((u * uPx)        , (v * vPx)         ).endVertex();
		tess.draw();
	}

}
