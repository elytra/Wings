package com.elytradev.wings;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3f;

import static net.minecraft.util.math.MathHelper.*;
import static java.lang.Math.PI;
import static java.lang.Math.asin;

public class WMath {

	public static final double RAD2DEG = (180 / PI);
	public static final double DEG2RAD = (PI / 180);
	
	public static Quat4d fromEuler(double yaw, double pitch, double roll) {
		Quat4d out = new Quat4d(0, 0, 0, 1);
		
		Quat4d rollQ = new Quat4d();
		Quat4d pitchQ = new Quat4d();
		Quat4d yawQ = new Quat4d();
		
		rollQ.set(new AxisAngle4d(0, 0, 1, roll));
		pitchQ.set(new AxisAngle4d(1, 0, 0, pitch));
		yawQ.set(new AxisAngle4d(0, 1, 0, yaw));
		
		out.mul(rollQ);
		out.mul(pitchQ);
		out.mul(yawQ);
		
		return out;
	}
	
	public static Quat4d fromEuler(Vector3f vec) {
		return fromEuler(vec.x, vec.y, vec.z);
	}
	
	
	
	public static int getGimbalPole(Quat4d quat) {
		double t = quat.y * quat.x + quat.z * quat.w;
		return t > 0.499 ? 1 : (t < -0.499 ? -1 : 0);
	}
	
	public static double getYaw(Quat4d quat) {
		return atan2(2f * quat.y * quat.w - 2 * quat.z * quat.x, 1 - 2 * (quat.y * quat.y) - 2 * (quat.x * quat.x));
	}
	
	public static double getPitch(Quat4d quat) {
		return asin(2 * quat.z * quat.y + 2 * quat.x * quat.w);
	}
	
	public static double getRoll(Quat4d quat) {
		return -atan2(2f * quat.z * quat.w - 2 * quat.y * quat.x, 1 - 2 * (quat.z * quat.z) - 2 * (quat.x * quat.x));
	}
	
	
	
	public static double rad2deg(double rad) {
		return rad * RAD2DEG;
	}
	
	public static double deg2rad(double deg) {
		return deg * DEG2RAD;
	}

}
