package com.elytradev.wings.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.Proxy;
import com.elytradev.wings.WMath;
import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer.FlightState;
import com.elytradev.wings.client.key.KeyBindingAdvanced;
import com.elytradev.wings.client.key.KeyBindingAdvancedWheel;
import com.elytradev.wings.client.render.LayerWings;
import com.elytradev.wings.client.render.WingsTileEntityItemStackRenderer;
import com.elytradev.wings.client.sound.AfterburnerSound;
import com.elytradev.wings.client.sound.AfterburnerStartSound;
import com.elytradev.wings.client.sound.QuietElytraSound;
import com.elytradev.wings.client.sound.SelfSonicBoomStartSound;
import com.elytradev.wings.client.sound.ThrusterSound;
import com.elytradev.wings.item.ItemMetalElectricElytra;
import com.elytradev.wings.item.ItemWings;
import com.elytradev.wings.network.SetFlightStateMessage;
import com.elytradev.wings.network.SetRotationAndSpeedMessage;
import com.elytradev.wings.network.SetThrusterMessage;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class ClientProxy extends Proxy {

	private static final ResourceLocation ENERGY = new ResourceLocation("wings", "textures/gui/energy.png");
	
	private KeyBinding keyToggleAdvanced;
	private KeyBinding keyRollCounterclockwise;
	private KeyBinding keyRollClockwise;
	
	private KeyBinding keyToggleThruster;
	private KeyBinding keyAfterburner;
	
	private KeyBindingAdvancedWheel keyThrottleUp;
	private KeyBindingAdvancedWheel keyThrottleDown;
	
	private KeyBinding keyBrake;
	
	private KeyBinding keyPitchUp;
	private KeyBinding keyPitchDown;
	
	private KeyBinding keyTurnLeft;
	private KeyBinding keyTurnRight;
	
	public static List<KeyBinding> advancedFlightKeybinds = Lists.newArrayList();
	
	private IKeyConflictContext ADVANCED_FLIGHT_KCC = new IKeyConflictContext() {
		
		@Override
		public boolean isActive() {
			if (Minecraft.getMinecraft().currentScreen != null) return false;
			if (Minecraft.getMinecraft().player != null) {
				return WingsPlayer.get(Minecraft.getMinecraft().player).flightState == FlightState.FLYING_ADVANCED;
			}
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
		Map<String, RenderPlayer> renders = skinMap.get(manager);
		for (Map.Entry<String, RenderPlayer> en : renders.entrySet()) {
			en.getValue().addLayer(new LayerWings(en.getValue()));
			//en.setValue(new WingsRenderPlayer(en.getValue()));
		}
		
		TileEntityItemStackRenderer.instance = new WingsTileEntityItemStackRenderer(TileEntityItemStackRenderer.instance);
		
		ForgeHooksClient.registerTESRItemStack(Wings.LEATHER_ELYTRA, 0, TileEntity.class);
		ForgeHooksClient.registerTESRItemStack(Wings.METAL_ELYTRA, 0, TileEntity.class);
		ForgeHooksClient.registerTESRItemStack(Wings.METAL_JET_ELYTRA, 0, TileEntity.class);
		ForgeHooksClient.registerTESRItemStack(Wings.METAL_ELECTRIC_ELYTRA, 0, TileEntity.class);
		
		ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		
		imm.register(Wings.LEATHER_ELYTRA, new DummyMeshDefinition("wings:leather_elytra#inventory"));
		imm.register(Wings.METAL_ELYTRA, new DummyMeshDefinition("wings:metal_elytra#inventory"));
		imm.register(Wings.METAL_JET_ELYTRA, new DummyMeshDefinition("wings:metal_jet_elytra#inventory"));
		imm.register(Wings.METAL_ELECTRIC_ELYTRA, new DummyMeshDefinition("wings:metal_electric_elytra#inventory"));
		
		imm.register(Item.getItemFromBlock(Wings.CONVERTER), new DummyMeshDefinition("wings:converter#inventory"));
		imm.register(Wings.GOGGLES, new DummyMeshDefinition("wings:goggles#inventory"));
		imm.register(Wings.BLUEPRINT, new DummyMeshDefinition("wings:blueprint#inventory"));
		
		String cat = "key.categories.wings";
		String catF = cat+".advanced";
		String catFM = catF+".movement";
		
		ClientRegistry.registerKeyBinding(keyToggleAdvanced = new KeyBinding("key.wings.toggleAdvanced", KeyConflictContext.IN_GAME, Keyboard.KEY_Z, cat));
		
		
		ClientRegistry.registerKeyBinding(keyToggleThruster = new KeyBindingAdvanced("key.wings.toggleThruster", ADVANCED_FLIGHT_KCC, Keyboard.KEY_LCONTROL, catF));
		ClientRegistry.registerKeyBinding(keyAfterburner = new KeyBindingAdvanced("key.wings.afterburner", ADVANCED_FLIGHT_KCC, Keyboard.KEY_SPACE, catF));
		
		ClientRegistry.registerKeyBinding(keyThrottleUp = new KeyBindingAdvancedWheel("key.wings.throttleUp", ADVANCED_FLIGHT_KCC, KeyBindingAdvancedWheel.WHEEL_UP, catF));
		ClientRegistry.registerKeyBinding(keyThrottleDown = new KeyBindingAdvancedWheel("key.wings.throttleDown", ADVANCED_FLIGHT_KCC, KeyBindingAdvancedWheel.WHEEL_DOWN, catF));
		
		ClientRegistry.registerKeyBinding(keyBrake = new KeyBindingAdvanced("key.wings.brake", ADVANCED_FLIGHT_KCC, Keyboard.KEY_LSHIFT, catFM));
		
		ClientRegistry.registerKeyBinding(keyRollCounterclockwise = new KeyBindingAdvanced("key.wings.rollCCW", ADVANCED_FLIGHT_KCC, Keyboard.KEY_Q, catFM));
		ClientRegistry.registerKeyBinding(keyRollClockwise = new KeyBindingAdvanced("key.wings.rollCW", ADVANCED_FLIGHT_KCC, Keyboard.KEY_E, catFM));
		
		ClientRegistry.registerKeyBinding(keyPitchUp = new KeyBindingAdvanced("key.wings.pitchUp", ADVANCED_FLIGHT_KCC, Keyboard.KEY_S, catFM));
		ClientRegistry.registerKeyBinding(keyPitchDown = new KeyBindingAdvanced("key.wings.pitchDown", ADVANCED_FLIGHT_KCC, Keyboard.KEY_W, catFM));
		
		ClientRegistry.registerKeyBinding(keyTurnLeft = new KeyBindingAdvanced("key.wings.turnLeft", ADVANCED_FLIGHT_KCC, Keyboard.KEY_A, catFM));
		ClientRegistry.registerKeyBinding(keyTurnRight = new KeyBindingAdvanced("key.wings.turnRight", ADVANCED_FLIGHT_KCC, Keyboard.KEY_D, catFM));
		
		GameSettings gm = Minecraft.getMinecraft().gameSettings;
		
		// KeyBindingAdvanced instances are implicitly added to this list
		// We only need to add exceptions to our "eat all other keybinds" rule
		
		advancedFlightKeybinds.add(keyToggleAdvanced);
		
		advancedFlightKeybinds.add(gm.keyBindChat);
		advancedFlightKeybinds.add(gm.keyBindCommand);
		advancedFlightKeybinds.add(gm.keyBindScreenshot);
		advancedFlightKeybinds.add(gm.keyBindPlayerList);
		advancedFlightKeybinds.add(gm.keyBindTogglePerspective);
		advancedFlightKeybinds.add(gm.keyBindSmoothCamera);
			
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
					if (kb instanceof KeyBindingAdvanced) {
						//arr[i] = new KeyEntryAdvanced(gkbl, (KeyBindingAdvanced)kb);
					}
				}
			}
		}
	}
	
	private final Invoker processKeyF3 = Invokers.findMethod(Minecraft.class, "processKeyF3", "func_184122_c", int.class);
	private final Invoker processKeyBinds = Invokers.findMethod(Minecraft.class, "processKeyBinds", "func_184117_aA");
	
	private final Accessor<Boolean> pressed = Accessors.findField(KeyBinding.class, "field_74513_e", "pressed");
	private final Accessor<Integer> pressTime = Accessors.findField(KeyBinding.class, "field_151474_i", "pressTime");
	private final Accessor<Map<String, KeyBinding>> KEYBIND_ARRAY = Accessors.findField(KeyBinding.class, "field_74516_a", "KEYBIND_ARRAY");
	private final Accessor<Map<String, RenderPlayer>> skinMap = Accessors.findField(RenderManager.class, "field_178636_l", "skinMap");
	
	private boolean actionKeyF3 = false;
	
	private boolean jumpTainted = false;
	private boolean lastOnGround = false;
	private FlightState lastFlightState = FlightState.NONE;
	private int flightTicks = 0;
	private int advancedFlightTicks = 0;
	private IFluidTankProperties[] lastTickFluidProperties = null;
	private int lastTickEnergy = -1;
	
	private boolean wheelUp;
	private boolean wheelDown;
	
	private float oldThrusterValue;
	
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent e) {
		if (e.phase == Phase.START) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.player != null) {
				EntityPlayer ep = mc.player;
				WingsPlayer wp = WingsPlayer.get(ep);
				if (wp.flightState == FlightState.FLYING_ADVANCED) {
					if (wp.rotation == null) return;
					if (mc.currentScreen != null) return;
					int dxRaw = Mouse.getDX();
					int dyRaw = Mouse.getDY();
					
					float sensitivity = mc.gameSettings.mouseSensitivity * 0.4f + 0.2f;
					float mult = sensitivity * sensitivity * sensitivity * 8;
					float dx = dxRaw * mult;
					float dy = dyRaw * mult;
	
					if (mc.gameSettings.invertMouse) {
						dy *= -1;
					}
					
					if (dx != 0) {
						Quat4d yawQ = new Quat4d();
						yawQ.set(new AxisAngle4d(0, 1, 0, WMath.deg2rad(dx)));
						
						wp.rotation.mul(wp.rotation, yawQ);
					}
					if (dy != 0) {
						Quat4d pitchQ = new Quat4d();
						pitchQ.set(new AxisAngle4d(1, 0, 0, WMath.deg2rad(-dy)));
						
						wp.rotation.mul(pitchQ, wp.rotation);
					}
					
					// for frustrum culling and correct facing when leaving advanced mode
					ep.rotationYaw = ep.prevRotationYaw = (float)(WMath.rad2deg(WMath.getYaw(wp.rotation))+180);
					ep.rotationPitch = ep.prevRotationPitch = (float)(WMath.rad2deg(WMath.getPitch(wp.rotation)));
				}
			}
			
			if (mc.world != null) {
				for (EntityPlayer ep : mc.world.playerEntities) {
					Optional<WingsPlayer> opt = WingsPlayer.getIfExists(ep);
					if (opt.isPresent()) {
						WingsPlayer wp = opt.get();
						if (wp.rotation != null) {
							//ep.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, ep.posX, ep.posY, ep.posZ, 0, 0, 0);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer ep = mc.player;
			if (ep != null) {
				WingsPlayer wp = WingsPlayer.get(ep);
				if (wp.flightState == FlightState.FLYING_ADVANCED) {
					// handle the events ourselves to suppress unwanted "universal" keybinds
					processKeyboard();
					processMouse();
					
					new SetRotationAndSpeedMessage(wp).sendToServer();
				}
			} else if (mc.currentScreen instanceof GuiControls) {
				// to add scroll wheel support
				processKeyboard();
				processMouse();
			}
		} else if (e.phase == Phase.END) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP ep = mc.player;
			if (ep != null) {
				ItemStack chest = ep.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				
				if (lastFlightState == FlightState.FLYING || lastFlightState == FlightState.FLYING_ADVANCED) {
					flightTicks++;
				} else if (flightTicks > 10) {
					flightTicks = 10;
				} else if (flightTicks > 0) {
					flightTicks--;
				}
				
				if (lastFlightState == FlightState.FLYING_ADVANCED) {
					advancedFlightTicks++;
				} else if (advancedFlightTicks > 10) {
					advancedFlightTicks = 10;
				} else if (advancedFlightTicks > 0) {
					advancedFlightTicks--;
				}
				
				WingsPlayer wp = WingsPlayer.get(ep);
				FlightState newState;
				if (chest.getItem() instanceof ItemWings) {
					ItemWings wings = (ItemWings)chest.getItem();
					
					if (!mc.gameSettings.keyBindJump.isKeyDown()) {
						jumpTainted = false;
					}
					if (mc.gameSettings.keyBindJump.isKeyDown() || keyToggleAdvanced.isKeyDown() && !jumpTainted && lastFlightState == FlightState.NONE) {
						if (!ep.onGround && ep.motionY < 0 && !ep.isInWater()) {
							mc.getSoundHandler().playSound(new ThrusterSound(ep));
							mc.getSoundHandler().playSound(new QuietElytraSound(ep));
							newState = keyToggleAdvanced.isKeyDown() ? FlightState.FLYING_ADVANCED : FlightState.FLYING;
						} else {
							newState = lastFlightState;
							jumpTainted = true;
						}
					} else {
						newState = lastFlightState;
					}
					if (newState == FlightState.FLYING && ep.onGround && !lastOnGround) {
						newState = FlightState.NONE;
					}
					if (keyToggleAdvanced.isPressed()) {
						if (newState == FlightState.FLYING) {
							wp.thruster = 0.5f;
							wp.afterburner = false;
							wp.brake = false;
							oldThrusterValue = 0;
							newState = FlightState.FLYING_ADVANCED;
							new SetThrusterMessage(0.5f).sendToServer();
						} else if (newState == FlightState.FLYING_ADVANCED) {
							wp.thruster = 0;
							newState = FlightState.FLYING;
						}
					}
					if (wings.hasThruster()) {
						if (newState == FlightState.FLYING) {
							boolean jump = mc.gameSettings.keyBindJump.isKeyDown();
							boolean sneak = mc.gameSettings.keyBindSneak.isKeyDown();
							if (jump && !wp.afterburner && !wp.brake) {
								wp.afterburner = true;
								wp.thruster = 0.6f; // for thruster sound
								new SetThrusterMessage(SetThrusterMessage.AFTERBURNER_SPEED).sendToServer();
							} else if (!jump && wp.afterburner) {
								wp.afterburner = false;
								wp.thruster = 0; // for thruster sound
								new SetThrusterMessage(0).sendToServer();
							} else if (sneak && !wp.brake && !wp.afterburner) {
								wp.brake = true;
								new SetThrusterMessage(SetThrusterMessage.BRAKE_SPEED).sendToServer();
							} else if (!sneak && wp.brake) {
								wp.brake = false;
								new SetThrusterMessage(0).sendToServer();
							}
						} else if (newState == FlightState.FLYING_ADVANCED) {
							if (wheelDown || keyThrottleDown.isKeyDown()) {
								wp.thruster -= 0.05f;
								if (wp.thruster < 0) {
									wp.thruster = 0;
								}
							}
							
							if (wheelUp || keyThrottleDown.isKeyDown()) {
								wp.thruster += 0.05f;
								if (wp.thruster > 1) {
									wp.thruster = 1;
								}
							}
							
							wp.afterburner = keyAfterburner.isKeyDown() && wings.hasAfterburner();
							// isKeyDown checks KeyModifier, which seems to be broken with our input code
							wp.brake = pressed.get(keyBrake);
							
							if (wp.afterburner && !wp.lastTickAfterburner) {
								mc.getSoundHandler().playSound(new AfterburnerStartSound(ep));
							}
							if (wp.sonicBoom && !wp.lastTickSonicBoom) {
								mc.getSoundHandler().stopSounds();
								mc.getSoundHandler().playSound(new SelfSonicBoomStartSound(ep));
							}
							if (!wp.sonicBoom && wp.lastTickSonicBoom) {
								if (wp.afterburner) {
									mc.getSoundHandler().playSound(new AfterburnerSound(ep));
								}
								mc.getSoundHandler().playSound(new ThrusterSound(ep));
								mc.getSoundHandler().playSound(new QuietElytraSound(ep));
							}
							
							wheelUp = false;
							wheelDown = false;
							
							if (keyToggleThruster.isPressed()) {
								if (wp.thruster > 0) {
									oldThrusterValue = wp.thruster;
									wp.thruster = 0;
								} else {
									wp.thruster = oldThrusterValue;
								}
							}
							
							if (wp.thruster != wp.lastTickThruster ||
									wp.afterburner != wp.lastTickAfterburner ||
									wp.brake != wp.lastTickBrake) {
								if (wp.afterburner) {
									new SetThrusterMessage(SetThrusterMessage.AFTERBURNER_SPEED).sendToServer();
								} else if (wp.brake) {
									new SetThrusterMessage(SetThrusterMessage.BRAKE_SPEED).sendToServer();
								} else {
									new SetThrusterMessage(wp.thruster).sendToServer();
								}
							}
						}
					}
					lastOnGround = mc.player.onGround;
				} else {
					newState = FlightState.NONE;
				}
				if (newState == FlightState.FLYING_ADVANCED) {
					if (keyRollClockwise.isKeyDown()) {
						wp.motionRoll += 0.5f;
					}
					if (keyRollCounterclockwise.isKeyDown()) {
						wp.motionRoll -= 0.5f;
					}
					
					if (keyTurnLeft.isKeyDown()) {
						wp.motionYaw -= 0.5f;
					}
					if (keyTurnRight.isKeyDown()) {
						wp.motionYaw += 0.5f;
					}
					
					if (keyPitchUp.isKeyDown()) {
						wp.motionPitch -= 0.5f;
					}
					if (keyPitchDown.isKeyDown()) {
						wp.motionPitch += 0.5f;
					}
				}
				if (newState != lastFlightState) {
					if (newState == FlightState.FLYING_ADVANCED) {
						// prevent keys sticking from our input overrides
						for (KeyBinding kb : KEYBIND_ARRAY.get(null).values()) {
							if (!advancedFlightKeybinds.contains(kb)) {
								pressed.set(kb, false);
								pressTime.set(kb, 0);
							}
						}
					}
					lastFlightState = newState;
					wp.flightState = newState;
					new SetFlightStateMessage(newState).sendToServer();
				}
			} else {
				lastFlightState = FlightState.NONE;
			}
		}
	}
	
	private static final  Accessor<Integer> event_dwheel = Accessors.findField(Mouse.class, "event_dwheel");
	private static Object opengl_globalLock;
	
	private void processMouse() {
		Minecraft mc = Minecraft.getMinecraft();
		while (Mouse.next()) {
			if (mc.currentScreen != null) {
				try {
					if (mc.currentScreen instanceof GuiControls) {
						GuiControls gc = (GuiControls)mc.currentScreen;
						int dw = Mouse.getEventDWheel();
						if (dw != 0 && gc.buttonId != null) {
							if (gc.buttonId instanceof KeyBindingAdvancedWheel) {
								int val = dw > 0 ? KeyBindingAdvancedWheel.WHEEL_UP : KeyBindingAdvancedWheel.WHEEL_DOWN;
								gc.buttonId.setKeyModifierAndCode(KeyModifier.getActiveModifier(), val);
								mc.gameSettings.setOptionKeyBinding(gc.buttonId, val);
								gc.buttonId = null;
								KeyBinding.resetKeyBindingArrayAndHash();
							}
							if (opengl_globalLock == null) {
								Field f = Class.forName("org.lwjgl.opengl.GlobalLock").getDeclaredField("lock");
								f.setAccessible(true);
								opengl_globalLock = f.get(null);
							}
							synchronized (opengl_globalLock) {
								event_dwheel.set(null, 0);
							}
						}
					}
					mc.currentScreen.handleMouseInput();
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}
			int dwheel = Mouse.getEventDWheel();
			if (dwheel != 0) {
				if (keyThrottleUp.doesMatchWheel(dwheel)) {
					wheelUp = true;
				}
				if (keyThrottleDown.doesMatchWheel(dwheel)) {
					wheelDown = true;
				}
			}
			int button = Mouse.getEventButton();
			boolean state = Mouse.getEventButtonState();
			setAdvancedButtonsPressed(button-100, state);
			if (!mc.inGameHasFocus && state) {
				mc.setIngameFocus();
			}
			if (state) {
			}
		}
	}
	
	private void processKeyboard() {
		Minecraft mc = Minecraft.getMinecraft();
		while (Keyboard.next()) {
			int keyCode = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

			mc.dispatchKeypresses();

			// we don't want to mess up GUIs
			if (mc.currentScreen != null) {
				try {
					mc.currentScreen.handleKeyboardInput();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			
			boolean press = Keyboard.getEventKeyState();

			if (press) {
				if (keyCode == Keyboard.KEY_F4 && mc.entityRenderer != null) {
					mc.entityRenderer.switchUseShader();
				}

				boolean f3 = false;

				if (mc.currentScreen == null) {
					if (keyCode == Keyboard.KEY_ESCAPE) {
						mc.displayInGameMenu();
					}

					f3 = Keyboard.isKeyDown(Keyboard.KEY_F3) && (Boolean)processKeyF3.invoke(mc, keyCode);
					actionKeyF3 |= f3;

					if (keyCode == Keyboard.KEY_F1) {
						mc.gameSettings.hideGUI = !mc.gameSettings.hideGUI;
					}
				}

				if (f3) {
					setAdvancedButtonsPressed(keyCode, false);
				} else {
					setAdvancedButtonsPressed(keyCode, true);
				}
			} else {
				setAdvancedButtonsPressed(keyCode, false);

				if (keyCode == Keyboard.KEY_F3) {
					if (actionKeyF3) {
						actionKeyF3 = false;
					} else {
						mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
						mc.gameSettings.showDebugProfilerChart = mc.gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
						mc.gameSettings.showLagometer = mc.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
					}
				}
			}
			
			processKeyBinds.invoke(mc);
		}
	}
	
	private void setAdvancedButtonsPressed(int keyCode, boolean state) {
		for (KeyBinding kb : advancedFlightKeybinds) {
			// isActiveAndMatches checkes KeyModifier, which is broken with our input code
			if (keyCode == kb.getKeyCode() && kb.getKeyModifier().isActive(null)) {
				pressed.set(kb, state);
				if (state) {
					pressTime.set(kb, pressTime.get(kb) + 1);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onDrawBlockHighlight(DrawBlockHighlightEvent e) {
		if (lastFlightState == FlightState.FLYING_ADVANCED) {
			e.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onPlaySoundEvent(PlaySoundEvent e) {
		Optional<WingsPlayer> opt = WingsPlayer.getIfExists(Minecraft.getMinecraft().player);
		if (opt.isPresent()) {
			WingsPlayer wp = opt.get();
			if (wp.sonicBoom) {
				if (e.getName().startsWith("sonic_boom_self")) return;
				e.setResultSound(null);
			} else if (wp.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemWings) {
				if (e.getSound() instanceof ElytraSound && !(e.getSound() instanceof QuietElytraSound)) {
					e.setResultSound(null);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderHand(RenderSpecificHandEvent e) {
		if (advancedFlightTicks > 10) {
			e.setCanceled(true);
		} else if (advancedFlightTicks > 0) {
			float interp = interpolateEase(advancedFlightTicks, 10, e.getPartialTicks(), lastFlightState == FlightState.FLYING_ADVANCED);
			GlStateManager.translate(0, -interp/2, 0);
		}
	}
	
	@SubscribeEvent
	public void onFOV(FOVModifier e) {
		if (lastFlightState != FlightState.NONE) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			WingsPlayer wp = WingsPlayer.get(player);
			if (wp.sonicBoom) {
				e.setFOV(e.getFOV()*2.5f);
			} else {
				float speed = (float)Math.sqrt((player.motionX * player.motionX) + (player.motionY * player.motionY) + (player.motionZ * player.motionZ));
				speed /= WingsPlayer.SOUND_BARRIER;
				e.setFOV(e.getFOV()*(1+(speed/2)));
			}
		}
	}
	
	@SubscribeEvent
	public void onCameraSetup(CameraSetup e) {
		if (lastFlightState == FlightState.FLYING_ADVANCED) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			WingsPlayer wp = WingsPlayer.get(player);
			if (wp.rotation != null) {
				e.setRoll(0);
				e.setYaw(0);
				e.setPitch(0);
				if (Minecraft.getMinecraft().gameSettings.thirdPersonView != 2) {
					Rendering.rotate(wp.prevRotation, wp.rotation, (float)e.getRenderPartialTicks());
				}
			}
		}
	}
	
	private final NumberFormat frac1 = NumberFormat.getInstance(); {
		frac1.setGroupingUsed(false);
		frac1.setMaximumFractionDigits(1);
	}
	
	private final NumberFormat frac6 = NumberFormat.getInstance(); {
		frac6.setGroupingUsed(false);
		frac6.setMaximumFractionDigits(6);
	}
	
	@SubscribeEvent
	public void onRenderText(RenderGameOverlayEvent.Text e) {
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			Optional<WingsPlayer> opt = WingsPlayer.getIfExists(Minecraft.getMinecraft().player);
			if (opt.isPresent()) {
				WingsPlayer wp = opt.get();
				if (wp.rotation != null) {
					ListIterator<String> iter = e.getLeft().listIterator();
					int idx = e.getLeft().size();
					while (iter.hasNext()) {
						String str = iter.next();
						if (str.startsWith("Facing: ")) {
							iter.set("Facing: N/A");
						} else if (str.startsWith("Local Difficulty: ")) {
							idx = iter.nextIndex();
						}
					}
					
					double yaw = WMath.rad2deg(WMath.getYaw(wp.rotation));
					double pitch = WMath.rad2deg(WMath.getPitch(wp.rotation));
					double roll = WMath.rad2deg(WMath.getRoll(wp.rotation));
					e.getLeft().add(idx++, "");
					e.getLeft().add(idx++, "\u00A7b[Wings]\u00A7r Euler (Y/P/R): "+frac1.format(yaw)+"° / "+frac1.format(pitch)+"° / "+frac1.format(roll)+"°");
					e.getLeft().add(idx++, "\u00A7b[Wings]\u00A7r Quaternion: "+frac6.format(wp.rotation.x)+", "+frac6.format(wp.rotation.y)+", "+frac6.format(wp.rotation.z)+", "+frac6.format(wp.rotation.w));
				}
			}
		}
	}
	
	@SubscribeEvent(receiveCanceled=true,priority=EventPriority.HIGHEST)
	public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
		if (advancedFlightTicks > 0) {
			float partial = e.getPartialTicks();
			if (lastFlightState != FlightState.FLYING_ADVANCED) {
				partial *= -1;
			}
			GlStateManager.pushMatrix();
			float interp = MathHelper.sin((Math.min(advancedFlightTicks+partial, 10)/20f)*((float)Math.PI));
			if (e.getType() == ElementType.CROSSHAIRS) {
				e.setCanceled(true);
				Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.pushMatrix();
				GlStateManager.translate(e.getResolution().getScaledWidth_double()/2, e.getResolution().getScaledHeight_double()/2, 0);
				GlStateManager.disableTexture2D();
				float stretch = interp*6;
				float rot = 0;
				WingsPlayer wp = WingsPlayer.get(Minecraft.getMinecraft().player);
				if (wp.rotation != null) {
					rot = (float) (WMath.rad2deg(WMath.getRoll(wp.rotation)) * interp);
				}
				GlStateManager.rotate(rot, 0, 0, 1);
				Rendering.drawRect(-5-stretch, -0.5f, 5+stretch, 0.5f, 0xFFFFFFFF);
				Rendering.drawRect(-0.5f, -4, 0.5f, -0.5f, 0xFFFFFFFF);
				Rendering.drawRect(-0.5f, 0.5f, 0.5f, 4, 0xFFFFFFFF);
				GlStateManager.enableTexture2D();
				GlStateManager.popMatrix();
				GlStateManager.disableBlend();
			} else if (e.getType() == ElementType.HOTBAR) {
				if (advancedFlightTicks > 10) {
					e.setCanceled(true);
				} else {
					GlStateManager.translate(0, interp*23, 0);
				}
			} else if (e.getType() == ElementType.ARMOR || e.getType() == ElementType.EXPERIENCE || e.getType() == ElementType.FOOD || e.getType() == ElementType.HEALTH || e.getType() == ElementType.AIR) {
				GlStateManager.translate(0, interp*23, 0);
			}
		}
	}
	
	@SubscribeEvent(receiveCanceled=true,priority=EventPriority.LOWEST)
	public void onPostRenderGameOverlay(RenderGameOverlayEvent.Post e) {
		if (advancedFlightTicks > 0) {
			GlStateManager.popMatrix();
		}
		if (e.getType() == ElementType.HOTBAR) {
			
		} else if (e.getType() == ElementType.ALL) {
			EntityPlayerSP ep = Minecraft.getMinecraft().player;
			WingsPlayer wp = WingsPlayer.get(ep);
			ItemStack chest = ep.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			int y = 5;
			int barWidth = 60;
			int x = -(barWidth + 5);
			if (flightTicks > 0) {
				double mY = ep.onGround ? 0 : ep.motionY;
				double speed = ep.motionX * ep.motionX + mY * mY + ep.motionZ * ep.motionZ;
				speed = MathHelper.sqrt(speed);
				speed *= 72;
				
				float interp = interpolateEase(flightTicks, 10, e.getPartialTicks(), lastFlightState != FlightState.NONE);
				
				if (chest.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
					IFluidHandlerItem ifhi = chest.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
					GlStateManager.pushMatrix();
					GlStateManager.translate(interp*(-x), 0, 0);
					IFluidTankProperties[] props = ifhi.getTankProperties();
					for (int i = 0; i < props.length; i++) {
						IFluidTankProperties prop = props[i];
						IFluidTankProperties lastTickProp = lastTickFluidProperties != null && lastTickFluidProperties.length == props.length ? lastTickFluidProperties[i] : null;
						FluidStack content = prop.getContents();
						FluidStack lastTickContent = lastTickProp == null ? null : lastTickProp.getContents();
						
						TextureAtlasSprite tas = null;
						
						float amt = 0;
						float max = 1;
						int color = -1;
						if (content != null || lastTickContent != null) {
							FluidStack template = content == null ? lastTickContent : content;
							amt = template.amount;
							if (lastTickContent != null) {
								amt = lastTickContent.amount + ((lastTickContent.amount - template.amount) * e.getPartialTicks());
							}
							max = prop.getCapacity();
							tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(template.getFluid().getStill(template).toString());
							color = template.getFluid().getColor(template);
						}
						
						Gui.drawRect(x+4, y, x+barWidth+6, y+12, 0xFF880000);
						Gui.drawRect(x+5, y+1, x+barWidth+5, y+11, 0xFF222222);
						if (amt > 0 && tas != null) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
							double px = (amt/max)*barWidth;
							int full = (int)(px/16);
							GlStateManager.color(1, 1, 1);
							for (int j = 0; j < full; j++) {
								drawTexturedRect(x+5+(j*16), y+1, tas, 16, 10, color, false);
							}
							drawTexturedRect(x+5+(full*16), y+1, tas, px-(full*16), 10, color, false);
						}
						Gui.drawRect(x+5+(barWidth/4), y, x+(barWidth/4)+6, y+5, 0xFF880000);
						Gui.drawRect(x+5+(barWidth/2), y, x+(barWidth/2)+6, y+9, 0xFF880000);
						Gui.drawRect(x+5+((barWidth/4)*3), y, x+((barWidth/4)*3)+6, y+5, 0xFF880000);
						y += 16;
					}
					lastTickFluidProperties = props.clone();
					for (int i = 0; i < lastTickFluidProperties.length; i++) {
						lastTickFluidProperties[i] = new FluidTankProperties(lastTickFluidProperties[i].getContents(), lastTickFluidProperties[i].getCapacity());
					}
					GlStateManager.popMatrix();
				} else {
					lastTickFluidProperties = null;
				}
				
				if (chest.hasCapability(CapabilityEnergy.ENERGY, null)) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(interp*(-x), 0, 0);
					IEnergyStorage ies = chest.getCapability(CapabilityEnergy.ENERGY, null);
					int energy = ies.getEnergyStored();
					
					float amt = energy;
					if (lastTickEnergy != -1) {
						amt = lastTickEnergy + ((lastTickEnergy - energy) * e.getPartialTicks());
					}
					float max = ItemMetalElectricElytra.FU_CAPACITY;
					
					Gui.drawRect(x+4, y, x+barWidth+6, y+12, 0xFF444444);
					Minecraft.getMinecraft().getTextureManager().bindTexture(ENERGY);
					GlStateManager.color(1, 1, 1);
					Rendering.drawTexturedRect(x+5, y+1, 0, 0, barWidth, 10, 60, 20);
					if (amt > 0) {
						float px = (amt/max)*barWidth;
						Rendering.drawTexturedRect(x+5, y+1, 0, 10, px, 10, 60, 20);
					}
					
					y += 16;
					lastTickEnergy = energy;
					GlStateManager.popMatrix();
				} else {
					lastTickEnergy = -1;
				}
				
				GlStateManager.pushMatrix();
				FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
				String str = ((int)speed)+" km/h";
				int w = fr.getStringWidth(str);
				GlStateManager.translate(interp*(w+5), 0, 0);
				fr.drawStringWithShadow(str, -w, y, -1);
				y += 12;
				GlStateManager.popMatrix();
			}
			if (advancedFlightTicks > 0) {
				float interp = interpolateEase(advancedFlightTicks, 10, e.getPartialTicks(), lastFlightState == FlightState.FLYING_ADVANCED);
				
				GlStateManager.pushMatrix();
				FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
				if (chest.getItem() instanceof ItemWings && ((ItemWings)chest.getItem()).hasThruster()) {
					String str;
					if (((ItemWings)chest.getItem()).isFuelDepleted(chest)) {
						str = I18n.format("hud.wings.thruster.no_fuel");
					} else if (wp.afterburner) {
						str = I18n.format("hud.wings.thruster.afterburner");
					} else if (wp.thruster > 0) {
						str = I18n.format("hud.wings.thruster", Math.round(wp.thruster*100));
					} else {
						str = I18n.format("hud.wings.thruster.off");
					}
					int w = fr.getStringWidth(str);
					GlStateManager.translate(interp*(w+5), 0, 0);
					fr.drawStringWithShadow(str, -w, y, -1);
					y += 12;
				}
				GlStateManager.popMatrix();
			}
		}
	}

	private static final float PIf = (float)Math.PI;
	
	private float interpolateEase(int ticks, int length, float partialTicks, boolean direction) {
		if (ticks < length) {
			if (!direction) {
				partialTicks *= -1;
			}
			return MathHelper.sin((Math.min(ticks+partialTicks, length)/(length*2))*PIf);
		} else {
			return 1;
		}
	}

	public static void drawTexturedRect(double x, double y, TextureAtlasSprite tex, double w, double h, int color, boolean flipped) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder bb = tess.getBuffer();
		
		float minU = tex.getInterpolatedU(0);
		float maxU = tex.getInterpolatedU(w);
		
		float minV = tex.getInterpolatedV(flipped ? h : 0);
		float maxV = tex.getInterpolatedV(flipped ? 0 : h);
		
		GlStateManager.color(((color>>16)&0xFF)/255f, ((color>>8)&0xFF)/255f, (color&0xFF)/255f, ((color>>24)&0xFF)/255f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		bb.begin(7, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x + 0, y + h, 0).tex(minU, maxV).endVertex();
		bb.pos(x + w, y + h, 0).tex(maxU, maxV).endVertex();
		bb.pos(x + w, y + 0, 0).tex(maxU, minV).endVertex();
		bb.pos(x + 0, y + 0, 0).tex(minU, minV).endVertex();
		tess.draw();
	}
	
}
