package com.dorp;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Inventory/display variant of the visor (halo + base shell, no face
 * plates). Uses no-op loops so the shared mood controller never errors
 * on missing bones.
 */
public class HimariVisorItemModel extends GeoModel<HimariVisorItem> {
   @Override
   public ResourceLocation getModelResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "geo/item/himari_visor_item.geo.json");
   }

   @Override
   public ResourceLocation getTextureResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "textures/item/himari_visor.png");
   }

   @Override
   public ResourceLocation getAnimationResource(HimariVisorItem object) {
      return ResourceLocation.fromNamespaceAndPath("dorp", "animations/himari_visor_item.animation.json");
   }
}
