package com.dorp;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class ForbiddenWorkbenchMenu extends CraftingMenu {
   public final ContainerLevelAccess access;

   public ForbiddenWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
      super(containerId, playerInventory, access);
      this.access = access;
   }
}
