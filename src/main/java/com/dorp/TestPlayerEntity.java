package com.dorp;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;

public class TestPlayerEntity extends PathfinderMob {
   public TestPlayerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
   }

   public static Builder createAttributes() {
      return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0);
   }
}
