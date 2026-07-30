package com.playercorpse.registry;

import com.playercorpse.menu.PlayerCorpseMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PlayerCorpseMenus {
   public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, "playercorpse");
   public static final DeferredHolder<MenuType<?>, MenuType<PlayerCorpseMenu>> CORPSE_MENU = MENU_TYPES.register(
      "player_corpse_menu",
      () -> new MenuType<>(
         (containerId, playerInventory) -> new PlayerCorpseMenu(PlayerCorpseMenus.CORPSE_MENU.get(), containerId, playerInventory), FeatureFlags.VANILLA_SET
      )
   );

   private PlayerCorpseMenus() {
   }
}
