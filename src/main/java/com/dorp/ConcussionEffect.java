package com.dorp;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class ConcussionEffect extends MobEffect {
   public ConcussionEffect() {
      super(MobEffectCategory.HARMFUL, 5583633);
   }

   public void onEffectStarted(LivingEntity entity, int amplifier) {
      super.onEffectStarted(entity, amplifier);
      entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 0, false, false, false));
      entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, false));
      entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0, false, false, false));
   }
}
