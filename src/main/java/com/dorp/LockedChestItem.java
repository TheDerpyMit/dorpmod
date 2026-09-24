package com.dorp;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LockedChestItem extends BlockItem {
   public LockedChestItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      tooltipComponents.add(Component.literal("A secure chest that only you and your trusted friends can open.").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.literal("Set a password on placement. Beware of Breachers!").withStyle(ChatFormatting.RED));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(
         new IClientItemExtensions() {
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
               return new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
                  private final ChestBlockEntity chest = new LockedChestBlockEntity(BlockPos.ZERO, ((Block)DorpMod.LOCKED_CHEST.get()).defaultBlockState());

                  public void renderByItem(
                     ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay
                  ) {
                     Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(this.chest, poseStack, buffer, packedLight, packedOverlay);
                  }
               };
            }
         }
      );
   }
}
