package com.dorp;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class CrimsonKryaniteGLM extends LootModifier {
   public static final MapCodec<CrimsonKryaniteGLM> CODEC = MapCodec.unit(new CrimsonKryaniteGLM(new LootItemCondition[0]));
   private static final ResourceLocation[] TARGET_TABLES = new ResourceLocation[]{
      ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
      ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
      ResourceLocation.withDefaultNamespace("chests/buried_treasure"),
      ResourceLocation.withDefaultNamespace("chests/underwater_ruin_big")
   };
   private static final float SPAWN_CHANCE = 0.05F;
   private static final int MAX_Y = 16;

   public CrimsonKryaniteGLM(LootItemCondition[] conditions) {
      super(conditions);
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      ResourceLocation tableId = context.getQueriedLootTableId();
      boolean isTarget = false;

      for (ResourceLocation target : TARGET_TABLES) {
         if (target.equals(tableId)) {
            isTarget = true;
            break;
         }
      }

      if (!isTarget) {
         return generatedLoot;
      } else {
         if (context.hasParam(LootContextParams.ORIGIN)) {
            BlockPos pos = BlockPos.containing((Position)context.getParam(LootContextParams.ORIGIN));
            if (pos.getY() > 16) {
               return generatedLoot;
            }
         }

         if (context.getRandom().nextFloat() < 0.05F) {
            int count = 1 + context.getRandom().nextInt(2);
            generatedLoot.add(new ItemStack((ItemLike)DorpMod.CRIMSON_KRYANITE.get(), count));
         }

         return generatedLoot;
      }
   }
}
