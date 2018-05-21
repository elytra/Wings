package com.elytradev.wings.asm;

import java.net.URLClassLoader;

import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

// Blatantly stolen from MalisisCore
// https://github.com/Ordinastie/MalisisCore/blob/400068758a9b514933b29e6a53b7cbc6bcf4084d/src/main/java/net/malisis/core/asm/CustomClassWriter.java
public class RemappingClassWriter extends ClassWriter {
	public static URLClassLoader customClassLoader = new URLClassLoader(((URLClassLoader) Launch.classLoader.getClass().getClassLoader()).getURLs());

	public RemappingClassWriter(int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		System.out.println(type1+", "+type2);
		type1 = FMLDeobfuscatingRemapper.INSTANCE.unmap(type1);
		type2 = FMLDeobfuscatingRemapper.INSTANCE.unmap(type2);
		
		System.out.println(type1+", "+type2);

		if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object")) {
			return "java/lang/Object";
		}

		Class<?> c, d;
		ClassLoader classLoader = customClassLoader;
		try {
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));

			String result = FMLDeobfuscatingRemapper.INSTANCE.map(c.getName().replace('.', '/'));

			return result;
		}
	}
}
