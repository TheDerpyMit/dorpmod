package com.dorp;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class HimariVisorArmorRenderer extends GeoArmorRenderer<HimariVisorItem> {
   public static final HimariVisorArmorRenderer INSTANCE = new HimariVisorArmorRenderer();

   public HimariVisorArmorRenderer() {
      super(new HimariVisorModel());
      this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
   }
}
