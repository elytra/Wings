package com.elytradev.wings.client;

import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.wings.Proxy;
import com.elytradev.wings.Wings;
import com.elytradev.wings.client.key.KeyBindingFlightMode;
import com.elytradev.wings.client.key.KeyEntryFlightMode;
import com.elytradev.wings.client.render.LayerWings;
import com.elytradev.wings.client.render.WingsTileEntityItemStackRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientProxy extends Proxy {

	private KeyBinding TOGGLE_FLIGHT_MODE;
	private KeyBinding ROLL_CCW;
	private KeyBinding ROLL_CW;
	
	private KeyBinding TOGGLE_THRUSTER;
	private KeyBinding AFTERBURNER;
	
	private KeyBinding THROTTLE_UP;
	private KeyBinding THROTTLE_DOWN;
	
	private KeyBinding BRAKE;
	
	private KeyBinding PITCH_UP;
	private KeyBinding PITCH_DOWN;
	
	private KeyBinding TURN_LEFT;
	private KeyBinding TURN_RIGHT;
	
	private IKeyConflictContext FLIGHT_MODE_KCC = new IKeyConflictContext() {
		
		@Override
		public boolean isActive() {
			// TODO
			return false;
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return other == this;
		}
	};
	
	@Override
	public void preInit() {
	}
	
	@Override
	public void postInit() {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		Map<String, RenderPlayer> renders = manager.getSkinMap();
		for (RenderPlayer render : renders.values()) {
			render.addLayer(new LayerWings(render));
		}
		
		TileEntityItemStackRenderer.instance = new WingsTileEntityItemStackRenderer(TileEntityItemStackRenderer.instance);
		
		ForgeHooksClient.registerTESRItemStack(Wings.LEATHER_ELYTRA, 0, TileEntity.class);
		ForgeHooksClient.registerTESRItemStack(Wings.METAL_ELYTRA, 0, TileEntity.class);
		ForgeHooksClient.registerTESRItemStack(Wings.METAL_JET_ELYTRA, 0, TileEntity.class);
		
		ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		
		imm.register(Wings.LEATHER_ELYTRA, new DummyMeshDefinition("wings:leather_elytra#inventory"));
		imm.register(Wings.METAL_ELYTRA, new DummyMeshDefinition("wings:metal_elytra#inventory"));
		imm.register(Wings.METAL_JET_ELYTRA, new DummyMeshDefinition("wings:metal_jet_elytra#inventory"));
		
		imm.register(Item.getItemFromBlock(Wings.CONVERTER), new DummyMeshDefinition("wings:converter#inventory"));
		
		String cat = "key.categories.wings";
		String catF = cat+".inFlightMode";
		String catFM = catF+".movement";
		
		ClientRegistry.registerKeyBinding(TOGGLE_FLIGHT_MODE = new KeyBinding("key.wings.toggleFlightMode", KeyConflictContext.IN_GAME, Keyboard.KEY_Z, cat));
		
		
		ClientRegistry.registerKeyBinding(TOGGLE_THRUSTER = new KeyBindingFlightMode("key.wings.toggleThruster", FLIGHT_MODE_KCC, Keyboard.KEY_LCONTROL, catF));
		ClientRegistry.registerKeyBinding(AFTERBURNER = new KeyBindingFlightMode("key.wings.afterburner", FLIGHT_MODE_KCC, Keyboard.KEY_SPACE, catF));
		
		ClientRegistry.registerKeyBinding(THROTTLE_UP = new KeyBindingFlightMode("key.wings.throttleUp", FLIGHT_MODE_KCC, Keyboard.KEY_NONE, catF) {
			@Override
			public String getDisplayName() {
				if (getKeyCode() == Keyboard.KEY_NONE) {
					return "Wheel Up";
				}
				return super.getDisplayName();
			}
		});
		ClientRegistry.registerKeyBinding(THROTTLE_DOWN = new KeyBindingFlightMode("key.wings.throttleDown", FLIGHT_MODE_KCC, Keyboard.KEY_NONE, catF) {
			@Override
			public String getDisplayName() {
				if (getKeyCode() == Keyboard.KEY_NONE) {
					return "Wheel Down";
				}
				return super.getDisplayName();
			}
			
		});
		
		ClientRegistry.registerKeyBinding(BRAKE = new KeyBindingFlightMode("key.wings.brake", FLIGHT_MODE_KCC, Keyboard.KEY_LSHIFT, catFM));
		
		ClientRegistry.registerKeyBinding(ROLL_CCW = new KeyBindingFlightMode("key.wings.rollCCW", FLIGHT_MODE_KCC, Keyboard.KEY_Q, catFM));
		ClientRegistry.registerKeyBinding(ROLL_CW = new KeyBindingFlightMode("key.wings.rollCW", FLIGHT_MODE_KCC, Keyboard.KEY_E, catFM));
		
		ClientRegistry.registerKeyBinding(PITCH_UP = new KeyBindingFlightMode("key.wings.pitchUp", FLIGHT_MODE_KCC, Keyboard.KEY_W, catFM));
		ClientRegistry.registerKeyBinding(PITCH_DOWN = new KeyBindingFlightMode("key.wings.pitchDown", FLIGHT_MODE_KCC, Keyboard.KEY_S, catFM));
		
		ClientRegistry.registerKeyBinding(TURN_LEFT = new KeyBindingFlightMode("key.wings.turnLeft", FLIGHT_MODE_KCC, Keyboard.KEY_A, catFM));
		ClientRegistry.registerKeyBinding(TURN_RIGHT = new KeyBindingFlightMode("key.wings.turnRight", FLIGHT_MODE_KCC, Keyboard.KEY_D, catFM));
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private final Accessor<GuiKeyBindingList> keyBindingList = Accessors.findField(GuiControls.class, "field_146494_r", "keyBindingList");
	private final Accessor<IGuiListEntry[]> listEntries = Accessors.findField(GuiKeyBindingList.class, "field_148190_m", "listEntries");
	private final Accessor<KeyBinding> keybinding = Accessors.findField(KeyEntry.class, "field_148282_b", "keybinding");
	
	@SubscribeEvent
	public void onModelRegister(ModelRegistryEvent e) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wings.CONVERTER), 0, new ModelResourceLocation("wings:converter#inventory"));
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post e) {
		if (e.getGui() instanceof GuiControls) {
			GuiControls gc = (GuiControls)e.getGui();
			GuiKeyBindingList gkbl = keyBindingList.get(gc);
			IGuiListEntry[] arr = listEntries.get(gkbl);
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] instanceof KeyEntry) {
					KeyEntry ke = (KeyEntry)arr[i];
					KeyBinding kb = keybinding.get(ke);
					if (kb instanceof KeyBindingFlightMode) {
						arr[i] = new KeyEntryFlightMode(gkbl, (KeyBindingFlightMode)kb);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
		}
	}
	
}
