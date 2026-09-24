package com.dorp;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;

public class TestPlayerItem extends Item {
   public TestPlayerItem(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide() && context.getLevel() instanceof ServerLevel serverLevel) {
         BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
         TestPlayerEntity dummy = (TestPlayerEntity)((EntityType)DorpMod.TEST_PLAYER_ENTITY.get()).create(serverLevel);
         if (dummy != null) {
            dummy.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            dummy.setCustomName(Component.literal("Test Player_" + (serverLevel.random.nextInt(9000) + 1000)));
            dummy.setCustomNameVisible(true);
            dummy.setNoAi(true);
            serverLevel.addFreshEntity(dummy);
            context.getPlayer()
               .displayClientMessage(Component.literal("Spawned Dummy Player: " + dummy.getCustomName().getString()).withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("Right-click a block to spawn a Test Player Dummy.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(
         Component.literal("Used for testing items that target entities (like Tracker).")
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
      );
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }
}
