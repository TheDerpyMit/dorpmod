package com.dorp.mixin;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
   @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
   private void dorpCreateTitle(CallbackInfoReturnable<String> cir) {
       String version = "1.83";

      try {
         if (ModList.get() != null) {
            version = ModList.get().getModContainerById("dorp").map(c -> c.getModInfo().getVersion().toString()).orElse(version);
         }
      } catch (Exception var4) {
      }

      cir.setReturnValue("dorpmod - v" + version + " [ACTIVE] [ NeoForge 1.21.1 ]");
   }
}
