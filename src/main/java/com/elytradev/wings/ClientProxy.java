package com.elytradev.wings;

import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
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
	public void postInit() {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		Map<String, RenderPlayer> renders = manager.getSkinMap();
		for (RenderPlayer render : renders.values()) {
			render.addLayer(new LayerWings(render));
		}
		
		String cat = "key.categories.wings";
		
		ClientRegistry.registerKeyBinding(TOGGLE_FLIGHT_MODE = new KeyBinding("key.wings.toggleFlightMode", KeyConflictContext.IN_GAME, Keyboard.KEY_Z, cat));
		
		ClientRegistry.registerKeyBinding(ROLL_CCW = new KeyBindingFlightMode("key.wings.rollCCW", FLIGHT_MODE_KCC, Keyboard.KEY_Q, cat));
		ClientRegistry.registerKeyBinding(ROLL_CW = new KeyBindingFlightMode("key.wings.rollCW", FLIGHT_MODE_KCC, Keyboard.KEY_E, cat));
		
		ClientRegistry.registerKeyBinding(TOGGLE_THRUSTER = new KeyBindingFlightMode("key.wings.toggleThruster", FLIGHT_MODE_KCC, Keyboard.KEY_LCONTROL, cat));
		ClientRegistry.registerKeyBinding(AFTERBURNER = new KeyBindingFlightMode("key.wings.afterburner", FLIGHT_MODE_KCC, Keyboard.KEY_SPACE, cat));
		
		ClientRegistry.registerKeyBinding(THROTTLE_UP = new KeyBindingFlightMode("key.wings.throttleUp", FLIGHT_MODE_KCC, Keyboard.KEY_NONE, cat) {
			@Override
			public String getDisplayName() {
				if (getKeyCode() == Keyboard.KEY_NONE) {
					return "Wheel Up";
				}
				return super.getDisplayName();
			}
		});
		ClientRegistry.registerKeyBinding(THROTTLE_DOWN = new KeyBindingFlightMode("key.wings.throttleDown", FLIGHT_MODE_KCC, Keyboard.KEY_NONE, cat) {
			@Override
			public String getDisplayName() {
				if (getKeyCode() == Keyboard.KEY_NONE) {
					return "Wheel Down";
				}
				return super.getDisplayName();
			}
			
		});
		
		ClientRegistry.registerKeyBinding(BRAKE = new KeyBindingFlightMode("key.wings.brake", FLIGHT_MODE_KCC, Keyboard.KEY_LSHIFT, cat));
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private final Accessor<GuiKeyBindingList> keyBindingList = Accessors.findField(GuiControls.class, "field_146494_r", "keyBindingList");
	private final Accessor<IGuiListEntry[]> listEntries = Accessors.findField(GuiKeyBindingList.class, "field_148190_m", "listEntries");
	private final Accessor<KeyBinding> keybinding = Accessors.findField(KeyEntry.class, "field_148282_b", "keybinding");
	
	
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
