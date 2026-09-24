package com.dorp;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class NicotineRushEffect extends MobEffect {
   public NicotineRushEffect() {
      super(MobEffectCategory.BENEFICIAL, 16755200);
      this.addAttributeModifier(
         Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("dorp", "effect.nicotine_rush.attack_damage"), 3.0, Operation.ADD_VALUE
      );
   }
}
