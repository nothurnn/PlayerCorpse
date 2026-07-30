package com.playercorpse;

import com.mojang.logging.LogUtils;
import com.playercorpse.registry.PlayerCorpseEntities;
import com.playercorpse.registry.PlayerCorpseMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("playercorpse")
public class PlayerCorpseMod {
   public static final String MOD_ID = "playercorpse";
   private static final Logger LOGGER = LogUtils.getLogger();

   public PlayerCorpseMod(IEventBus modEventBus, ModContainer modContainer) {
      PlayerCorpseEntities.ENTITY_TYPES.register(modEventBus);
      PlayerCorpseMenus.MENU_TYPES.register(modEventBus);
      modContainer.registerConfig(Type.COMMON, PlayerCorpseConfig.SPEC);
      NeoForge.EVENT_BUS.register(new PlayerCorpseEvents());
      LOGGER.info("Corpse: Refined initialized.");
   }
}
