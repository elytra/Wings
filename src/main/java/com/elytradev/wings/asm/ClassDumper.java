package com.elytradev.wings.asm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import com.elytradev.mini.MiniCoremod;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({"com.elytradev.mini", "com.elytradev.wings.asm"})
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class ClassDumper extends MiniCoremod {

	public static class Transformer implements IClassTransformer {

		@Override
		public byte[] transform(String name, String transformedName, byte[] basicClass) {
			File out = new File(new File("dumpedClasses"), transformedName.replace('.', File.separatorChar)+".class");
			out.getParentFile().mkdirs();
			try {
				com.google.common.io.Files.write(basicClass, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return basicClass;
		}

	}

	public ClassDumper() {
		super(ClassDumper.Transformer.class);
		try {
			Files.walkFileTree(new File("dumpedClasses").toPath(), new FileVisitor<Path>() {

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
