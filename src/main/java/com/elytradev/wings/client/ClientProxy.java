package com.elytradev.wings.client;

import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.Proxy;
import com.elytradev.wings.Wings;
import com.elytradev.wings.WingsPlayer.FlightState;
import com.elytradev.wings.client.key.KeyBindingFlightMode;
import com.elytradev.wings.client.key.KeyEntryFlightMode;
import com.elytradev.wings.client.render.LayerWings;
import com.elytradev.wings.client.render.WingsTileEntityItemStackRenderer;
import com.elytradev.wings.item.ItemWings;
import com.elytradev.wings.network.SetFlightStateMessage;
import com.elytradev.wings.network.SetThrusterMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiKeyBindingList.KeyEntry;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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
			if (Minecraft.getMinecraft().player != null) {
				return WingsPlayer.get(Minecraft.getMinecraft().player).flightState == FlightState.FLYING_FLIGHT_MODE;
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
		imm.register(Wings.GOGGLES, new DummyMeshDefinition("wings:goggles#inventory"));
		
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
	
	private boolean jumpTainted = false;
	private boolean lastOnGround = false;
	private boolean lastToggleFlightModeIsDown = false;
	private FlightState lastFlightState = FlightState.NONE;
	private int flightTicks = 0;
	private int flightModeTicks = 0;
	private IFluidTankProperties[] lastTickFluidProperties = null;
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP ep = mc.player;
			if (ep != null) {
				ItemStack chest = ep.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				
				if (lastFlightState == FlightState.FLYING || lastFlightState == FlightState.FLYING_FLIGHT_MODE) {
					flightTicks++;
				} else if (flightTicks > 10) {
					flightTicks = 10;
				} else if (flightTicks > 0) {
					flightTicks--;
				}
				
				WingsPlayer wp = WingsPlayer.get(ep);
				FlightState newState;
				if (chest.getItem() instanceof ItemWings) {
					ItemWings wings = (ItemWings)chest.getItem();
					
					if (lastFlightState == FlightState.FLYING_FLIGHT_MODE) {
						flightModeTicks++;
					} else if (flightModeTicks > 10) {
						flightModeTicks = 10;
					} else if (flightModeTicks > 0) {
						flightModeTicks--;
					}
					
					if (!mc.gameSettings.keyBindJump.isKeyDown()) {
						jumpTainted = false;
					}
					if (mc.gameSettings.keyBindJump.isKeyDown() && !jumpTainted && lastFlightState == FlightState.NONE) {
						if (!ep.onGround && ep.motionY < 0) {
							newState = FlightState.FLYING;
						} else {
							newState = lastFlightState;
							jumpTainted = true;
						}
					} else {
						newState = lastFlightState;
					}
					if (ep.onGround && !lastOnGround) {
						newState = FlightState.NONE;
					}
					if (TOGGLE_FLIGHT_MODE.isKeyDown()) {
						if (!lastToggleFlightModeIsDown) {
							if (newState == FlightState.FLYING) {
								newState = FlightState.FLYING_FLIGHT_MODE;
							} else if (newState == FlightState.FLYING_FLIGHT_MODE) {
								newState = FlightState.FLYING;
							}
						}
						lastToggleFlightModeIsDown = true;
					} else {
						lastToggleFlightModeIsDown = false;
					}
					if (newState == FlightState.FLYING && wings.hasThruster()) {
						boolean jump = mc.gameSettings.keyBindJump.isKeyDown();
						boolean sneak = mc.gameSettings.keyBindSneak.isKeyDown();
						if (jump && !wp.afterburner && !wp.brake) {
							wp.afterburner = true;
							new SetThrusterMessage(SetThrusterMessage.AFTERBURNER_SPEED).sendToServer();
						} else if (!jump && wp.afterburner) {
							wp.afterburner = false;
							new SetThrusterMessage(0).sendToServer();
						} else if (sneak && !wp.brake && !wp.afterburner) {
							wp.brake = true;
							new SetThrusterMessage(SetThrusterMessage.BRAKE_SPEED).sendToServer();
						} else if (!sneak && wp.brake) {
							wp.brake = false;
							new SetThrusterMessage(0).sendToServer();
						}
					}
					lastOnGround = mc.player.onGround;
				} else {
					newState = FlightState.NONE;
				}
				if (newState != lastFlightState) {
					lastFlightState = newState;
					wp.flightState = newState;
					new SetFlightStateMessage(newState).sendToServer();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
		if (flightModeTicks > 0) {
			float partial = e.getPartialTicks();
			if (lastFlightState != FlightState.FLYING_FLIGHT_MODE) {
				partial *= -1;
			}
			float interp = MathHelper.sin((Math.min(flightModeTicks+partial, 10)/20f)*((float)Math.PI));
			if (e.getType() == ElementType.HOTBAR) {
				if (flightModeTicks > 10) {
					e.setCanceled(true);
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, interp*23, 0);
				}
			} else if (e.getType() == ElementType.ARMOR || e.getType() == ElementType.EXPERIENCE || e.getType() == ElementType.FOOD || e.getType() == ElementType.HEALTH) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, interp*23, 0);
			} else {
				GlStateManager.pushMatrix();
			}
		}
	}
	
	@SubscribeEvent
	public void onPostRenderGameOverlay(RenderGameOverlayEvent.Post e) {
		if (flightModeTicks > 0) {
			GlStateManager.popMatrix();
		}
		if (e.getType() == ElementType.HOTBAR) {
			
		} else if (e.getType() == ElementType.ALL) {
			if (flightTicks > 0) {
				EntityPlayerSP ep = Minecraft.getMinecraft().player;
				ItemStack chest = ep.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				double mY = ep.onGround ? 0 : ep.motionY;
				double speed = ep.motionX * ep.motionX + mY * mY + ep.motionZ * ep.motionZ;
				speed = MathHelper.sqrt(speed);
				speed /= 20;
				speed *= 3600;
				
				float interp;
				if (flightTicks < 9) {
					float partial = e.getPartialTicks();
					if (lastFlightState == FlightState.NONE) {
						partial *= -1;
					}
					interp = MathHelper.sin((Math.min(flightTicks+partial, 10)/20f)*((float)Math.PI));
				} else {
					interp = 1;
				}
				
				int y = 5;
				
				if (chest.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
					IFluidHandlerItem ifhi = chest.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
					GlStateManager.pushMatrix();
					int w = 60;
					int x = -(w + 5);
					GlStateManager.translate(interp*(-x), 0, 0);
					IFluidTankProperties[] props = ifhi.getTankProperties();
					for (int i = 0; i < props.length; i++) {
						IFluidTankProperties prop = props[i];
						IFluidTankProperties lastTickProp = lastTickFluidProperties != null && lastTickFluidProperties.length == props.length ? lastTickFluidProperties[i] : null;
						Gui.drawRect(x+4, y-1, x+w+6, y+11, 0xFF880000);
						Gui.drawRect(x+5, y, x+w+5, y+10, 0xFF222222);
						FluidStack content = prop.getContents();
						FluidStack lastTick = lastTickProp == null ? null : lastTickProp.getContents();
						if (content != null || lastTick != null) {
							FluidStack template = content == null ? lastTick : content;
							TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(template.getFluid().getStill(template).toString());
							Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
							double fluidAmt = template.amount;
							if (lastTick != null) {
								fluidAmt = lastTick.amount + ((lastTick.amount - template.amount) * e.getPartialTicks());
							}
							double amt = fluidAmt/prop.getCapacity();
							double px = amt*w;
							int full = (int)(px/16);
							GlStateManager.color(1, 1, 1);
							for (int j = 0; j < full; j++) {
								drawRect(x+5+(j*16), y, tas, 16, 10, false);
							}
							drawRect(x+5+(full*16), y, tas, px-(full*16), 10, false);
						}
						Gui.drawRect(x+5+(w/4), y-1, x+(w/4)+6, y+4, 0xFF880000);
						Gui.drawRect(x+5+(w/2), y-1, x+(w/2)+6, y+8, 0xFF880000);
						Gui.drawRect(x+5+((w/4)*3), y-1, x+((w/4)*3)+6, y+4, 0xFF880000);
						y += 18;
					}
					lastTickFluidProperties = props.clone();
					for (int i = 0; i < lastTickFluidProperties.length; i++) {
						lastTickFluidProperties[i] = new FluidTankProperties(lastTickFluidProperties[i].getContents(), lastTickFluidProperties[i].getCapacity());
					}
					GlStateManager.popMatrix();
				}
				
				GlStateManager.pushMatrix();
				FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
				String str = ((int)speed)+" km/h";
				int w = fr.getStringWidth(str);
				GlStateManager.translate(interp*(w+5), 0, 0);
				fr.drawStringWithShadow(str, -w, y, -1);
				GlStateManager.popMatrix();
			}
		}
	}

	public static void drawRect(double x, double y, TextureAtlasSprite tex, double w, double h, boolean flipped) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder bb = tess.getBuffer();
		
		float minU = tex.getInterpolatedU(0);
		float maxU = tex.getInterpolatedU(w);
		
		float minV = tex.getInterpolatedV(flipped ? h : 0);
		float maxV = tex.getInterpolatedV(flipped ? 0 : h);
		
		bb.begin(7, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x + 0, y + h, 0).tex(minU, maxV).endVertex();
		bb.pos(x + w, y + h, 0).tex(maxU, maxV).endVertex();
		bb.pos(x + w, y + 0, 0).tex(maxU, minV).endVertex();
		bb.pos(x + 0, y + 0, 0).tex(minU, minV).endVertex();
		tess.draw();
	}
	
}
