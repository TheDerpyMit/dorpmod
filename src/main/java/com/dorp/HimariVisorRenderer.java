package com.dorp;

import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class HimariVisorRenderer {
   public static final HimariVisorModel MODEL = new HimariVisorModel();
   public static final GeoItemRenderer<HimariVisorItem> GEO_RENDERER = createRenderer();

   private HimariVisorRenderer() {
   }

   private static GeoItemRenderer<HimariVisorItem> createRenderer() {
      HimariVisorItemRenderer renderer = new HimariVisorItemRenderer(MODEL);
      renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer));
      return renderer;
   }
}
