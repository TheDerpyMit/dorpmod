package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HeadsetRenderer implements ICurioRenderer {
   public <T extends LivingEntity, M extends EntityModel<T>> void render(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack matrixStack,
      RenderLayerParent<T, M> renderLayerParent,
      MultiBufferSource renderTypeBuffer,
      int light,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      LivingEntity entity = slotContext.entity();
      if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
         matrixStack.pushPose();
         humanoidModel.head.translateAndRotate(matrixStack);
         matrixStack.translate(0.0F, -0.25F, 0.0F);
         matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         matrixStack.scale(0.625F, -0.625F, -0.625F);
         Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(stack, ItemDisplayContext.HEAD, light, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, entity.level(), entity.getId());
         matrixStack.popPose();
      }
   }
}
