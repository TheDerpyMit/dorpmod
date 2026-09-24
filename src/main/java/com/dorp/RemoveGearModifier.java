package com.dorp;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class RemoveGearModifier extends LootModifier {
   public static final MapCodec<RemoveGearModifier> CODEC = MapCodec.unit(new RemoveGearModifier(new LootItemCondition[0]));

   public RemoveGearModifier(LootItemCondition[] conditions) {
      super(conditions);
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      ObjectArrayList<ItemStack> filteredLoot = new ObjectArrayList();
      ObjectListIterator var4 = generatedLoot.iterator();

      while (var4.hasNext()) {
         ItemStack stack = (ItemStack)var4.next();
         Item item = stack.getItem();
         if (item != Items.DIAMOND_CHESTPLATE
            && item != Items.DIAMOND_LEGGINGS
            && item != Items.NETHERITE_HELMET
            && item != Items.NETHERITE_CHESTPLATE
            && item != Items.NETHERITE_LEGGINGS
            && item != Items.NETHERITE_BOOTS) {
            filteredLoot.add(stack);
         }
      }

      return filteredLoot;
   }
}
