package com.dorp;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class TestPlayerRenderer extends LivingEntityRenderer<TestPlayerEntity, PlayerModel<TestPlayerEntity>> {
   public TestPlayerRenderer(Context context) {
      super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
   }

   public ResourceLocation getTextureLocation(TestPlayerEntity entity) {
      return ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
   }
}
