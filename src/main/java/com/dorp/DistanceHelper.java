package com.dorp;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public class DistanceHelper {
   public static double distanceSq(Level level, Position a, Position b) {
      return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a, b);
   }
}
