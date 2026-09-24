package com.dorp;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMonsterRoarHandler {
   public static void handle(double x, double y, double z) {
      Level level = Minecraft.getInstance().level;
      if (level != null) {
         level.addParticle(ParticleTypes.SONIC_BOOM, x, y + 1.0, z, 0.0, 0.0, 0.0);
         level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0, z, 0.0, 0.0, 0.0);

         for (int i = 0; i < 8; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 3.0;
            double dz = (level.random.nextDouble() - 0.5) * 3.0;
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + dx, y + 0.5, z + dz, 0.0, 0.2, 0.0);
         }

         if (y > -1000.0) {

            level.playLocalSound(x, y, z, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.0F, 1.0F, false);
         }
      }
   }
}
