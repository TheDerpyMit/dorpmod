package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class HimariVisorItemRenderer extends GeoItemRenderer<HimariVisorItem> {
   private static final double CX = 0.2675;
   private static final double CY = 2.097;
   private static final double CZ = 0.014;

   private static final double IX = 0.0;
   private static final double IY = 0.38;
   private static final double IZ = 0.014;

   public static final GeoItemRenderer<HimariVisorItem> GUI_RENDERER = createGuiRenderer();

   public HimariVisorItemRenderer(GeoModel<HimariVisorItem> model) {
      super(model);
   }

   private static GeoItemRenderer<HimariVisorItem> createGuiRenderer() {
      GeoItemRenderer<HimariVisorItem> renderer = new GeoItemRenderer<>(new HimariVisorItemModel());
      renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer));
      return renderer;
   }

   private static void fit(PoseStack poses, double scale, double tx, double ty, double tz) {
      poses.translate(tx, ty, tz);
      poses.scale((float) scale, (float) scale, (float) scale);
      poses.translate(-CX, -CY, -CZ);
   }

   @Override
   public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poses,
         MultiBufferSource buffer, int packedLight, int packedOverlay) {
      if (ctx == ItemDisplayContext.GUI) {
         poses.translate(0.0, 0.38, 0.0);
         poses.scale(0.9F, 0.9F, 0.9F);
         poses.translate(-IX, -IY, -IZ);
         GUI_RENDERER.renderByItem(stack, ctx, poses, buffer, packedLight, packedOverlay);
         return;
      }
      switch (ctx) {
         case FIRST_PERSON_RIGHT_HAND:
         case FIRST_PERSON_LEFT_HAND:
            fit(poses, 0.5, 0.35, -0.25, -0.7);
            break;
         case THIRD_PERSON_RIGHT_HAND:
         case THIRD_PERSON_LEFT_HAND:
            fit(poses, 0.55, 0.0, 0.0, 0.0);
            break;
         case GROUND:
            fit(poses, 0.45, 0.0, 0.3, 0.0);
            break;
         case FIXED:
            fit(poses, 0.5, 0.0, 0.0, 0.0);
            break;
         default:
            break;
      }
      super.renderByItem(stack, ctx, poses, buffer, packedLight, packedOverlay);
   }
}
