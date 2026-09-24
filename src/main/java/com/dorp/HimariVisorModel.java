package com.dorp;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HimariVisorModel extends GeoModel<HimariVisorItem> {
   @Override
   public ResourceLocation getModelResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "geo/item/himari_visor.geo.json");
   }

   @Override
   public ResourceLocation getTextureResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "textures/item/himari_visor.png");
   }

   @Override
   public ResourceLocation getAnimationResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "animations/himari_visor.animation.json");
   }
}
