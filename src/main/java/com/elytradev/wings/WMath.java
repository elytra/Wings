package com.elytradev.wings;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.minecraft.util.math.MathHelper;

import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.*;

public class WMath {

	public static final float PIf = (float)PI;
	public static final float RAD2DEG = (float)(180 / PI);
	public static final float DEG2RAD = (float)(PI / 180);
	
	public static Quat4f fromEuler(float yaw, float pitch, float roll) {
		Quat4f out = new Quat4f(0, 0, 0, 1);
		
		Quat4f rollQ = new Quat4f();
		Quat4f pitchQ = new Quat4f();
		Quat4f yawQ = new Quat4f();
		
		rollQ.set(new AxisAngle4f(0, 0, 1, roll));
		pitchQ.set(new AxisAngle4f(1, 0, 0, pitch));
		yawQ.set(new AxisAngle4f(0, 1, 0, yaw));
		
		out.mul(rollQ);
		out.mul(pitchQ);
		out.mul(yawQ);
		
		return out;
	}
	
	public static Quat4f fromEuler(Vector3f vec) {
		return fromEuler(vec.x, vec.y, vec.z);
	}
	
	
	
	public static int getGimbalPole(Quat4f quat) {
		float t = quat.y * quat.x + quat.z * quat.w;
		return t > 0.499f ? 1 : (t < -0.499f ? -1 : 0);
	}
	
	public static float getYaw(Quat4f quat) {
		return getGimbalPole(quat) == 0 ? atan2(2 * (quat.y * quat.w + quat.x * quat.z), 1f - 2f * (quat.y * quat.y + quat.x * quat.x)) : 0;
	}
	
	public static float getPitch(Quat4f quat) {
		return getGimbalPole(quat) == 0 ? asin(clamp(2f * (quat.w * quat.x - quat.z * quat.y), -1f, 1f)) : getGimbalPole(quat) * PIf * 0.5f;
	}
	
	public static float getRoll(Quat4f quat) {
		return getGimbalPole(quat) == 0 ? atan2(2f * (quat.w * quat.z + quat.y * quat.x), 1f - 2f * (quat.x * quat.x + quat.z * quat.z)) : getGimbalPole(quat) * 2f * atan2(quat.y, quat.w);
	}
	
	
	
	public static float atan2(float y, float x) {
		return (float)MathHelper.atan2(y, x);
	}
	
	public static float asin(float a) {
		return (float)Math.asin(a);
	}
	
	
	
	public static float rad2deg(float rad) {
		return rad * RAD2DEG;
	}
	
	public static float deg2rad(float deg) {
		return deg * DEG2RAD;
	}

}
