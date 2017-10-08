package com.elytradev.wings;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid=Wings.MODID, name=Wings.NAME, version=Wings.VERSION)
public class Wings {

	public static final String MODID = "wings";
	public static final String NAME = "Wings";
	public static final String VERSION = "@VERSION@";
	
	public static ItemLeatherElytra LEATHER_ELYTRA;
	public static ItemMetalElytra METAL_ELYTRA;
	public static ItemMetalJetElytra METAL_JET_ELYTRA;
	
	public static final IAttribute FLIGHT_SPEED = (new RangedAttribute(null, "wings:generic.flightSpeed", 1, 0.1, 1024)).setShouldWatch(true);
	
	@SidedProxy(clientSide="com.elytradev.wings.ClientProxy", serverSide="com.elytradev.wings.Proxy")
	public static Proxy proxy;
	@Instance
	public static Wings inst;
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit();
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}
	
	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> e) {
		
		e.getRegistry().register((LEATHER_ELYTRA = new ItemLeatherElytra())
				.setMaxDamage(324)
				.setUnlocalizedName("wings.leather_elytra")
				.setRegistryName("leather_elytra"));
		
		e.getRegistry().register((METAL_ELYTRA = new ItemMetalElytra())
				.setMaxDamage(648)
				.setUnlocalizedName("wings.metal_elytra")
				.setRegistryName("metal_elytra"));
		
		e.getRegistry().register((METAL_JET_ELYTRA = new ItemMetalJetElytra())
				.setUnlocalizedName("wings.metal_jet_elytra")
				.setRegistryName("metal_jet_elytra"));
	}
	
}
