package com.dorp;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class OverSmokingEffect extends MobEffect {
   public OverSmokingEffect() {
      super(MobEffectCategory.HARMFUL, 5592405);
      this.addAttributeModifier(
         Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("dorp", "effect.over_smoking.slowness"), -0.15, Operation.ADD_MULTIPLIED_TOTAL
      );
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      int i = 25 >> amplifier;
      return i > 0 ? duration % i == 0 : true;
   }

   public boolean applyEffectTick(LivingEntity entity, int amplifier) {
      if (entity.getHealth() > 1.0F) {
         entity.hurt(entity.damageSources().magic(), 1.0F);
      }

      return true;
   }
}
