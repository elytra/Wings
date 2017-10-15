package com.elytradev.wings.client.render;

import java.util.Optional;

import javax.vecmath.Quat4d;

import com.elytradev.concrete.reflect.accessor.Accessor;
import com.elytradev.concrete.reflect.accessor.Accessors;
import com.elytradev.concrete.reflect.invoker.Invoker;
import com.elytradev.concrete.reflect.invoker.Invokers;
import com.elytradev.wings.WingsPlayer;
import com.elytradev.wings.client.Rendering;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class WingsRenderPlayer extends RenderPlayer {

	private final RenderPlayer delegate;
	
	private final Invoker applyRotations = Invokers.findMethod(RenderLivingBase.class, "applyRotations", "func_77043_a", EntityLivingBase.class, float.class, float.class, float.class);
	private final Invoker renderModel = Invokers.findMethod(RenderLivingBase.class, "renderModel", "func_77036_a", EntityLivingBase.class, float.class, float.class, float.class, float.class, float.class, float.class);
	private final Invoker setFlag = Invokers.findMethod(Entity.class, "setFlag", "func_70052_a", int.class, boolean.class);
	
	private final Accessor<Integer> ticksElytraFlying = Accessors.findField(EntityLivingBase.class, "field_184629_bo", "ticksElytraFlying");
	
	public WingsRenderPlayer(RenderPlayer delegate) {
		super(delegate.getRenderManager());
		this.delegate = delegate;
		
		this.layerRenderers = delegate.layerRenderers;
		this.brightnessBuffer = delegate.brightnessBuffer;
		this.mainModel = delegate.mainModel;
	}

	
	@Override
	protected void applyRotations(AbstractClientPlayer acp, float p_77043_2_, float rotationYaw, float partialTicks) {
		Optional<WingsPlayer> opt = WingsPlayer.getIfExists(acp);
		if (opt.isPresent()) {
			WingsPlayer wp = opt.get();
			if (wp.rotation != null) {
				Quat4d prev = wp.prevRotation == null ? new Quat4d(0, 0, 0, 1) : (Quat4d)wp.prevRotation.clone();
				Quat4d cur = (Quat4d)wp.rotation.clone();
				prev.conjugate();
				cur.conjugate();
				Rendering.class.getName();
				Rendering.rotate(prev, cur, partialTicks);
				
				GlStateManager.rotate(270, 1, 0, 0);
				
				GlStateManager.translate(0, -0.8f, 0);
				return;
			}
		}
		applyRotations.invoke(delegate, acp, p_77043_2_, rotationYaw, partialTicks);
	}
	
	@Override
	protected void renderModel(AbstractClientPlayer acp, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		Optional<WingsPlayer> opt = WingsPlayer.getIfExists(acp);
		if (opt.isPresent()) {
			WingsPlayer wp = opt.get();
			if (wp.rotation != null) {
				setFlag.invoke(acp, 7, false);
				int oldTicksElytraFlying = acp.getTicksElytraFlying();
				ticksElytraFlying.set(acp, 0);
				getMainModel().isChild = false;
				renderModel.invoke(delegate, acp, 0, 0, ageInTicks, 0, -60, scaleFactor);
				ticksElytraFlying.set(acp, oldTicksElytraFlying);
				setFlag.invoke(acp, 7, true);
				return;
			}
		}
		getMainModel().isChild = false;
		renderModel.invoke(delegate, acp, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}
	
	@Override
	public void setRenderOutlines(boolean renderOutlinesIn) {
		delegate.setRenderOutlines(renderOutlinesIn);
	}

	@Override
	public boolean shouldRender(AbstractClientPlayer livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return delegate.shouldRender(livingEntity, camera, camX, camY, camZ);
	}

	@Override
	public <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean addLayer(U layer) {
		if (delegate == null) return false;
		return delegate.addLayer(layer);
	}

	@Override
	public ModelPlayer getMainModel() {
		if (delegate == null) return super.getMainModel();
		return delegate.getMainModel();
	}

	@Override
	public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public void bindTexture(ResourceLocation location) {
		delegate.bindTexture(location);
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
		return delegate.getEntityTexture(entity);
	}

	@Override
	public void transformHeldFull3DItemLayer() {
		delegate.transformHeldFull3DItemLayer();
	}

	@Override
	public float prepareScale(AbstractClientPlayer entitylivingbaseIn,
			float partialTicks) {
		return delegate.prepareScale(entitylivingbaseIn, partialTicks);
	}

	@Override
	public void renderRightArm(AbstractClientPlayer clientPlayer) {
		delegate.renderRightArm(clientPlayer);
	}

	@Override
	public void renderLeftArm(AbstractClientPlayer clientPlayer) {
		delegate.renderLeftArm(clientPlayer);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
		delegate.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
	}

	@Override
	public FontRenderer getFontRendererFromRenderManager() {
		return delegate.getFontRendererFromRenderManager();
	}

	@Override
	public RenderManager getRenderManager() {
		return delegate.getRenderManager();
	}

	@Override
	public boolean isMultipass() {
		return delegate.isMultipass();
	}

	@Override
	public void renderMultipass(AbstractClientPlayer p_188300_1_, double p_188300_2_, double p_188300_4_, double p_188300_6_, float p_188300_8_, float p_188300_9_) {
		delegate.renderMultipass(p_188300_1_, p_188300_2_, p_188300_4_,
				p_188300_6_, p_188300_8_, p_188300_9_);
	}

	@Override
	public void renderName(AbstractClientPlayer entity, double x, double y, double z) {
		delegate.renderName(entity, x, y, z);
	}
	
}
