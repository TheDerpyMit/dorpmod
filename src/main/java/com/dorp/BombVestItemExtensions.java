package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class BombVestItemExtensions {
   public static final IClientItemExtensions INSTANCE = new IClientItemExtensions() {
      public HumanoidModel<?> getHumanoidArmorModel(final LivingEntity entity, final ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
         if (slot != EquipmentSlot.CHEST) {
            return original;
         } else {
            ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR);
            return new HumanoidModel<LivingEntity>(root) {
               public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
                  if (this.body.visible) {
                     poseStack.pushPose();
                     this.body.translateAndRotate(poseStack);
                     poseStack.translate(0.0F, 0.25F, 0.0F);
                     poseStack.scale(1.0F, -1.0F, 1.0F);
                     MultiBufferSource baseBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                     MultiBufferSource bufferSource = renderType -> !renderType.toString().contains("glint") && !renderType.toString().contains("foil")
                        ? baseBufferSource.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
                        : baseBufferSource.getBuffer(renderType);
                     Minecraft.getInstance()
                        .getItemRenderer()
                        .renderStatic(stack, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, bufferSource, entity.level(), entity.getId());
                     poseStack.popPose();
                  }
               }
            };
         }
      }
   };
}
