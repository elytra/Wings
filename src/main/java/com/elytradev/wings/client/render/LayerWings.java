package com.elytradev.wings.client.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.elytradev.wings.item.ItemWings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerWings implements LayerRenderer<EntityLivingBase> {
	protected final RenderLivingBase<?> renderPlayer;
	
	private static TileEntityBanner bannerDummy = new TileEntityBanner();

	public LayerWings(RenderLivingBase<?> renderPlayer) {
		this.renderPlayer = renderPlayer;
	}

	@Override
	public void doRenderLayer(EntityLivingBase elb,
			float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack is = elb .getItemStackFromSlot(EntityEquipmentSlot.CHEST);

		if (is.getItem() instanceof ItemWings) {
			GlStateManager.color(1, 1, 1);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ZERO);


			GlStateManager.pushMatrix();
			GlStateManager.translate(-(2/16f), 0f, 0f);
			if (elb.isSneaking()) {
				GlStateManager.rotate(30f, 1, 0, 0);
				GlStateManager.translate(0, (4/16f), 0f);
			} else {
				GlStateManager.translate(0, 0, (2/16f));
			}

			Vec3d forward = elb.getForward();
			forward = forward.subtract(0, forward.y, 0).normalize();
			Vec3d motion = new Vec3d(elb.motionX, 0, elb.motionZ).normalize();
			
			double speed = MathHelper.sqrt(Math.abs(elb.motionX * elb.motionX) + Math.abs(elb.motionZ * elb.motionZ));
			speed *= motion.dotProduct(forward);
			if (speed > 0.5) speed = 0.5;
			
			float rot = 15f+(float)(speed*180f);
			
			drawWings(is, rot);

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
	
	public static void drawWings(ItemStack stack, float rot) {
		if (stack.getItem() instanceof ItemWings) {
			ItemWings wings = (ItemWings)stack.getItem();
			
			ItemStack leftWing = stack.copy();
			ItemStack rightWing = stack.copy();
			if (stack.hasTagCompound()) {
				if (stack.getTagCompound().hasKey("LeftWing")) {
					leftWing.getTagCompound().removeTag("LeftWing");
					leftWing.getTagCompound().setTag("BlockEntityTag", stack.getTagCompound().getTag("LeftWing"));
				}
				if (stack.getTagCompound().hasKey("RightWing")) {
					rightWing.getTagCompound().removeTag("RightWing");
					rightWing.getTagCompound().setTag("BlockEntityTag", stack.getTagCompound().getTag("RightWing"));
				}
			}
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			
			String base = wings.getBaseMaterial();
			renderCube(0, 0, 0, 4, 12, wings.hasThruster() ? 4 : 2, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(base), true, true, true, 0f);
			
			GlStateManager.translate((2/16f), 0f, ((wings.hasThruster() ? 2 : 1)/16f));
			
			bannerDummy.setItemValues(leftWing, true);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate((2/16f), 0, 0);
			GlStateManager.rotate(-rot, 0, 1, 0);
			GlStateManager.translate((0/16f), 0, 0);
			renderWing(0, 0, 0, 10, 20, bannerDummy.getPatternResourceLocation(), bannerDummy.getPatternList(), bannerDummy.getColorList(), 2);
			GlStateManager.popMatrix();
			
			
			bannerDummy.setItemValues(rightWing, true);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(-(2/16f), 0, 0);
			GlStateManager.rotate(rot, 0, 1, 0);
			GlStateManager.translate(-(10/16f), 0, 0);
			renderWing(0, 0, 0, 10, 20, bannerDummy.getPatternResourceLocation(), bannerDummy.getPatternList(), bannerDummy.getColorList(), -2);
			GlStateManager.popMatrix();
		}
	}
	
	private static void renderWing(int x, int y, int z, float w, float h, String patternResLoc, List<BannerPattern> patternList, List<EnumDyeColor> colorList, float skew) {
		ResourceLocation tex = BannerTextures.BANNER_DESIGNS.getResourceLocation(patternResLoc, patternList, colorList);
		Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		
		float minU = 21/64f;
		float maxU = 1/64f;
		
		float minV = 1/64f;
		float maxV = 39/64f;
		
		float s = 1/16f;
		
		float sk1;
		float sk2;
		
		if (skew == 0) {
			sk1 = 0;
			sk2 = 0;
		} else if (skew < 0) {
			sk1 = 0;
			sk2 = -skew;
			float swap = minU;
			minU = maxU;
			maxU = swap;
		} else {
			sk1 = skew;
			sk2 = 0;
		}
		
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		vb.pos((x+0)*s, (y+h+sk2)*s, (z-0.01)*s).tex(minU, maxV).normal(0, 0, -1).endVertex();
		vb.pos((x+w)*s, (y+h+sk1)*s, (z-0.01)*s).tex(maxU, maxV).normal(0, 0, -1).endVertex();
		vb.pos((x+w)*s, (y+0+sk1)*s, (z-0.01)*s).tex(maxU, minV).normal(0, 0, -1).endVertex();
		vb.pos((x+0)*s, (y+0+sk2)*s, (z-0.01)*s).tex(minU, minV).normal(0, 0, -1).endVertex();
		
		vb.pos((x+w)*s, (y+h+sk1)*s, (z)*s).tex(maxU, maxV).normal(0, 0, 1).endVertex();
		vb.pos((x+0)*s, (y+h+sk2)*s, (z)*s).tex(minU, maxV).normal(0, 0, 1).endVertex();
		vb.pos((x+0)*s, (y+0+sk2)*s, (z)*s).tex(minU, minV).normal(0, 0, 1).endVertex();
		vb.pos((x+w)*s, (y+0+sk1)*s, (z)*s).tex(maxU, minV).normal(0, 0, 1).endVertex();
		
		tess.draw();
	}
	
	private static void renderCube(int x, int y, int z, float w, float h, float d, TextureAtlasSprite tas, boolean renderTop, boolean renderBottom, boolean renderSides, float skew) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		
		float minVX = tas.getInterpolatedV(x);
		float maxVX = tas.getInterpolatedV(x+w);
		float minVY = tas.getInterpolatedV(y);
		float maxVY = tas.getInterpolatedV(y+h);
		
		float minUX = tas.getInterpolatedU(x);
		float maxUX = tas.getInterpolatedU(x+w);
		float minUZ = tas.getInterpolatedU(z);
		float maxUZ = tas.getInterpolatedU(z+d);
		
		float s = 1/16f;
		
		float sk1;
		float sk2;
		
		if (skew == 0) {
			sk1 = 0;
			sk2 = 0;
		} else if (skew < 0) {
			sk1 = 0;
			sk2 = -skew;
		} else {
			sk1 = skew;
			sk2 = 0;
		}
		
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		if (renderSides) {
			vb.pos((x+0)*s, (y+h+sk2)*s, (z+0)*s).tex(minUX, maxVY).normal(0, 0, -1).endVertex();
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+0)*s).tex(maxUX, maxVY).normal(0, 0, -1).endVertex();
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+0)*s).tex(maxUX, minVY).normal(0, 0, -1).endVertex();
			vb.pos((x+0)*s, (y+0+sk2)*s, (z+0)*s).tex(minUX, minVY).normal(0, 0, -1).endVertex();
			
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+d)*s).tex(maxUX, maxVY).normal(0, 0, 1).endVertex();
			vb.pos((x+0)*s, (y+h+sk2)*s, (z+d)*s).tex(minUX, maxVY).normal(0, 0, 1).endVertex();
			vb.pos((x+0)*s, (y+0+sk2)*s, (z+d)*s).tex(minUX, minVY).normal(0, 0, 1).endVertex();
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+d)*s).tex(maxUX, minVY).normal(0, 0, 1).endVertex();
			
			
			vb.pos((x+0)*s, (y+0)*s, (z+d)*s).tex(maxUZ, minVY).normal(-1, 0, 0).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+d)*s).tex(maxUZ, maxVY).normal(-1, 0, 0).endVertex();
			vb.pos((x+0)*s, (y+h)*s, (z+0)*s).tex(minUZ, maxVY).normal(-1, 0, 0).endVertex();
			vb.pos((x+0)*s, (y+0)*s, (z+0)*s).tex(minUZ, minVY).normal(-1, 0, 0).endVertex();
			
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+0)*s).tex(minUZ, minVY).normal(1, 0, 0).endVertex();
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+0)*s).tex(minUZ, maxVY).normal(1, 0, 0).endVertex();
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+d)*s).tex(maxUZ, maxVY).normal(1, 0, 0).endVertex();
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+d)*s).tex(maxUZ, minVY).normal(1, 0, 0).endVertex();
		}
		
		if (renderBottom) {
			vb.pos((x+0)*s, (y+0+sk2)*s, (z+0)*s).tex(minUZ, minVX).normal(0, -1, 0).endVertex();
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+0)*s).tex(minUZ, maxVX).normal(0, -1, 0).endVertex();
			vb.pos((x+w)*s, (y+0+sk1)*s, (z+d)*s).tex(maxUZ, maxVX).normal(0, -1, 0).endVertex();
			vb.pos((x+0)*s, (y+0+sk2)*s, (z+d)*s).tex(maxUZ, minVX).normal(0, -1, 0).endVertex();
		}
		
		if (renderTop) {
			vb.pos((x+0)*s, (y+h+sk2)*s, (z+0)*s).tex(minUZ, minVX).normal(0, 1, 0).endVertex();
			vb.pos((x+0)*s, (y+h+sk2)*s, (z+d)*s).tex(maxUZ, minVX).normal(0, 1, 0).endVertex();
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+d)*s).tex(maxUZ, maxVX).normal(0, 1, 0).endVertex();
			vb.pos((x+w)*s, (y+h+sk1)*s, (z+0)*s).tex(minUZ, maxVX).normal(0, 1, 0).endVertex();
			
		}
		tess.draw();
	}
}