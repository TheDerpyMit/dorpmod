package com.dorp.mixin;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.hollingsworth.nuggets.internal.ClientEvents", remap = false)
public class NuggetsClientEventsMixin {
   @Inject(method = "clientSetup", at = @At("HEAD"), cancellable = true)
   private static void cancelClientSetup(FMLClientSetupEvent event, CallbackInfo ci) {
      ci.cancel();
   }
}
