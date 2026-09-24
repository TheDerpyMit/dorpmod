package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrimsonPhantomRenderer extends LivingEntityRenderer<CrimsonPhantomEntity, GlitchingPhantomModel> {
   public CrimsonPhantomRenderer(Context ctx) {
      super(ctx, new GlitchingPhantomModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
   }

   public ResourceLocation getTextureLocation(CrimsonPhantomEntity entity) {
      Optional<UUID> optUuid = entity.getTargetPlayerUUID();
      if (optUuid.isPresent()) {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn != null) {
            PlayerInfo info = conn.getPlayerInfo(optUuid.get());
            if (info != null) {
               return info.getSkin().texture();
            }
         }
      }

      Minecraft mc = Minecraft.getInstance();
      return mc.player != null ? mc.player.getSkin().texture() : ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
   }

   public void render(CrimsonPhantomEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buf, int light) {
      pose.pushPose();
      float tick = entity.tickCount + partialTick;
      if (entity.isJumpscare() && entity.tickCount >= 20) {
         float pulse = 1.0F + 0.06F * (float)Math.abs(Math.sin(tick * 0.6F));
         pose.scale(pulse, pulse, pulse);
      } else {
         float pulse = 1.0F + 0.015F * (float)Math.sin(tick * 0.15F);
         pose.scale(pulse, pulse, pulse);
      }

      super.render(entity, yaw, partialTick, pose, buf, light);
      pose.popPose();
   }

   protected boolean shouldShowName(CrimsonPhantomEntity entity) {
      return false;
   }
}
