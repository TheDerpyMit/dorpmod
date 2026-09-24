package com.dorp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@OnlyIn(Dist.CLIENT)
public class ClientRingHandler {
   private static final long LERP_DURATION_MS = 1500L;
   private static boolean lerpActive = false;
   private static float lerpYaw = 0.0F;
   private static float lerpPitch = 0.0F;
   private static long lerpStartMs = -1L;

   public static void resetState() {
      lerpActive = false;
      lerpYaw = 0.0F;
      lerpPitch = 0.0F;
      lerpStartMs = -1L;
   }

   public static void register(IEventBus modEventBus) {
      NeoForge.EVENT_BUS.addListener(ClientRingHandler::onPlayerTick);
   }

   public static void triggerTotemAnimation() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.gameRenderer.displayItemActivation(new ItemStack((ItemLike)DorpMod.CRIMSON_THREAD.get()));
      }
   }

   public static void triggerCameraLerp(double targetX, double targetY, double targetZ) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer lp = mc.player;
      if (lp != null) {
         double dx = targetX - lp.getX();
         double dy = targetY - lp.getEyeY();
         double dz = targetZ - lp.getZ();
         double horiz = Math.sqrt(dx * dx + dz * dz);
         lerpYaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
         lerpPitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
         lerpActive = true;
         lerpStartMs = System.currentTimeMillis();
         mc.getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)DorpMod.RING_CALMSTEPS.get(), 1.0F, 1.0F));
      }
   }

   public static void playAmbientSound(int soundId) {
      Minecraft mc = Minecraft.getInstance();

      SoundEvent sound = switch (soundId) {
         case 0 -> (SoundEvent)DorpMod.RING_CALMSTEPS.get();
         case 1 -> (SoundEvent)DorpMod.RING_DISTANTSTEPS.get();
         case 2 -> (SoundEvent)DorpMod.RING_CANYOUHEARME.get();
         case 3 -> (SoundEvent)DorpMod.RING_HESDOWN.get();
         case 4 -> (SoundEvent)DorpMod.RING_DRIPSONG.get();
         default -> (SoundEvent)DorpMod.RING_CALMSTEPS.get();
         case 6 -> (SoundEvent)DorpMod.RING_GOODBYE.get();
      };
      mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 1.0F));
   }

   private static void onPlayerTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer lp = mc.player;
      if (lp == null || event.getEntity() != lp) {
         if (lp == null || !lp.isAlive()) {
            lerpActive = false;
            lerpStartMs = -1L;
         }
         return;
      }
      ClientLevel level = mc.level;
         if (level != null) {
            CrimsonPhantomEntity targetPhantom = null;

            List<Entity> snapshot = new ArrayList<>();
            try {
               for (Entity entity : level.entitiesForRendering()) {
                  snapshot.add(entity);
               }
            } catch (RuntimeException e) {
               return;
            }

            for (Entity entity : snapshot) {
               if (entity instanceof CrimsonPhantomEntity phantom
                  && phantom.isJumpscare()
                  && phantom.getTargetPlayerUUID().map(uuid -> uuid.equals(lp.getUUID())).orElse(false)) {
                  targetPhantom = phantom;
                  break;
               }
            }

            if (targetPhantom != null) {
               double dx = targetPhantom.getX() - lp.getX();
               double dy = targetPhantom.getEyeY() - lp.getEyeY();
               double dz = targetPhantom.getZ() - lp.getZ();
               double horiz = Math.sqrt(dx * dx + dz * dz);
               float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
               float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
               lp.setYRot(yaw);
               lp.setXRot(pitch);
               lp.yRotO = yaw;
               lp.xRotO = pitch;
               if (lp.hasEffect(MobEffects.BLINDNESS)) {
                  lp.removeEffect(MobEffects.BLINDNESS);
               }
            } else if (lerpActive) {
               long elapsed = System.currentTimeMillis() - lerpStartMs;
               if (elapsed >= 1500L) {
                  lp.setYRot(lerpYaw);
                  lp.setXRot(lerpPitch);
                  lerpActive = false;
               } else {
                  float t = (float)elapsed / 1500.0F;
                  t = t * t * (3.0F - 2.0F * t);
                  float yawDiff = lerpYaw - lp.getYRot();

                  while (yawDiff > 180.0F) {
                     yawDiff -= 360.0F;
                  }

                  while (yawDiff < -180.0F) {
                     yawDiff += 360.0F;
                  }

                  float factor = t * 0.12F;
                  lp.setYRot(lp.getYRot() + yawDiff * factor);
                   lp.setXRot(lp.getXRot() + (lerpPitch - lp.getXRot()) * factor);
                }
             }
         }
   }

   public static void handleCrimsonWarning() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         boolean hasRing = false;

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).is((Item)DorpMod.CRIMSON_THREAD.get())) {
               hasRing = true;
               break;
            }
         }

         if (!hasRing) {
            Optional<SlotResult> curioOpt = CuriosApi.getCuriosInventory(mc.player)
               .flatMap(handler -> handler.findFirstCurio((Item)DorpMod.CRIMSON_THREAD.get()));
            if (curioOpt.isPresent()) {
               hasRing = true;
            }
         }

         if (hasRing) {
            DorpMod.showNativeWarning("BURN //IT\\\\", "System Error");
            DorpMod.showNativeWarning("GET RID OF //IT\\\\", "System Error");
            String msg = mc.player.getRandom().nextBoolean() ? "BURN //IT\\\\" : "GET RID OF //IT\\\\";
            MutableComponent component = Component.literal(msg)
               .withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.ITALIC});
            mc.player.displayClientMessage(component, true);
            mc.player.displayClientMessage(component, false);
         }
      }
   }
}
