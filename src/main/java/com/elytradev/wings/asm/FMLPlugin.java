package com.elytradev.wings.asm;

import com.elytradev.mini.MiniCoremod;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({"com.elytradev.mini", "com.elytradev.wings.asm"})
@IFMLLoadingPlugin.SortingIndex(1001)
public class FMLPlugin extends MiniCoremod {

	public FMLPlugin() {
		super(
				RenderPlayerTransformer.class,
				KeyEntryTransformer.class
			);
	}

}